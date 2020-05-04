package net.jmecn.zkxui.tui.dialog;

import jcurses.util.Message;
import jcurses.widgets.Button;
import jcurses.widgets.Dialog;
import jcurses.widgets.GridLayoutManager;
import jcurses.widgets.Label;
import jcurses.widgets.TextField;
import jcurses.widgets.WidgetsConstants;
import net.jmecn.zkxui.client.utils.StringUtils;
import net.jmecn.zkxui.client.vo.Env;

/**
 * 用于输入或显示Zk环境配置参数
 * 
 * @title ZkEnvDialog
 * @author yanmaoyuan
 * @date 2020年5月3日
 * @version 1.0
 */
public class ZkEnvDialog extends Dialog implements WidgetsConstants {

    private Env oldEnv;

    private Env result;

    private boolean modified = false;

    private Label nameLabel = null;

    private Label valueLabel = null;

    private TextField nameField = null;

    private TextField valueField = null;

    private Button okButton = null;

    private Button cancelButton = null;

    /**
     */
    public ZkEnvDialog() {
        this(null);
    }

    /**
     */
    public ZkEnvDialog(Env env) {
        super(60, 7, true, "ZooKeeper Environment");

        nameLabel = new Label("Name: ");
        valueLabel = new Label("ZkServers: ");

        nameField = new TextField();
        valueField = new TextField();

        if (env != null) {
            this.oldEnv = env;
            if (StringUtils.isNotBlank(env.getName())) {
                nameField.setText(StringUtils.trim(env.getName()));
            }
            if (StringUtils.isNotBlank(env.getZkServers())) {
                valueField.setText(StringUtils.trim(env.getZkServers()));
            }
        }

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
        gridLayout.addWidget(nameLabel, 0, 1, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_RIGHT);
        gridLayout.addWidget(nameField, 1, 1, 4, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
        gridLayout.addWidget(valueLabel, 0, 2, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_RIGHT);
        gridLayout.addWidget(valueField, 1, 2, 4, 2, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
        gridLayout.addWidget(okButton, 1, 4, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
        gridLayout.addWidget(cancelButton, 3, 4, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
    }

    public Env getResult() {
        return result;
    }

    public boolean isModified() {
        return modified;
    }

    private void ok() {
        String name = nameField.getText();
        String value = valueField.getText();

        if (StringUtils.isBlank(name)) {
            new Message("WARN", "name can not be empty", "OK").show();
            return;
        }

        if (StringUtils.isBlank(value)) {
            new Message("WARN", "ZkServers can not be empty", "OK").show();
            return;
        }

        result = new Env(name, value);
        result.setName(name);
        result.setZkServers(value);

        if (oldEnv != null) {
            if (!name.equals(oldEnv.getName()) || !value.equals(oldEnv.getZkServers())) {
                modified = true;
            }
        } else {
            modified = true;
        }

        close();
    }

    private void cancel() {
        oldEnv = null;
        modified = false;
        close();
    }
}
