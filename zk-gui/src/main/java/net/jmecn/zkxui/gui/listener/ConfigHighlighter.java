/**
 * Huatu.com Inc. Copyright (c) 2014-2020 All Rights Reserved.
 */
package net.jmecn.zkxui.gui.listener;

import java.awt.Color;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 * @title PropertiesHighligher
 * @author yanmaoyuan
 * @date 2020年5月1日
 * @version 1.0
 */
public class ConfigHighlighter implements CaretListener {

    private JTextComponent textComponent;

    private Highlighter.HighlightPainter commentPainter = new DefaultHighlightPainter(new Color(192, 192, 192));

    private Highlighter.HighlightPainter nodePainter = new DefaultHighlightPainter(new Color(72, 192, 72));

    private Highlighter.HighlightPainter keyPainter = new DefaultHighlightPainter(new Color(196, 255, 196));

    private Highlighter.HighlightPainter valuePainter = new DefaultHighlightPainter(new Color(255, 255, 128));

    public ConfigHighlighter(JTextComponent textComponent) {
        this.textComponent = textComponent;
        refresh(this.textComponent);
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        if (e.getMark() == e.getDot()) {
            refresh(this.textComponent);
        }
    }

    public void refresh(JTextComponent textComponent) {
        Highlighter hl = textComponent.getHighlighter();
        hl.removeAllHighlights();

        String text = textComponent.getText();
        String[] lines = text.split("\n");

        int totalIndex = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.trim().length() == 0) {
                totalIndex += line.length();
                totalIndex++; // for '\n'
                continue;
            }
            // 注释
            else if (line.startsWith("#")) {
                int start = totalIndex;
                int end = start + line.trim().length();

                try {
                    hl.addHighlight(start, end, commentPainter);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }

                totalIndex += line.length();
                totalIndex++; // for '\n'
                continue;
            }
            // 数据节点
            else if (line.startsWith("/")) {
                int start = totalIndex;
                int first = line.indexOf('=');

                if (first > 0) {
                    int end = start + first;
                    try {
                        hl.addHighlight(start, end, nodePainter);
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }

                    int second = line.indexOf('=', first + 1);
                    if (second > 0) {
                        try {
                            hl.addHighlight(end + 1, start + second, keyPainter);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }

                        try {
                            hl.addHighlight(start + second + 1, start + line.trim().length(), valuePainter);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                totalIndex += line.length();
                totalIndex++; // for '\n'
                continue;
            }
        }
    }
}