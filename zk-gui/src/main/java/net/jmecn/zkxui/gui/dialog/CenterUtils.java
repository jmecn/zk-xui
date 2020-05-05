package net.jmecn.zkxui.gui.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

public class CenterUtils {

    public static void center(Component frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int x = (screenSize.width - width) / 2;
        int y = (screenSize.height - height) / 2;
        frame.setLocation(x, y);
    }

    public static void center(Component parent, Component frame) {
        if (parent == null) {
            center(frame);
            return;
        }

        int width = frame.getWidth();
        int height = frame.getHeight();
        Dimension screenSize = parent.getSize();
        Point loc = parent.getLocationOnScreen();

        int x = loc.x + (screenSize.width - width) / 2;
        int y = loc.y + (screenSize.height - height) / 2;
        frame.setLocation(x, y);
    }
}
