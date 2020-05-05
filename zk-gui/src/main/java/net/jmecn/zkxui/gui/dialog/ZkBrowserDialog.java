package net.jmecn.zkxui.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import javax.swing.table.JTableHeader;

import lombok.extern.slf4j.Slf4j;
import net.jmecn.zkxui.client.ZkXuiApp;
import net.jmecn.zkxui.client.vo.ZkNode;
import net.jmecn.zkxui.gui.model.ZkNodeListModel;
import net.jmecn.zkxui.gui.model.ZkPropertyTableModel;

@Slf4j
public class ZkBrowserDialog extends JFrame {

	private static final long serialVersionUID = 1L;

	private final static String TITLE = "ZooKeeper Browser";

	private ZkXuiApp app;

	private ZkNode zkNode;

	private JButton connectButton;

	private JButton importButton;

	private JButton exportButton;

	private JButton addNodeButton;

	private JButton addPropertyButton;

	private JTextField pathField;

	private ZkNodeListModel listModel;

	private ZkPropertyTableModel tableModel;

	public ZkBrowserDialog() {
		this.setTitle(TITLE);
		this.setSize(1280, 720);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

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

		importButton = new JButton("Import");
		exportButton = new JButton("Export");
		addNodeButton = new JButton("Add Node");
		addPropertyButton = new JButton("Add Property");

		setButtonEnabled(false);

		mainMenu.add(connectButton);
		mainMenu.add(importButton);
		mainMenu.add(exportButton);
		mainMenu.add(addNodeButton);
		mainMenu.add(addPropertyButton);

		return mainMenu;
	}

	private void setButtonEnabled(boolean enabled) {
		importButton.setEnabled(enabled);
		exportButton.setEnabled(enabled);
		addNodeButton.setEnabled(enabled);
		addPropertyButton.setEnabled(enabled);
	}

	private JPanel getPathPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		JLabel pathLabel = new JLabel("Current Path: ");
		panel.add(pathLabel, BorderLayout.WEST);

		pathField = new JTextField("/");
		pathField.setEditable(false);
		pathField.setBackground(Color.WHITE);
		panel.add(pathField, BorderLayout.CENTER);

		return panel;
	}

	private JScrollPane getNodeList() {
		listModel = new ZkNodeListModel();

		JList<String> nodeList = new JList<>(listModel);
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(nodeList);
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

		return scroll;
	}

	private void connect() {
		ZkServerDialog dialog = new ZkServerDialog(this);
		dialog.setVisible(true);

		ZkXuiApp app = dialog.getResult();
		System.out.println(app);
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
	    pathField.setText("/");
	    tableModel.setZkNode(null);
	    listModel.setZkNode(true, null);
	}

    private void refresh() {
    	if (app == null) {
    		return;
    	}
        ZkNode zkNode = app.list();
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
}
