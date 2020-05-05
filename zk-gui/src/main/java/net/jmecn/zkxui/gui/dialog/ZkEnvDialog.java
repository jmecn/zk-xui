package net.jmecn.zkxui.gui.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.jmecn.zkxui.client.utils.StringUtils;
import net.jmecn.zkxui.client.vo.Env;

public class ZkEnvDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private Env oldEnv;

	private Env result;

	private boolean modified = false;

	private JTextField nameField;

	private JTextArea valueField;

	public ZkEnvDialog() {
		this(null);
	}

	public ZkEnvDialog(Env env) {
		this.setTitle("ZooKeeper Environment");
		this.setModal(true);
		this.setSize(640, 240);
		this.setResizable(false);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		CenterUtils.center(this);

		JLabel nameLabel = new JLabel("Name: ");
		JLabel valueLabel = new JLabel("Value: ");
		nameField = new JTextField();
		valueField = new JTextArea();
		valueField.setLineWrap(true);// 自动换行
		valueField.setWrapStyleWord(true);
		JScrollPane valueScroll = new JScrollPane();
		valueScroll.setViewportView(valueField);

		if (env != null) {
			this.oldEnv = env;
			if (StringUtils.isNotBlank(env.getName())) {
				nameField.setText(StringUtils.trim(env.getName()));
			}
			if (StringUtils.isNotBlank(env.getZkServers())) {
				valueField.setText(StringUtils.trim(env.getZkServers()));
			}
		}

		JButton okButton = new JButton("OK");
		okButton.addActionListener((e) -> {
			ok();
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((e) -> {
			cancel();
		});

		JPanel panel = new JPanel();
		this.setContentPane(panel);

		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);

		GridBagConstraints gbs = new GridBagConstraints();

		// name
		gbs.gridx = 0;
		gbs.gridy = 0;
		gbs.gridwidth = 1;
		gbs.gridheight = 1;
		gbs.weightx = 1.0;
		gbs.weighty = 1.0;
		gbs.insets = new Insets(2, 2, 2, 2);
		gbs.fill = GridBagConstraints.NONE;
		gbs.anchor = GridBagConstraints.EAST;
		layout.setConstraints(nameLabel, gbs);

		panel.add(nameLabel);

		gbs.gridx = 1;
		gbs.gridy = 0;
		gbs.gridwidth = 5;
		gbs.gridheight = 1;
		gbs.weightx = 5.0;
		gbs.weighty = 1.0;
		gbs.insets = new Insets(2, 2, 2, 16);
		gbs.fill = GridBagConstraints.HORIZONTAL;
		gbs.anchor = GridBagConstraints.CENTER;
		layout.setConstraints(nameField, gbs);
		panel.add(nameField);

		// value
		gbs.gridx = 0;
		gbs.gridy = 1;
		gbs.gridwidth = 1;
		gbs.gridheight = 1;
		gbs.weightx = 1.0;
		gbs.weighty = 1.0;
		gbs.insets = new Insets(2, 2, 2, 2);
		gbs.fill = GridBagConstraints.NONE;
		gbs.anchor = GridBagConstraints.NORTHEAST;
		layout.setConstraints(valueLabel, gbs);
		panel.add(valueLabel);

		gbs.gridx = 1;
		gbs.gridy = 1;
		gbs.gridwidth = 5;
		gbs.gridheight = 3;
		gbs.weightx = 5.0;
		gbs.weighty = 3.0;
		gbs.insets = new Insets(2, 2, 2, 16);
		gbs.fill = GridBagConstraints.BOTH;
		gbs.anchor = GridBagConstraints.CENTER;
		layout.setConstraints(valueScroll, gbs);
		panel.add(valueScroll);

		// buttons
		gbs.gridx = 2;
		gbs.gridy = 4;
		gbs.gridwidth = 1;
		gbs.gridheight = 1;
		gbs.weightx = 1.0;
		gbs.weighty = 1.0;
		gbs.insets = new Insets(2, 2, 2, 2);
		gbs.fill = GridBagConstraints.NONE;
		gbs.anchor = GridBagConstraints.EAST;
		layout.setConstraints(okButton, gbs);
		panel.add(okButton);

		gbs.gridx = 3;
		gbs.gridy = 4;
		gbs.gridwidth = 1;
		gbs.gridheight = 1;
		gbs.weightx = 1.0;
		gbs.weighty = 1.0;
		gbs.insets = new Insets(2, 2, 2, 2);
		gbs.fill = GridBagConstraints.NONE;
		gbs.anchor = GridBagConstraints.WEST;
		layout.setConstraints(cancelButton, gbs);
		panel.add(cancelButton);
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
			JOptionPane.showMessageDialog(this, "Name can not be empty", "WARN", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (StringUtils.isBlank(value)) {
			JOptionPane.showMessageDialog(this, "Value can not be empty", "WARN", JOptionPane.WARNING_MESSAGE);
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

		this.dispose();
	}

	private void cancel() {
		oldEnv = null;
		modified = false;
		this.dispose();
	}
}
