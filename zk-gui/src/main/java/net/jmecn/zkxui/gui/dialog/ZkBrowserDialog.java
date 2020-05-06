package net.jmecn.zkxui.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;

import lombok.extern.slf4j.Slf4j;
import net.jmecn.zkxui.client.ZkXuiApp;
import net.jmecn.zkxui.client.utils.StringUtils;
import net.jmecn.zkxui.client.vo.LeafBean;
import net.jmecn.zkxui.client.vo.ZkNode;
import net.jmecn.zkxui.gui.model.ZkNodeListModel;
import net.jmecn.zkxui.gui.model.ZkPropertyTableModel;
import net.jmecn.zkxui.gui.task.ProgressDialog;
import net.jmecn.zkxui.gui.task.ProgressTask;

@Slf4j
public class ZkBrowserDialog extends JFrame {

    private static final long serialVersionUID = 1L;

    private final static String TITLE = "ZooKeeper Browser";

    private final static String NOT_CONNECTED = "NOT CONNECTED";

    private ZkXuiApp app;

    private ZkNode zkNode;

    private JButton connectButton;

    private JButton importFileButton;

    private JButton importEditorButton;

    private JButton exportFileButton;

    private JButton exportEditorButton;

    private JButton addNodeButton;

    private JButton addPropertyButton;

    private JTextField pathField;

    private ZkNodeListModel listModel;

    private ZkPropertyTableModel tableModel;

    private JFileChooser chooser;

    public ZkBrowserDialog() {
        this.setTitle(TITLE);
        this.setSize(1280, 720);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        CenterUtils.center(this);

        chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        JSplitPane centerPanel = new JSplitPane();
        centerPanel.setOneTouchExpandable(true);// 让分隔线显示出箭头
        centerPanel.setContinuousLayout(true);// 操作箭头，重绘图形
        centerPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);// 设置分割线方向
        centerPanel.setDividerLocation(256);
        centerPanel.setDividerSize(2);
        centerPanel.setLeftComponent(getNodeList());
        centerPanel.setRightComponent(getPropertyTable());

        getContentPane().add(getPathPanel(), BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);

