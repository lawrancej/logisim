/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.cburch.logisim.data.Bounds;

public interface Caret {
	// listener methods
	public void addCaretListener(CaretListener e);
	public void removeCaretListener(CaretListener e);

	// query/Graphics methods
	public String getText();
	public Bounds getBounds(Graphics g);
	public void draw(Graphics g);

	// finishing
	public void commitText(String text);
	public void cancelEditing();
	public void stopEditing();

	// events to handle
	public void mousePressed(MouseEvent e);
	public void mouseDragged(MouseEvent e);
	public void mouseReleased(MouseEvent e);
	public void keyPressed(KeyEvent e);
	public void keyReleased(KeyEvent e);
	public void keyTyped(KeyEvent e);
}
