package net.jmecn.zkxui.cli;

import lombok.extern.slf4j.Slf4j;
import net.jmecn.zkxui.client.ZkXuiApp;
import net.jmecn.zkxui.client.utils.StringUtils;
import net.jmecn.zkxui.client.vo.Env;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/3/7
 */
@Slf4j
@Command(name = "export", mixinStandardHelpOptions = true, version = "export 1.0.0",
        description = "Export configurations from zookeeper.", sortOptions = false)
public class Export implements Callable<Integer> {

    private static final String ALL_IN_ONE = "ALL-IN-ONE";// 默认ALL-IN-ONE文件名
    private static final String SUFFIX = "properties";// 默认后缀名

    @Option(names = {"-s", "--server"}, required = true, description = "zookeeper bootstrap server address. (eg: 127.0.0.1:2181,127.0.0.2.2:2181)")
    private String server;

    @Option(names = { "-o", "--output-dir" }, defaultValue = ".", paramLabel = "OUTPUT", description = "the output directory, default: .")
    private File outputDir;

    @Option(names = {"-a", "--all-in-one"}, description = "export all config path to one file.")
    private boolean allInOne;

    @Option(names = {"-b", "--both"}, description = "export both ALL-IN-ONE and separated config file.")
    private boolean both;

    @Option(names = {"-n", "--suffix-name"}, defaultValue = SUFFIX, description = "suffix name for export files (eg: txt), default: properties")
    private String suffix;

    @Option(names = {"-f", "--force-overwrite"}, description = "force overwrite the existing files.")
    private boolean forceOverwrite;

    @Option(names = {"-t", "--session-timeout"}, description = "zookeeper connection session timeout(in millis).")
    private Integer timeout;

    @Parameters(paramLabel = "CONFIG_PATH", description = "one or more config path to export (eg: /config/scrm-context-prod)")
    private String[] configPaths;

    private static final String INFO = "info";
    private static final String WARN = "warn";

    private static String format(String msg, Object ... args) {
        String fmt = msg;
        int i = 0;
        while (fmt.contains("{}")) {
            fmt = fmt.replaceFirst("\\{}", "{" + i + "}");
            i++;
        }
        return MessageFormat.format(fmt, args);
    }

    private void writeLog(String level, String msg, Object ... args) {
        switch (level) {
            case INFO:
                log.info(msg, args);
                System.out.println(format(msg, args));
                break;
            case WARN:
                log.warn(msg, args);
                System.err.println(format(msg, args));
                break;
        }
    }
    @Override
    public Integer call() throws Exception {
        if (configPaths == null || configPaths.length == 0) {
            writeLog(WARN, "CONFIG_PATH not found");
            return 1;
        }

        if (outputDir == null) {
            writeLog(WARN, "outputDir is null");
            return 1;
        }

        if (!outputDir.isDirectory()) {
            writeLog(WARN, "outputDir is not a directory.");
            return 1;
        }
        if (!outputDir.exists()) {
            writeLog(WARN, "outputDir not exists.");
            return 1;
        }

        writeLog(INFO, "connect server:{}", server);
        Env env = new Env("server", server);
        ZkXuiApp app = new ZkXuiApp(env);

        if (timeout != null && timeout > 0) {
            app.setZkSessionTimeout(timeout);
        }

        if (!app.connect()) {
            writeLog(WARN, "connect failed.");
            return 1;
        }

        if (both) {
            allInOne(app);
            output(app);
        } else {
            if (allInOne) {
                allInOne(app);
            } else {
                output(app);
            }
        }

        writeLog(INFO, "Done!");
        app.disconnect();
        return 0;
    }

    private void allInOne(ZkXuiApp app) {
        writeLog(INFO, "create ALL-ON-ONE file..");

        File file = new File(outputDir, getFileName(ALL_IN_ONE, suffix));

        if (file.exists()) {
            if (forceOverwrite) {
                writeLog(INFO, "force overwrite exists file:{}", file.getAbsolutePath());
            } else {
                writeLog(INFO, "file exists:{}", file.getAbsolutePath());
                return;
            }
        }

        boolean write = false;
        try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
            for (String path : configPaths) {
                if (!app.exists(path)) {
                    writeLog(WARN, "config path not found:{}", path);
                    continue;
                }
                writeLog(INFO, "exporting config path:{}", path);
                String content = app.export(path, false);
                out.println(content);
                write = true;
            }
            out.flush();
            writeLog(INFO, "save {} to {}.", Arrays.toString(configPaths), file.getAbsolutePath());
        } catch (IOException e) {
            log.error("export failed", e);
            if (file.delete()) {
                writeLog(INFO, "delete {}", file.getAbsolutePath());
            }
        }

        if (!write && file.exists()) {
            if (file.delete()) {
                writeLog(INFO, "NOTHING DUMP, delete {}", file.getAbsolutePath());
            } else {
                writeLog(INFO, "delete failed. {}", file.getAbsolutePath());
            }
        }
    }

    private void output(ZkXuiApp app) {
        writeLog(INFO, "create separated file..");

        for (String path : configPaths) {
            if (!app.exists(path)) {
                writeLog(WARN, "config path not found:{}", path);
                continue;
            }

            String filename = getFileName(path, suffix);
            File file = new File(outputDir, filename);
            writeLog(INFO, "exporting config path:{}, file:{}", path, file.getAbsolutePath());

            if (file.exists()) {
                if (forceOverwrite) {
                    writeLog(WARN, "force overwrite exists file:{}", file.getAbsolutePath());
                } else {
                    writeLog(WARN, "file exists:{}", file.getAbsolutePath());
                    continue;
                }
            }

            String content = app.export(path, false);

            try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
                out.print(content);
                out.flush();
                writeLog(INFO, "save {} to {}.", path, file.getAbsolutePath());
            } catch (IOException e) {
                log.error("export failed, path:{}", path, e);
                if (file.delete()) {
                    writeLog(INFO, "delete {}", file.getAbsolutePath());
                }
            }
        }
    }

    private String getFileName(String path, String suffix) {
        String name;

        // 移除路径前面的目录
        int idx = path.lastIndexOf('/');
        if (idx < 0) {
            name = path;
        } else {
            name = path.substring(idx + 1);
        }

        // 补充文件后缀名
        if (name.indexOf('.') < 0 && StringUtils.isNotBlank(suffix)) {
            if (suffix.startsWith(".")) {
                name = name + suffix;
            } else {
                name = name + "." + suffix;
            }
        }
        return name;
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new Export()).execute(args));
    }
}
