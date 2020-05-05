package net.jmecn.zkxui.gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.jmecn.zkxui.gui.dialog.ZkBrowserDialog;

public class ZkGuiMain {

    public static void main(String[] args) {
        String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            ZkBrowserDialog dialog = new ZkBrowserDialog();
            dialog.setVisible(true);
        });
    }

}
