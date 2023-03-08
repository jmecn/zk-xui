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

    @Option(names = {"-n", "--suffix-name"}, defaultValue = SUFFIX, description = "suffix name for export files (eg: txt), default: properties")
    private String suffix;

    @Option(names = {"-f", "--force-overwrite"}, description = "force overwrite the existing files.")
    private boolean forceOverwrite;

    @Option(names = {"-t", "--session-timeout"}, description = "zookeeper connection session timeout(in millis).")
    private Integer timeout;

    @Parameters(paramLabel = "CONFIG_PATH", description = "one or more config path to export (eg: /config/scrm-context-prod)")
    private String[] configPaths;

    @Override
    public Integer call() throws Exception {
        if (configPaths == null || configPaths.length == 0) {
            log.warn("CONFIG_PATH not found");
            return 1;
        }

        if (outputDir == null) {
            log.warn("outputDir is null");
            return 1;
        }

        if (!outputDir.isDirectory()) {
            log.warn("outputDir is not a directory.");
            return 1;
        }
        if (!outputDir.exists()) {
            log.warn("outputDir not exists.");
            return 1;
        }

        log.info("connect server:{}", server);
        Env env = new Env("server", server);
        ZkXuiApp app = new ZkXuiApp(env);

        if (timeout != null && timeout > 0) {
            app.setZkSessionTimeout(timeout);
        }

        if (!app.connect()) {
            app = null;
            log.warn("connect failed.");
            return 1;
        }

        if (allInOne) {
            allInOne(app);
        } else {
            output(app);
        }

        log.info("Done!");
        app.disconnect();
        return 0;
    }

    private void allInOne(ZkXuiApp app) {
        File file = new File(outputDir, getFileName(ALL_IN_ONE, suffix));

        if (file.exists()) {
            if (forceOverwrite) {
                log.warn("force overwrite exists file:{}", file.getAbsolutePath());
            } else {
                log.warn("file exists:{}", file.getAbsolutePath());
                return;
            }
        }

        try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
            for (String path : configPaths) {
                log.info("export config path:{}", path);
                String content = app.export(path, false);
                out.println(content);
            }
            out.flush();
            log.info("save {} to {}.", Arrays.toString(configPaths), file.getAbsolutePath());
        } catch (IOException e) {
            log.error("export failed", e);
        }
    }

    private void output(ZkXuiApp app) {
        for (String path : configPaths) {
            String filename = getFileName(path, suffix);
            File file = new File(outputDir, filename);
            log.info("export config path:{}, file:{}", path, file.getAbsolutePath());

            if (file.exists()) {
                if (forceOverwrite) {
                    log.warn("force overwrite exists file:{}", file.getAbsolutePath());
                } else {
                    log.warn("file exists:{}", file.getAbsolutePath());
                    continue;
                }
            }

            String content = app.export(path, false);

            try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
                out.print(content);
                out.flush();
                log.info("save {} to {}.", path, file.getAbsolutePath());
            } catch (IOException e) {
                log.error("export failed, path:{}", path, e);
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
