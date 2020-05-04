package net.jmecn.zkxui.tui.dialog;

import jcurses.widgets.Button;
import jcurses.widgets.Dialog;
import jcurses.widgets.GridLayoutManager;
import jcurses.widgets.Label;
import jcurses.widgets.TextField;
import jcurses.widgets.WidgetsConstants;

/**
 * @title ZkUpdatePropertyDialog
 * @author yanmaoyuan
 * @date 2020年5月4日
 * @version 1.0
 */
public class ZkUpdatePropertyDialog extends Dialog implements WidgetsConstants {

    private String oldValue;

    private String result;

    private boolean modified = false;

    private TextField valueField = null;

    private Button okButton = null;

    private Button cancelButton = null;

    public ZkUpdatePropertyDialog(String path, String name, String oldValue) {
        super(64, 7, true, "Update Property");

        this.oldValue = oldValue;

        valueField = new TextField();
        valueField.setText(oldValue);

        okButton = new Button("OK");
        okButton.addListener((e) -> {
            ok();
        });
        cancelButton = new Button("Cancel");
        cancelButton.addListener((e) -> {
            cancel();
        });

        GridLayoutManager gridLayout = new GridLayoutManager(5, 5);
        getRootPanel().setLayoutManager(gridLayout);
        gridLayout.addWidget(new Label(path), 0, 0, 5, 1, ALIGNMENT_CENTER, ALIGNMENT_LEFT);
        gridLayout.addWidget(new Label("Name: "), 0, 1, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_RIGHT);
        gridLayout.addWidget(new Label(name), 1, 1, 4, 1, ALIGNMENT_CENTER, ALIGNMENT_LEFT);
        gridLayout.addWidget(new Label("Value: "), 0, 2, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_RIGHT);
        gridLayout.addWidget(valueField, 1, 2, 4, 2, ALIGNMENT_CENTER, ALIGNMENT_LEFT);
        gridLayout.addWidget(okButton, 1, 4, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
        gridLayout.addWidget(cancelButton, 3, 4, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
    }

    public String getResult() {
        return result;
    }

    public boolean isModified() {
        return modified;
    }

    private void ok() {
        result = valueField.getText();
        if (!oldValue.equals(result)) {
            modified = true;
        }

        close();
    }

    private void cancel() {
        oldValue = null;
        modified = false;
        close();
    }
}