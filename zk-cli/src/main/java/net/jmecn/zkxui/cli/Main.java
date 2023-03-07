package net.jmecn.zkxui.cli;

import lombok.extern.slf4j.Slf4j;
import net.jmecn.zkxui.gui.ZkGuiMain;
import net.jmecn.zkxui.tui.ZkTuiMain;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Slf4j
@Command(name = "zkxui", mixinStandardHelpOptions = true, version = "1.1.0",
        description = "Toolkit for process zookeeper configurations.")
public class Main {

    public static void main(String[] args) {
        // 命令行模式
        if (args == null || args.length == 0) {
            log.info("ZK-XUI started.");
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                ZkTuiMain.main(args);
            } else {
                ZkGuiMain.main(args);
            }
        } else {
            // 命令行模式
            cmd(args);
        }
    }

    private static void cmd(String[] args) {
        CommandLine commandLine = new CommandLine(new Main())
                .addSubcommand("export", new Export())
                .addSubcommand("help", new Help());
        commandLine.setExecutionStrategy(new CommandLine.RunLast());
        commandLine.execute(args);
    }
}
