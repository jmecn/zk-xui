package net.jmecn.zkxui.gui.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.jmecn.zkxui.client.utils.StringUtils;
import net.jmecn.zkxui.client.vo.ZkNode;

public class ZkNodeDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private Set<String> existsNodes;

    private String oldValue;

    private String result;

    private boolean modified = false;

    private JTextField nameField = null;

    public ZkNodeDialog(String path, ZkNode zkNode) {
        this(path, zkNode, null);
    }

    public ZkNodeDialog(String path, ZkNode zkNode, String oldValue) {
        this.setTitle("Add ZooKeeper Node");
        this.setSize(320, 120);
        this.setResizable(false);
        this.setModal(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        CenterUtils.center(this);

        this.existsNodes = new HashSet<>();
        if (zkNode.getNodeLst() != null && zkNode.getNodeLst().size() > 0) {
            existsNodes.addAll(zkNode.getNodeLst());
        }
        if (zkNode.getLeafBeanLSt() != null && zkNode.getLeafBeanLSt().size() > 0) {
            existsNodes.addAll(zkNode.getLeafBeanLSt().stream().map(it -> it.getName()).collect(Collectors.toList()));
        }

        JLabel pathLabel = new JLabel("Current path:");
        JTextField pathField = new JTextField(path);
        pathField.setEditable(false);
        JLabel nameLabel = new JLabel("Name: ");
        nameField = new JTextField();

        if (oldValue != null) {
            this.oldValue = oldValue;
            if (StringUtils.isNotBlank(oldValue)) {
                nameField.setText(StringUtils.trim(oldValue));
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

        // buttons
        gbs.gridx = 2;
        gbs.gridy = 2;
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
        gbs.gridy = 2;
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
        String name = nameField.getText();

        if (StringUtils.isBlank(name)) {
            JOptionPane.showMessageDialog(this, "Name can not be empty", "WARN", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // check exists
        if (existsNodes.contains(name)) {
            JOptionPane.showMessageDialog(this, "Name[" + name + "] already exists", "WARN",
                    JOptionPane.WARNING_MESSAGE);
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

        dispose();
    }

    private void cancel() {
        oldValue = null;
        modified = false;
        dispose();
    }
}