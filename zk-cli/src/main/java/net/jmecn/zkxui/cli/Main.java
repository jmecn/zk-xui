package net.jmecn.zkxui.cli;

import lombok.extern.slf4j.Slf4j;
import net.jmecn.zkxui.gui.ZkGuiMain;
import net.jmecn.zkxui.tui.ZkTuiMain;

@Slf4j
public class Main {

    public static void main(String[] args) {
        log.info("ZK-XUI started.");
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            ZkTuiMain.main(args);
        } else {
            ZkGuiMain.main(args);
        }
    }

}
