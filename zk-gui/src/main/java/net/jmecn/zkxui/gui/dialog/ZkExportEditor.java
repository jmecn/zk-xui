package net.jmecn.zkxui.gui.dialog;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import net.jmecn.zkxui.client.utils.StringUtils;
import net.jmecn.zkxui.gui.listener.ConfigHighlighter;

public class ZkExportEditor extends JDialog {

    private static final long serialVersionUID = 1L;

    public ZkExportEditor() {
        this(null);
    }

    public ZkExportEditor(String content) {
        this.setSize(1080, 720);
        this.setTitle("ZooKeeper Config Editor");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        CenterUtils.center(this);

        JScrollPane scrollPane = new JScrollPane();

        JTextPane textPane = new JTextPane();
        if (StringUtils.isNotBlank(content)) {
            textPane.setText(content);
        }
        scrollPane.setViewportView(textPane);
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);

        textPane.addCaretListener(new ConfigHighlighter(textPane));
    }
}
