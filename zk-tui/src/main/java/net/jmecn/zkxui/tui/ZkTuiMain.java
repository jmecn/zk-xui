package net.jmecn.zkxui.tui;

import jcurses.system.Toolkit;
import lombok.extern.slf4j.Slf4j;
import net.jmecn.zkxui.tui.dialog.ZkServerDialog;

/**
 * @title ZkTuiMain
 * @author yanmaoyuan
 * @date 2020年5月1日
 * @version 1.0
 */
@Slf4j
public class ZkTuiMain {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            log.info("headless={}", java.awt.GraphicsEnvironment.isHeadless());

            Toolkit.beep();

            // Choose ZooKeeper Server and connect to it.
            ZkServerDialog dialog = new ZkServerDialog();
            dialog.show();

        } catch (Exception e) {
            log.error("Error", e);
        }
    }
}