package net.jmecn.zkxui.cli;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/3/7
 */
@Command(name = "help", mixinStandardHelpOptions = true, version = "1.0.0",
        description = "Help.")
public class Help implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
