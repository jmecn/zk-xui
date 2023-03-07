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
import java.util.concurrent.Callable;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/3/7
 */
@Slf4j
@Command(name = "export", mixinStandardHelpOptions = true, version = "export 1.0.0",
        description = "Export configurations from zookeeper.")
public class Export implements Callable<Integer> {

    @Option(names = {"-s", "--server"}, required = true, description = "zookeeper bootstrap server address.")
    private String server;

    @Option(names = {"-f", "--force-overwrite"}, description = "force overwrite the existing files.")
    private boolean forceOverwrite;

    @Option(names = {"-t", "--session-timeout"}, description = "zookeeper connection session timeout(in millis).")
    private Integer timeout;

    @Option(names = { "-o", "--output-dir" }, required = true, paramLabel = "OUTPUT", description = "the output directory")
    private File outputDir;

    @Option(names = {"-n", "--suffix-name"}, defaultValue = "properties", description = "suffix name for export files(eg: .properties)")
    private String suffix;

    @Parameters(paramLabel = "CONFIG_PATH", description = "one or more config path to export")
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

        log.info("success, disconnect zookeeper");
        app.disconnect();
        return 0;
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
