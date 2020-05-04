/**
 * Huatu.com Inc. Copyright (c) 2014-2020 All Rights Reserved.
 */
package net.jmecn.zkxui.gui;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import net.jmecn.zkxui.gui.listener.ConfigHighlighter;

/**
 * @title Main
 * @author yanmaoyuan
 * @date 2020年4月30日
 * @version 1.0
 */
public class HighlightTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        frame.setTitle("Highlight");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JScrollPane scrollPane = new JScrollPane();

        JTextPane textPane = new JTextPane();
        textPane.setText(
            "#comment\n/config/skynet-config-prod/application-jdbc.yml=spring.datasource.url=jdbc:mysql://localhost:3306/skynet?useSSL=false");
        scrollPane.setViewportView(textPane);
        frame.getContentPane().add(scrollPane);

        textPane.addCaretListener(new ConfigHighlighter(textPane));
        frame.setVisible(true);
    }

}
