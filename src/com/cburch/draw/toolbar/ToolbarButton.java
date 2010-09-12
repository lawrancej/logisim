/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.toolbar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import com.cburch.logisim.util.GraphicsUtil;

class ToolbarButton extends JComponent implements MouseListener {
	private static final int BORDER = 2;
	
	private Toolbar toolbar;
	private ToolbarItem item;
	
	ToolbarButton(Toolbar toolbar, ToolbarItem item) {
		this.toolbar = toolbar;
		this.item = item;
		addMouseListener(this);
		setFocusable(true);
		setToolTipText("");
	}
	
	public ToolbarItem getItem() {
		return item;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension dim = item.getDimension(toolbar.getOrientation());
		dim.width += 2 * BORDER;
		dim.height += 2 * BORDER;
		return dim;
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		if (toolbar.getPressed() == this) {
			Dimension dim = item.getDimension(toolbar.getOrientation());
			Color defaultColor = g.getColor();
			GraphicsUtil.switchToWidth(g, 2);
			g.setColor(Color.GRAY);
			g.fillRect(BORDER, BORDER, dim.width, dim.height);
			GraphicsUtil.switchToWidth(g, 1);
			g.setColor(defaultColor);
		}

		Graphics g2 = g.create();
		g2.translate(BORDER, BORDER);
		item.paintIcon(ToolbarButton.this, g2);
		g2.dispose();

		// draw selection indicator
		if (toolbar.getToolbarModel().isSelected(item)) {
			Dimension dim = item.getDimension(toolbar.getOrientation());
			GraphicsUtil.switchToWidth(g, 2);
			g.setColor(Color.BLACK);
			g.drawRect(BORDER, BORDER, dim.width, dim.height);
			GraphicsUtil.switchToWidth(g, 1);
		}
	}

	@Override
	public String getToolTipText(MouseEvent e) {
		return item.getToolTip();
	}

	public void mousePressed(MouseEvent e) {
		if (item != null && item.isSelectable()) {
			toolbar.setPressed(this);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (toolbar.getPressed() == this) {
			toolbar.getToolbarModel().itemSelected(item);
			toolbar.setPressed(null);
		}
	}
	
	public void mouseClicked(MouseEvent e) { }
	
	public void mouseEntered(MouseEvent e) { }
	
	public void mouseExited(MouseEvent e) {
		toolbar.setPressed(null);
	}
}
