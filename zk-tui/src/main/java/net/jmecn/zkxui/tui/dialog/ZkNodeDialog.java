package net.jmecn.zkxui.tui.dialog;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jcurses.util.Message;
import jcurses.widgets.Button;
import jcurses.widgets.Dialog;
import jcurses.widgets.GridLayoutManager;
import jcurses.widgets.Label;
import jcurses.widgets.TextField;
import jcurses.widgets.WidgetsConstants;
import net.jmecn.zkxui.client.utils.StringUtils;
import net.jmecn.zkxui.client.vo.ZkNode;

/**
 * @title ZkNodeDialog
 * @author yanmaoyuan
 * @date 2020年5月4日
 * @version 1.0
 */
public class ZkNodeDialog extends Dialog implements WidgetsConstants {

    private Set<String> existsNodes;

    private String oldValue;

    private String result;

    private boolean modified = false;

    private Label nameLabel = null;

    private TextField nameField = null;

    private Button okButton = null;

    private Button cancelButton = null;

    public ZkNodeDialog(String path, ZkNode zkNode) {
        this(path, zkNode, null);
    }

    public ZkNodeDialog(String path, ZkNode zkNode, String oldValue) {
        super(64, 6, true, "Add Node");

        this.existsNodes = new HashSet<>();
        if (zkNode.getNodeLst() != null && zkNode.getNodeLst().size() > 0) {
            existsNodes.addAll(zkNode.getNodeLst());
        }
        if (zkNode.getLeafBeanLSt() != null && zkNode.getLeafBeanLSt().size() > 0) {
            existsNodes.addAll(zkNode.getLeafBeanLSt().stream().map(it -> it.getName()).collect(Collectors.toList()));
        }

        Label pathLabel = new Label(path);
        nameLabel = new Label("Name: ");
        nameField = new TextField();

        if (oldValue != null) {
            this.oldValue = oldValue;
            if (StringUtils.isNotBlank(oldValue)) {
                nameField.setText(StringUtils.trim(oldValue));
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

        GridLayoutManager gridLayout = new GridLayoutManager(5, 4);
        getRootPanel().setLayoutManager(gridLayout);
        gridLayout.addWidget(pathLabel, 0, 0, 5, 1, ALIGNMENT_CENTER, ALIGNMENT_LEFT);
        gridLayout.addWidget(nameLabel, 0, 1, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_RIGHT);
        gridLayout.addWidget(nameField, 1, 1, 4, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
        gridLayout.addWidget(okButton, 1, 3, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
        gridLayout.addWidget(cancelButton, 3, 3, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
    }

    public String getResult() {
        return result;
    }

    public boolean isModified() {
        return modified;
    }

    private void ok() {
        String name = nameField.getText();

        if (StringUtils.isBlank(name)) {
            new Message("WARN", "name can not be empty", "OK").show();
            return;
        }

        // check exists
        if (existsNodes.contains(name)) {
            new Message("WARN", "name [" + name + "] already exists", "OK").show();
            return;
        }

        result = name;

        if (oldValue != null) {
            if (!name.equals(oldValue)) {
                modified = true;
            }
        } else {
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