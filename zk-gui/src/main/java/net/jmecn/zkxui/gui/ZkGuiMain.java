package net.jmecn.zkxui.gui;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.jmecn.zkxui.gui.dialog.ZkBrowserDialog;

public class ZkGuiMain {

	public static void main(String[] args) {
//		ZkServerDialog dialog = new ZkServerDialog();
//	 	dialog.setVisible(true);
		
		String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		ZkBrowserDialog dialog = new ZkBrowserDialog();
		dialog.setVisible(true);
	}

}
