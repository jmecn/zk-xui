package net.jmecn.zkxui.gui.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ZkUpdatePropertyDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private String oldValue;

    private String result;

    private boolean modified = false;

    private JTextArea valueField = null;

    public ZkUpdatePropertyDialog(String path, String name, String oldValue) {
        this.setTitle("Update ZooKeeper Property");
        this.setModal(true);
        this.setSize(640, 240);
        this.setResizable(false);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        CenterUtils.center(this);

        this.oldValue = oldValue;

        JLabel pathLabel = new JLabel("Current path:");
        JTextField pathField = new JTextField(path);
        pathField.setEditable(false);

        JLabel nameLabel = new JLabel("Name: ");
        JTextField nameField = new JTextField(name);
        nameField.setEditable(false);

        JLabel valueLabel = new JLabel("Value: ");
        valueField = new JTextArea();
        valueField.setText(oldValue);
        valueField.setLineWrap(true);// 自动换行
        valueField.setWrapStyleWord(true);
        JScrollPane valueScroll = new JScrollPane();
        valueScroll.setViewportView(valueField);

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

        // path
        gbs.gridx = 0;
        gbs.gridy = 0;
        gbs.gridwidth = 1;
        gbs.gridheight = 1;
        gbs.weightx = 1.0;
        gbs.weighty = 1.0;
        gbs.insets = new Insets(2, 2, 2, 2);
        gbs.fill = GridBagConstraints.NONE;
        gbs.anchor = GridBagConstraints.EAST;
        layout.setConstraints(pathLabel, gbs);

        panel.add(pathLabel);

        gbs.gridx = 1;
        gbs.gridy = 0;
        gbs.gridwidth = 4;
        gbs.gridheight = 1;
        gbs.weightx = 4.0;
        gbs.weighty = 1.0;
        gbs.insets = new Insets(2, 2, 2, 16);
        gbs.fill = GridBagConstraints.HORIZONTAL;
        gbs.anchor = GridBagConstraints.WEST;
        layout.setConstraints(pathField, gbs);
        panel.add(pathField);

        // name
        gbs.gridx = 0;
        gbs.gridy = 1;
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
        gbs.gridy = 1;
        gbs.gridwidth = 4;
        gbs.gridheight = 1;
        gbs.weightx = 4.0;
        gbs.weighty = 1.0;
        gbs.insets = new Insets(2, 2, 2, 16);
        gbs.fill = GridBagConstraints.HORIZONTAL;
        gbs.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(nameField, gbs);
        panel.add(nameField);

        // value
        gbs.gridx = 0;
        gbs.gridy = 2;
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
        gbs.gridy = 2;
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
        gbs.gridy = 5;
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
        gbs.gridy = 5;
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

        dispose();
    }

    private void cancel() {
        oldValue = null;
        modified = false;
        dispose();
    }
}