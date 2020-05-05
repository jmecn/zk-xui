package net.jmecn.zkxui.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import net.jmecn.zkxui.client.utils.StringUtils;
import net.jmecn.zkxui.gui.listener.ConfigHighlighter;

public class ZkImportEditor extends JDialog {

	private static final long serialVersionUID = 1L;

	private boolean modified = false;

	private String content = "";
	
	private Boolean overwrite = false;

	private JTextPane textPane;
	
	private JCheckBox check;
	
	public static void main(String[] args) {
		new ZkImportEditor().setVisible(true);
	}
	public ZkImportEditor() {
		this.setSize(1080, 720);
		this.setTitle("ZooKeeper Config Editor");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setModal(true);
		
		CenterUtils.center(this);

        JScrollPane scrollPane = new JScrollPane();

        textPane = new JTextPane();
        if (StringUtils.isNotBlank(content)) {
        	textPane.setText(content);
        }
        scrollPane.setViewportView(textPane);
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);

        textPane.addCaretListener(new ConfigHighlighter(textPane));
        
	}

	private JPanel getButtonPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		check=  new JCheckBox("Overwrite");
		check.setSelected(overwrite);
		
		
		JButton okBtn = new JButton("Save");
		okBtn.addActionListener((e) -> { save(); });

		JButton cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener((e) -> { cancel(); });

		panel.add(check);
		panel.add(okBtn);
		panel.add(cancelBtn);
		return panel;
	}

	private void save() {
		String text = textPane.getText();
		if (StringUtils.isNotBlank(text)) {
			content = text;
			modified = true;
		}

		overwrite = check.isSelected();
		dispose();
	}

	private void cancel() {
		modified = false;
		dispose();
	}

	public boolean isModified() {
		return modified;
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public String getContent() {
		return content;
	}
}
