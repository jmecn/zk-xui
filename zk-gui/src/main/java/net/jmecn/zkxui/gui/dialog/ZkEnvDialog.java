package net.jmecn.zkxui.gui.dialog;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.jmecn.zkxui.client.utils.StringUtils;
import net.jmecn.zkxui.client.vo.Env;

public class ZkEnvDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private Env oldEnv;

    private Env result;

    private boolean modified = false;

    private JTextField nameField;
    
    private JTextField valueField;

    public ZkEnvDialog() {
    	this(null);
    }

    public ZkEnvDialog(Env env) {
		// TODO Auto-generated constructor stub

    	JLabel nameLabel = new JLabel("Name: ");
    	JLabel valueLabel = new JLabel("Name: ");
    	nameField = new JTextField();
    	valueField = new JTextField();
    	
    	if (env != null) {
            this.oldEnv = env;
            if (StringUtils.isNotBlank(env.getName())) {
                nameField.setText(StringUtils.trim(env.getName()));
            }
            if (StringUtils.isNotBlank(env.getZkServers())) {
                valueField.setText(StringUtils.trim(env.getZkServers()));
            }
        }
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
        	JOptionPane.showMessageDialog(this, "name can not be empty", "WARN", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (StringUtils.isBlank(value)) {
        	JOptionPane.showMessageDialog(this, "ZkServers can not be empty", "WARN", JOptionPane.WARNING_MESSAGE);
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
