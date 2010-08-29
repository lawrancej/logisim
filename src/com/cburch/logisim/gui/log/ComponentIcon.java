/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.log;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.Icon;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;

class ComponentIcon implements Icon {
	public static final int TRIANGLE_NONE = 0;
	public static final int TRIANGLE_CLOSED = 1;
	public static final int TRIANGLE_OPEN = 2;

	private Component comp;
	private int triangleState = TRIANGLE_NONE;

	ComponentIcon(Component comp) {
		this.comp = comp;
	}
	
	public void setTriangleState(int value) {
		triangleState = value;
	}

	public int getIconHeight() {
		return 20;
	}

	public int getIconWidth() {
		return 20;
	}

	public void paintIcon(java.awt.Component c, Graphics g,
			int x, int y) {
		// draw tool icon
		Graphics gIcon = g.create();
		ComponentDrawContext context = new ComponentDrawContext(c, null, null, g, gIcon);
		comp.getFactory().paintIcon(context, x, y, comp.getAttributeSet());
		gIcon.dispose();
		
		if (triangleState != TRIANGLE_NONE) {
			int[] xp;
			int[] yp;
			if (triangleState == TRIANGLE_CLOSED) {
				xp = new int[] { x + 13, x + 13, x + 17 };
				yp = new int[] { y + 11, y + 19, y + 15 };
			} else {
				xp = new int[] { x + 11, x + 19, x + 15 };
				yp = new int[] { y + 13, y + 13, y + 17 };
			}
			g.setColor(Color.LIGHT_GRAY);
			g.fillPolygon(xp, yp, 3);
			g.setColor(Color.DARK_GRAY);
			g.drawPolygon(xp, yp, 3);
		}
	}
}
