package net.jmecn.zkxui.tui.dialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import jcurses.event.ItemEvent;
import jcurses.util.Message;
import jcurses.widgets.Dialog;
import jcurses.widgets.FileDialog;
import jcurses.widgets.GridLayoutManager;
import jcurses.widgets.JCList;
import jcurses.widgets.Label;
import jcurses.widgets.Panel;
import jcurses.widgets.PopUpMenu;
import jcurses.widgets.TextField;
import jcurses.widgets.WidgetsConstants;
import lombok.extern.slf4j.Slf4j;
import net.jmecn.zkxui.client.ZkXuiApp;
import net.jmecn.zkxui.client.utils.StringUtils;
import net.jmecn.zkxui.client.vo.LeafBean;
import net.jmecn.zkxui.client.vo.ZkNode;
import net.jmecn.zkxui.tui.utils.ButtonBuilder;

/**
 * @title ZkBrowserDialog
 * @author yanmaoyuan
 * @date 2020年5月3日
 * @version 1.0
 */
@Slf4j
public class ZkBrowserDialog extends Dialog implements WidgetsConstants {

    private final static int GRID_X = 10;

    private ZkXuiApp app;

    private ZkNode zkNode;

    private TextField pathField;

    private JCList<String> nodeList;

    private JCList<String> leaveList;

    public ZkBrowserDialog(ZkXuiApp app, int width, int height) {
        super(width, height, true, "ZooKeeper Browser / " + app.getEnv().getName());
        this.app = app;

        int gridHeight = height - 2;
        GridLayoutManager gridLayout = new GridLayoutManager(GRID_X, gridHeight);
        getRootPanel().setLayoutManager(gridLayout);

        gridLayout.addWidget(getPathPanel(), 0, 0, GRID_X, 1, ALIGNMENT_TOP, ALIGNMENT_LEFT);
        gridLayout.addWidget(getNodeList(), 0, 1, 2, gridHeight - 2, ALIGNMENT_TOP, ALIGNMENT_LEFT);
        gridLayout.addWidget(getLeaveList(), 2, 1, GRID_X - 2, gridHeight - 2, ALIGNMENT_TOP, ALIGNMENT_LEFT);
        gridLayout.addWidget(getToolBox(), 0, gridHeight - 1, GRID_X, 1, ALIGNMENT_TOP, ALIGNMENT_LEFT);

        refresh();
    }

    private JCList<String> getNodeList() {
        nodeList = new JCList<>();
        nodeList.addListener((e) -> {
            if (e.getType() == ItemEvent.CALLED) {
                if (!app.isRootNode() && e.getId() == 0) {
                    forward(e.getId(), (String) e.getItem());
                    return;
                }

                PopUpMenu popup = new PopUpMenu(-1, -1, "Node");
                popup.add("Enter Node");
                popup.add("Delete it");
                popup.show();
                if (popup.getSelectedIndex() == 0) {
                    forward(e.getId(), (String) e.getItem());
                } else if (popup.getSelectedIndex() == 1) {
                    deleteNode(e.getId(), (String) e.getItem());
                } else {
                    log.debug("nothing");
                }
            }
        });

        return nodeList;
    }

    private JCList<String> getLeaveList() {
        leaveList = new JCList<>();
        leaveList.addListener((e) -> {
            if (e.getType() == ItemEvent.CALLED) {
                if (e.getId() == 0) {
                    return;
                }

                PopUpMenu popup = new PopUpMenu(-1, -1, "Property");
                popup.add("Edit Property");
                popup.add("Delete it");
                popup.show();

                if (popup.getSelectedIndex() == 0) {
                    updateProperty(e.getId());
                } else if (popup.getSelectedIndex() == 1) {
                    deleteProperty(e.getId());
                } else {
                    log.debug("nothing");
                }
            }
        });

        return leaveList;
    }

