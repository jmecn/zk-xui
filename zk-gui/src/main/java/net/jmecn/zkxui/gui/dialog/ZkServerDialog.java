package net.jmecn.zkxui.gui.dialog;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import lombok.extern.slf4j.Slf4j;
import net.jmecn.zkxui.client.ZkServerList;
import net.jmecn.zkxui.client.ZkXuiApp;
import net.jmecn.zkxui.client.vo.Env;

@Slf4j
public class ZkServerDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private Env result;

	private JList<Env> list;

	public ZkServerDialog(ZkBrowserDialog parent) {
		super(parent);
		this.setTitle("ZooKeeper Server List");
		this.setSize(600, 400);
		this.setModal(true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		this.setLocation(parent.getLocationOnScreen().x + 100, parent.getLocationOnScreen().y + 50);
		getContentPane().add(getEnvList(), BorderLayout.CENTER);
		getContentPane().add(getToolBox(), BorderLayout.SOUTH);
	}

	private JScrollPane getEnvList() {
		list = new JList<>();
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(list);

		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					int index = list.locationToIndex(evt.getPoint());
					ok(index);
				}
			}
		});

		List<Env> envList = ZkServerList.INSTANCE.list();
		list.setListData(envList.toArray(new Env[0]));

		list.setSelectedIndex(0);
		return scroll;
	}

	private JMenuBar getToolBox() {
		JMenuBar bar = new JMenuBar();

		JButton add = new JButton("Add");
		add.addActionListener((e) -> {
			add();
		});

		JButton connect = new JButton("Connect");
		connect.addActionListener((e) -> {
			ok(list.getSelectedIndex());
		});

		JButton edit = new JButton("Edit");
		add.addActionListener((e) -> {
			edit();
		});

		JButton delete = new JButton("Delete");
		add.addActionListener((e) -> {
			del();
		});

		bar.add(add);
		bar.add(connect);
		bar.add(edit);
		bar.add(delete);
		return bar;
	}

	private void loadList() {
		List<Env> envList = ZkServerList.INSTANCE.list();
		list.setListData(envList.toArray(new Env[0]));
	}

	private void edit() {
		int idx = list.getSelectedIndex();
		List<Env> envlist = ZkServerList.INSTANCE.list();
		if (envlist.size() <= 0 || idx < 0 || idx >= envlist.size()) {
			// out of index
			return;
		}

		Env old = envlist.get(idx);
		ZkEnvDialog dialog = new ZkEnvDialog(old);
		dialog.setVisible(true);

		Env result = dialog.getResult();
		if (result != null && dialog.isModified()) {
			old.setName(result.getName());
			old.setZkServers(result.getZkServers());
			ZkServerList.INSTANCE.save();
			repaint();
		}
	}

	private void ok(int idx) {
		List<Env> envlist = ZkServerList.INSTANCE.list();
		if (envlist.size() <= 0 || idx < 0 || idx >= envlist.size()) {
			// out of index
			return;
		}

		result = envlist.get(idx);
		dispose();
	}

	private void add() {
		List<Env> envlist = ZkServerList.INSTANCE.list();

		ZkEnvDialog dialog = new ZkEnvDialog();
		dialog.setVisible(true);

		Env result = dialog.getResult();
		if (result != null && dialog.isModified()) {
			envlist.add(result);
			ZkServerList.INSTANCE.save();

			loadList();
			list.setSelectedIndex(envlist.size() - 1);// track the last
			this.repaint();
			log.info("Add: {}", result);
		}

	}

	private void del() {
		List<Env> envlist = ZkServerList.INSTANCE.list();
		int idx = list.getSelectedIndex();

		if (envlist.size() <= 0 || idx < 0 || idx >= envlist.size()) {
			// out of index
			return;
		}

		Env env = envlist.get(idx);

		int result = JOptionPane.showConfirmDialog(this, //
				"Are you sure to delete the selected ZooKeeper environment?\nNAME: "//
						+ env.getName() + "\nVALUE: " + env.getZkServers(), //
				"DELETE", //
				JOptionPane.YES_NO_OPTION, //
				JOptionPane.QUESTION_MESSAGE);//

		if (JOptionPane.YES_OPTION != result) {
			return;
		}

		// 移除
		envlist.remove(idx);
		ZkServerList.INSTANCE.save();

		// 刷新界面
		list.remove(idx);
		if (list.getModel().getSize() > idx) {
			list.setSelectedIndex(idx);
		} else if (idx > 0) {
			list.setSelectedIndex(idx - 1);
		} else {
			list.setSelectedIndex(0);
		}
		this.repaint();
		log.info("Delete: {}", env);
	}

	public ZkXuiApp getResult() {
		if (result == null) {
			return null;
		}

		return new ZkXuiApp(result);
	}
}