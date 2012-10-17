/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.util;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

public class EditableLabelField extends JTextField {
	static final int FIELD_BORDER = 2;

	public EditableLabelField() {
		super(10);
		setBackground(new Color(255, 255, 255, 128));
		setOpaque(false);
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.BLACK),
				BorderFactory.createEmptyBorder(1, 1, 1, 1)));
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0,0, getWidth(),getHeight());
		super.paintComponent(g);
	}
}
