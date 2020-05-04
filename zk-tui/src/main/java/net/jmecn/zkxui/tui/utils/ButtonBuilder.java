package net.jmecn.zkxui.tui.utils;

import jcurses.event.ActionListener;
import jcurses.widgets.Button;

/**
 * @title ButtonBuilder
 * @author yanmaoyuan
 * @date 2020年5月4日
 * @version 1.0
 */
public class ButtonBuilder {

    public static Button newButton(String text, Character shortCut, ActionListener l) {
        Button btn = new Button(text);
        if (shortCut != null) {
            btn.setShortCut(shortCut);
        }
        btn.addListener(l);
        return btn;
    }

}