        this.setJMenuBar(getMainMenu());

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (app != null) {
                    app.disconnect();
                    app = null;
                }
            }
        });
    }

    /**
     * 主菜单
     * 
     * @return
     */
    private JMenuBar getMainMenu() {
        JMenuBar mainMenu = new JMenuBar();

        connectButton = new JButton("Connect");
        connectButton.addActionListener((e) -> {
            if (app == null) {
                connect();
            } else {
                disconnect();
            }
        });

        importFileButton = new JButton("Import File");
        importFileButton.addActionListener((e) -> {
            importFromFile();
        });

        importEditorButton = new JButton("Import Editor");
        importEditorButton.addActionListener((e) -> {
            importFromEditor();
        });

        exportFileButton = new JButton("Export to File");
        exportFileButton.addActionListener((e) -> {
            exportToFile();
        });

        exportEditorButton = new JButton("Export to Editor");
        exportEditorButton.addActionListener((e) -> {
            exportToEditor();
        });

        addNodeButton = new JButton("Add Node");
        addNodeButton.addActionListener((e) -> {
            addNode();
        });

        addPropertyButton = new JButton("Add Property");
        addPropertyButton.addActionListener((e) -> {
            addProperty();
        });

        setButtonEnabled(false);

        mainMenu.add(connectButton);
        mainMenu.add(importFileButton);
        mainMenu.add(importEditorButton);
        mainMenu.add(exportFileButton);
        mainMenu.add(exportEditorButton);
        mainMenu.add(addNodeButton);
        mainMenu.add(addPropertyButton);

        return mainMenu;
    }

    private void setButtonEnabled(boolean enabled) {
        importFileButton.setEnabled(enabled);
        importEditorButton.setEnabled(enabled);
        exportFileButton.setEnabled(enabled);
        exportEditorButton.setEnabled(enabled);
        addNodeButton.setEnabled(enabled);
        addPropertyButton.setEnabled(enabled);
    }

    private JPanel getPathPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        JLabel pathLabel = new JLabel("Current Path: ");
        panel.add(pathLabel, BorderLayout.WEST);

        pathField = new JTextField(NOT_CONNECTED);
        pathField.setEditable(false);
        pathField.setBackground(Color.WHITE);
        panel.add(pathField, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane getNodeList() {
        listModel = new ZkNodeListModel();

        JList<String> list = new JList<>(listModel);
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(list);

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (SwingUtilities.isRightMouseButton(evt)) {
                    // TODO popup menu
                } else if (SwingUtilities.isLeftMouseButton(evt)) {
                    if (evt.getClickCount() == 2) {
                        int index = list.locationToIndex(evt.getPoint());
                        String node = listModel.getElementAt(index);
                        forward(index, node);
                    }
                }
            }
        });

        list.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int index = list.getSelectedIndex();
                    String node = listModel.getElementAt(index);
                    forward(index, node);
                    return;
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    int index = list.getSelectedIndex();
                    String node = listModel.getElementAt(index);
                    deleteNode(index, node);
                    return;
                } else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    addNode();
                    return;
                }
            }
        });
        return scroll;
    }

    private JScrollPane getPropertyTable() {
        tableModel = new ZkPropertyTableModel();
        JTable table = new JTable(tableModel);
        table.setDragEnabled(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.getColumnModel().getColumn(0).setMinWidth(240);
        tableHeader.getColumnModel().getColumn(0).setMaxWidth(240);

        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(table);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (SwingUtilities.isRightMouseButton(evt)) {
                    // TODO popup menu
                } else if (SwingUtilities.isLeftMouseButton(evt)) {
                    if (evt.getClickCount() == 2) {
                        int row = table.rowAtPoint(evt.getPoint());
                        updateProperty(row);
                    }
                }
            }
        });

        table.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int index = table.getSelectedRow();
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    updateProperty(index);
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteProperty(index);
                } else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    addProperty();
                }
            }
        });
        return scroll;
    }

    private void connect() {
        ZkServerDialog dialog = new ZkServerDialog(this);
        dialog.setVisible(true);

        ZkXuiApp app = dialog.getResult();
        if (app == null) {
            return;
        }

        this.app = app;
        setButtonEnabled(true);
        connectButton.setText("Disconnect");

        refresh();
        repaint();
    }

    private void disconnect() {
        if (app != null) {
            app.disconnect();
            app = null;
        }
        connectButton.setText("Connect");
        setButtonEnabled(false);
        reset();
    }

    private void reset() {
        pathField.setText(NOT_CONNECTED);
        tableModel.setZkNode(null);
        listModel.setZkNode(true, null);
    }

    private void refresh() {
        if (app == null) {
            return;
        }

        ProgressTask<ZkNode> task = new ProgressTask<ZkNode>() {
            @Override
            public ZkNode call() throws Exception {
                return app.list();
            }
        };

        ProgressDialog<ZkNode> dialog = new ProgressDialog<>("Fetch: " + app.getCurrentPath(), task);
        dialog.setVisible(true);

        ZkNode zkNode = dialog.getResult();
        if (zkNode == null) {
            return;
        }

        this.zkNode = zkNode;

        pathField.setText(app.getCurrentPath());
        listModel.setZkNode(app.isRootNode(), zkNode);
        tableModel.setZkNode(zkNode);
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

    private void deleteNode(int idx, String node) {
        if (!app.isRootNode() && idx == 0) {
            return;
        }

        int result = JOptionPane.showConfirmDialog(this, //
            "Are you sure to delete node[" + node + "] under the node:\n" + app.getCurrentPath(), //
            "Delete ZooKeeper Node", //
            JOptionPane.YES_NO_OPTION);//

        if (JOptionPane.YES_OPTION != result) {
            return;
        }

        app.deleteNode(Collections.singletonList(node));

        refresh();
        repaint();
    }

    private void deleteProperty(int idx) {
        if (idx < 0 || idx >= zkNode.getLeafBeanLSt().size()) {
            return;
        }

        LeafBean leaf = zkNode.getLeafBeanLSt().get(idx);
        int result = JOptionPane.showConfirmDialog(this, //
            "Are you sure to delete property[" + leaf.getName() + "] under the node:\n" + app.getCurrentPath(), //
            "Delete ZooKeeper Property", //
            JOptionPane.YES_NO_OPTION);//

        if (JOptionPane.YES_OPTION != result) {
            return;
        }

        app.deleteProperties(Collections.singletonList(leaf.getName()));

        refresh();
        repaint();
    }

    private void importFromFile() {
        if (JFileChooser.APPROVE_OPTION != chooser.showOpenDialog(this)) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file == null || file.isDirectory() || !file.exists()) {
            return;
        }

        //
        int result = JOptionPane.showConfirmDialog(this, //
            "Overwrite exists properties?\nOnly properties that not exists will be import if you choose 'No'.",
            "Overwrite", JOptionPane.YES_NO_CANCEL_OPTION);
        if (JOptionPane.CANCEL_OPTION == result) {
            return;
        }

        Boolean overwrite = JOptionPane.YES_OPTION == result;

        try (FileInputStream in = new FileInputStream(file)) {
            app.importData(in, overwrite);
            in.close();

            JOptionPane.showMessageDialog(this, "Import data success.");

            refresh();
            repaint();
        } catch (IOException e) {
            log.error("Import failed.", e);
            JOptionPane.showMessageDialog(this, "Import data failed.");
        }
    }

    private void importFromEditor() {
        ZkImportEditor editor = new ZkImportEditor();
        editor.setVisible(true);

        if (!editor.isModified()) {
            return;
        }

        editor.getContent();
        editor.isOverwrite();

        try {
            app.importData(editor.getContent(), editor.isOverwrite());
            JOptionPane.showMessageDialog(this, "Import data success.");

            refresh();
            repaint();
        } catch (Exception e) {
            log.error("Import failed.", e);
            JOptionPane.showMessageDialog(this, "Import data failed.");
        }
    }

    private void exportToFile() {
        if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this,
            "Export this node below:\n" + app.getCurrentPath(), "Export ZooKeeper Data", JOptionPane.YES_NO_OPTION)) {
            return;
        }

        if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(this)) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (file == null || file.isDirectory()) {
            return;
        }

        if (file.exists()) {
            if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this,
                "Are you sure to overwrite the existing file?\n" + file.getAbsolutePath(), "Export ZooKeeper Data",
                JOptionPane.YES_NO_OPTION)) {
                return;
            }
        }

        String content = null;
        try {
            content = app.export();
        } catch (Exception e) {
            log.error("Failed export " + app.getCurrentPath(), e);
            JOptionPane.showMessageDialog(this, "Failed export " + app.getCurrentPath());
            return;
        }

        if (StringUtils.isBlank(content)) {
            JOptionPane.showMessageDialog(this, "Failed export " + app.getCurrentPath());
            return;
        }

        try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
            out.print(content);
            out.flush();
            out.close();
            JOptionPane.showMessageDialog(this, "Exported to " + file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed export");
            JOptionPane.showMessageDialog(this, "Failed export " + app.getCurrentPath());
        }
    }

    private void exportToEditor() {
        if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this,
            "Export this node below:\n" + app.getCurrentPath(), "Export ZooKeeper Data", JOptionPane.YES_NO_OPTION)) {
            return;
        }

        String content = null;
        try {
            content = app.export();
        } catch (Exception e) {
            log.error("Failed export " + app.getCurrentPath(), e);
            JOptionPane.showMessageDialog(this, "Failed export " + app.getCurrentPath());
            return;
        }

        if (StringUtils.isBlank(content)) {
            JOptionPane.showMessageDialog(this, "Failed export " + app.getCurrentPath());
            return;
        }

        ZkExportEditor editor = new ZkExportEditor(content);
        editor.setVisible(true);
    }

    private void addNode() {
        ZkNodeDialog dialog = new ZkNodeDialog(app.getCurrentPath(), zkNode);
        dialog.setVisible(true);

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
        dialog.setVisible(true);

        LeafBean result = dialog.getResult();
        if (result == null || !dialog.isModified()) {
            return;
        }

        app.addProperty(result.getName(), result.getStrValue());

        refresh();
        repaint();
    }

    private void updateProperty(int idx) {
        log.debug("update property with id={}", idx);
        if (idx < 0 || idx >= zkNode.getLeafBeanLSt().size()) {
            return;
        }

        LeafBean leaf = zkNode.getLeafBeanLSt().get(idx);
        ZkUpdatePropertyDialog dialog =
            new ZkUpdatePropertyDialog(app.getCurrentPath(), leaf.getName(), leaf.getStrValue());
        dialog.setVisible(true);

        String result = dialog.getResult();
        if (result == null || !dialog.isModified()) {
            return;
        }

        app.updateProperty(leaf.getName(), result);

        refresh();
        repaint();
    }
}
