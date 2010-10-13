package com.cburch.logisim.gui.generic;

import java.awt.Frame;

import javax.swing.JDialog;

public class LDialog extends JDialog {
	public LDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		init();
	}
	
	private void init() {
		LFrame.attachIcon(this);
	}
}
