package net.jmecn.zkxui.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * @title Main
 * @author yanmaoyuan
 * @date 2020年5月1日
 * @version 1.0
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    /**
     * @param args
     */
    public static void main(String[] args) {
        MainFrame app = new MainFrame();
        app.setVisible(true);
    }

    public MainFrame() {
        this.setSize(1080, 720);
        this.setContentPane(contentPane());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public JPanel contentPane() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // JPanel north = new JPanel();
        // panel.add(north, BorderLayout.NORTH);
        JTextField path = new JTextField("/");
        panel.add(path, BorderLayout.NORTH);

        JScrollPane left = new JScrollPane();
        panel.add(left, BorderLayout.WEST);
        JList<String> list = new JList<>(new String[] { "..", "config", "dubbo", "topics", "spring-cloud-service" });
        list.setSelectedIndex(0);
        left.setViewportView(list);

        JScrollPane center = new JScrollPane();
        panel.add(center, BorderLayout.CENTER);

        JTable table = new JTable(new Object[][] { { "foo", "bar" } }, new Object[] { "name", "value" });
        center.setViewportView(table);
        return panel;
    }

}
