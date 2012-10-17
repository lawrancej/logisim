/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.toolbar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

public class ToolbarSeparator implements ToolbarItem {
	private int size;
	
	public ToolbarSeparator(int size) {
		this.size = size;
	}
	
	public boolean isSelectable() {
		return false;
	}
	
	public void paintIcon(Component destination, Graphics g) {
		Dimension dim = destination.getSize();
		g.setColor(Color.GRAY);
		int x = 0;
		int y = 0;
		int w = dim.width;
		int h = dim.height;
		if (h >= w) { // separator is a vertical line in horizontal toolbar
			h -= 8;
			y = 2;
			x = (w - 2) / 2;
			w = 2;
		} else { // separator is a horizontal line in vertical toolbar
			w -= 8;
			x = 2;
			y = (h - 2) / 2;
			h = 2;
		}
		g.fillRect(x, y, w, h);
	}
	
	public String getToolTip() {
		return null;
	}
	
	public Dimension getDimension(Object orientation) {
		return new Dimension(size, size);
	}
}
