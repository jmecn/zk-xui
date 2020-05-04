package net.jmecn.zkxui.tui.dialog;

import java.util.StringTokenizer;

import jcurses.widgets.Button;
import jcurses.widgets.Dialog;
import jcurses.widgets.GridLayoutManager;
import jcurses.widgets.Label;
import jcurses.widgets.WidgetsConstants;

/**
 * @title ConfirmDialog
 * @author yanmaoyuan
 * @date 2020年5月4日
 * @version 1.0
 */
public class ConfirmDialog extends Dialog implements WidgetsConstants {

    final static int MIN_LENGTH = 64;

    public Boolean result = null;

    public Boolean getResult() {
        return result;
    }

    public ConfirmDialog(String title, String text, String yesBtn, String noBtn) {
        super(getWidth(text, title) + 4, getHeight(text) + 5, true, title);

        int h = getHeight(text) + 3;
        GridLayoutManager gridLayout = new GridLayoutManager(2, h);
        getRootPanel().setLayoutManager(gridLayout);

        Button yesButton = new Button(yesBtn);
        yesButton.addListener((e) -> {
            result = true;
            close();
        });

        Button noButton = new Button(noBtn);
        noButton.addListener((e) -> {
            result = false;
            close();
        });

        gridLayout.addWidget(new Label(text), 0, 0, 2, h - 4, ALIGNMENT_CENTER, ALIGNMENT_LEFT);
        gridLayout.addWidget(yesButton, 0, h - 2, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
        gridLayout.addWidget(noButton, 1, h - 2, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
    }

    private static int getWidth(String label, String title) {
        StringTokenizer tokenizer = new StringTokenizer(label, "\n");
        int result = MIN_LENGTH;
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            if (result < token.length()) {
                result = token.length();
            }
        }
        if (title.length() > result) {
            result = title.length();
        }

        // message must fit on the screen
        if (result > jcurses.system.Toolkit.getScreenWidth() - 3) {
            result = jcurses.system.Toolkit.getScreenWidth() - 3;
        }

        return result;
    }

    private static int getHeight(String label) {
        StringTokenizer tokenizer = new StringTokenizer(label, "\n");
        int result = 0;
        while (tokenizer.hasMoreElements()) {
            tokenizer.nextElement();
            result++;
        }
        return result;
    }
}