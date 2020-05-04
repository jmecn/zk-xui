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
import net.jmecn.zkxui.client.vo.LeafBean;
import net.jmecn.zkxui.client.vo.ZkNode;

/**
 * @title ZkAddPropertyDialog
 * @author yanmaoyuan
 * @date 2020年5月4日
 * @version 1.0
 */
public class ZkAddPropertyDialog extends Dialog implements WidgetsConstants {

    private Set<String> existsNodes;

    private LeafBean result;

    private boolean modified = false;

    private TextField nameField = null;

    private TextField valueField = null;

    private Button okButton = null;

    private Button cancelButton = null;

    public ZkAddPropertyDialog(String path, ZkNode zkNode) {
        super(64, 7, true, "Add Property");

        this.existsNodes = new HashSet<>();
        if (zkNode.getNodeLst() != null && zkNode.getNodeLst().size() > 0) {
            existsNodes.addAll(zkNode.getNodeLst());
        }
        if (zkNode.getLeafBeanLSt() != null && zkNode.getLeafBeanLSt().size() > 0) {
            existsNodes.addAll(zkNode.getLeafBeanLSt().stream().map(it -> it.getName()).collect(Collectors.toList()));
        }

        nameField = new TextField();
        valueField = new TextField();

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
        gridLayout.addWidget(nameField, 1, 1, 4, 1, ALIGNMENT_CENTER, ALIGNMENT_LEFT);
        gridLayout.addWidget(new Label("Value: "), 0, 2, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_RIGHT);
        gridLayout.addWidget(valueField, 1, 2, 4, 2, ALIGNMENT_CENTER, ALIGNMENT_LEFT);
        gridLayout.addWidget(okButton, 1, 4, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
        gridLayout.addWidget(cancelButton, 3, 4, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
    }

    public LeafBean getResult() {
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

        // check exists
        if (existsNodes.contains(name)) {
            new Message("WARN", "name [" + name + "] already exists", "OK").show();
            return;
        }

        if (StringUtils.isEmpty(value)) {
            result = new LeafBean(null, name, "".getBytes());
        } else {
            result = new LeafBean(null, name, value.getBytes());
        }

        modified = true;

        close();
    }

    private void cancel() {
        modified = false;
        close();
    }
}