    private Panel getPathPanel() {
        Panel panel = new Panel();

        pathField = new TextField(-1, app.getCurrentPath());
        GridLayoutManager layout = new GridLayoutManager(GRID_X, 1);
        panel.setLayoutManager(layout);

        layout.addWidget(new Label("PATH: "), 0, 0, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_RIGHT);
        layout.addWidget(pathField, 1, 0, GRID_X - 1, 1, ALIGNMENT_CENTER, ALIGNMENT_LEFT);
        return panel;
    }

    private Panel getToolBox() {
        Panel panel = new Panel();
        GridLayoutManager layout = new GridLayoutManager(4, 1);
        panel.setLayoutManager(layout);

        layout.addWidget(ButtonBuilder.newButton("[I]mport", 'i', (e) -> {
            importData();
        }), 0, 0, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
        layout.addWidget(ButtonBuilder.newButton("[E]xport", 'e', (e) -> {
            exportData();
        }), 1, 0, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
        layout.addWidget(ButtonBuilder.newButton("Add [N]ode", 'n', (e) -> {
            addNode();
        }), 2, 0, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);
        layout.addWidget(ButtonBuilder.newButton("Add [P]roperty", 'p', (e) -> {
            addProperty();
        }), 3, 0, 1, 1, ALIGNMENT_CENTER, ALIGNMENT_CENTER);

        return panel;
    }

    private void refresh() {
        ZkNode zkNode = app.list();
        if (zkNode == null) {
            return;
        }

        this.zkNode = zkNode;

        pathField.setText(app.getCurrentPath());

        refreshLeaves();
        refreshNodes();
    }

    private void refreshNodes() {
        nodeList.clear();
        if (!app.isRootNode()) {
            nodeList.add("..");
        }
        List<String> nodes = zkNode.getNodeLst();
        for (String node : nodes) {
            nodeList.add(node);
        }
    }

    private void refreshLeaves() {
        List<LeafBean> leaves = zkNode.getLeafBeanLSt();

        // 统计字段长度，用于对齐list
        int maxLen = 40;// 最少40
        for (LeafBean bean : leaves) {
            int len = bean.getName().length();
            if (maxLen < len) {
                maxLen = len;
            }
        }
        String format = "| %-" + maxLen + "s | %s";

        leaveList.clear();
        leaveList.add(String.format(format, "NAME", "VALUE"));
        for (LeafBean bean : leaves) {
            leaveList.add(String.format(format, bean.getName(), bean.getStrValue()));
        }
    }

    private void forward(int idx, String node) {
        if (!app.isRootNode() && idx == 0) {
            app.setCurrentPath(app.getParentPath());
        } else {
            if (app.isRootNode()) {
                app.setCurrentPath("/" + node);
            } else {
                app.setCurrentPath(app.getCurrentPath() + "/" + node);
            }
        }
        refresh();
        repaint();
    }

    private void updateProperty(int idx) {
        if (idx <= 0 || idx > zkNode.getLeafBeanLSt().size()) {
            return;
        }

        LeafBean leaf = zkNode.getLeafBeanLSt().get(idx - 1);
        ZkUpdatePropertyDialog dialog = new ZkUpdatePropertyDialog(app.getCurrentPath(), leaf.getName(),
                leaf.getStrValue());
        dialog.show();

        String result = dialog.getResult();
        if (result == null || !dialog.isModified()) {
            return;
        }

        app.updateProperty(leaf.getName(), result);

        refresh();
        repaint();
    }

    private void deleteNode(int idx, String node) {
        if (!app.isRootNode() && idx == 0) {
            return;
        }

        ConfirmDialog confirm = new ConfirmDialog("WARN",
                "Are you sure to delete node[" + node + "] under the node:\n" + app.getCurrentPath(), "NO", "YES");
        confirm.show();

        if (confirm.getResult() == null || confirm.getResult()) {
            return;
        }

        app.deleteNode(Collections.singletonList(node));

        refresh();
        repaint();
    }

    private void deleteProperty(int idx) {
        if (idx <= 0 || idx > zkNode.getLeafBeanLSt().size()) {
            return;
        }

        LeafBean leaf = zkNode.getLeafBeanLSt().get(idx - 1);
        ConfirmDialog confirm = new ConfirmDialog("WARN",
                "Are you sure to delete property[" + leaf.getName() + "] under the node:\n" + app.getCurrentPath(),
                "NO", "YES");
        confirm.show();

        if (confirm.getResult() == null || confirm.getResult()) {
            return;
        }

        app.deleteProperties(Collections.singletonList(leaf.getName()));

        refresh();
        repaint();
    }

    private void importData() {
        FileDialog dialog = new FileDialog("File window");
        dialog.show();
        File file = dialog.getChoosedFile();
        if (file == null || file.isDirectory() || !file.exists()) {
            return;
        }

        //
        ConfirmDialog overwriteDialog = new ConfirmDialog("INFO",
                "Overwrite exists properties?\nOnly properties that not exists will be import if you choose 'No'.",
                "Yes", "No");
        overwriteDialog.show();
        if (overwriteDialog.getResult() == null) {
            return;
        }

        Boolean overwrite = overwriteDialog.getResult();

        try (FileInputStream in = new FileInputStream(file)) {
            app.importData(in, overwrite);
            in.close();
            new Message("INFO", "Import data success.", "OK").show();

            refresh();
            repaint();
        } catch (IOException e) {
            log.error("Import failed.", e);
            new Message("WARN", "Import data failed.", "OK").show();
        }
    }

    private void exportData() {
        ConfirmDialog confirm = new ConfirmDialog("Export", "Export this node below:\n" + app.getCurrentPath(), "Yes",
                "No");
        confirm.show();
        if (confirm.getResult() == null || !confirm.getResult()) {
            return;
        }

        FileDialog dialog = new FileDialog("File window");
        dialog.show();
        File file = dialog.getChoosedFile();
        if (file == null || file.isDirectory()) {
            return;
        }

        if (file.exists()) {
            ConfirmDialog overwriteConfirm = new ConfirmDialog("WARN",
                    "Are you sure to overwrite the existing file?\n" + file.getAbsolutePath(), "YES", "NO");
            overwriteConfirm.show();

            if (overwriteConfirm.getResult() == null || !overwriteConfirm.getResult()) {
                log.debug("User do not want to overwrite the existing file:{}", file.getAbsoluteFile());
                return;
            }
        }

        String content = null;
        try {
            content = app.export();
        } catch (Exception e) {
            log.error("Failed export " + app.getCurrentPath(), e);
            new Message("WARN", "Failed export " + app.getCurrentPath(), "OK").show();
            return;
        }

        if (StringUtils.isBlank(content)) {
            new Message("WARN", "Failed export " + app.getCurrentPath(), "OK").show();
            return;
        }

        try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
            out.print(content);
            out.flush();
            out.close();
            new Message("INFO", "Exported to " + file.getAbsolutePath(), "OK").show();
        } catch (IOException e) {
            log.error("Failed export");
            new Message("WARN", "Failed export " + app.getCurrentPath(), "OK").show();
        }
    }

    private void addNode() {
        ZkNodeDialog dialog = new ZkNodeDialog(app.getCurrentPath(), zkNode);
        dialog.show();

        String result = dialog.getResult();
        if (result == null || !dialog.isModified()) {
            return;
        }

        app.addNode(result);

        refresh();
        repaint();
    }

    private void addProperty() {
        ZkAddPropertyDialog dialog = new ZkAddPropertyDialog(app.getCurrentPath(), zkNode);
        dialog.show();

        LeafBean result = dialog.getResult();
        if (result == null || !dialog.isModified()) {
            return;
        }

        app.addProperty(result.getName(), result.getStrValue());

        refresh();
        repaint();
    }

}