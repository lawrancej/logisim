/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.comp;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.Icon;

import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.AttributeSets;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.Icons;
import com.cburch.logisim.util.StringGetter;
import com.cburch.logisim.util.StringUtil;

public abstract class AbstractComponentFactory implements ComponentFactory {
	private static final Icon toolIcon = Icons.getIcon("subcirc.gif");

	private AttributeSet defaultSet;
	
	protected AbstractComponentFactory() {
		defaultSet = null;
	}

	@Override
	public String toString() { return getName(); }

	public abstract String getName();
	public String getDisplayName() { return getDisplayGetter().toString(); }
	public StringGetter getDisplayGetter() { return StringUtil.constantGetter(getName()); }
	public abstract Component createComponent(Location loc, AttributeSet attrs);
	public abstract Bounds getOffsetBounds(AttributeSet attrs);

	public AttributeSet createAttributeSet() {
		return AttributeSets.EMPTY;
	}
	
	public boolean isAllDefaultValues(AttributeSet attrs, LogisimVersion ver) {
		return false;
	}

	public Object getDefaultAttributeValue(Attribute<?> attr, LogisimVersion ver) {
		AttributeSet dfltSet = defaultSet;
		if (dfltSet == null) {
			dfltSet = (AttributeSet) createAttributeSet().clone();
			defaultSet = dfltSet;
		}
		return dfltSet.getValue(attr);
	}

	//
	// user interface methods
	//
	public void drawGhost(ComponentDrawContext context, Color color,
				int x, int y, AttributeSet attrs) {
		Graphics g = context.getGraphics();
		Bounds bds = getOffsetBounds(attrs);
		g.setColor(color);
		GraphicsUtil.switchToWidth(g, 2);
		g.drawRect(x + bds.getX(), y + bds.getY(),
			bds.getWidth(), bds.getHeight());
	}

	public void paintIcon(ComponentDrawContext context,
			int x, int y, AttributeSet attrs) {
		Graphics g = context.getGraphics();
		if (toolIcon != null) {
			toolIcon.paintIcon(context.getDestination(), g, x + 2, y + 2);
		} else {
			g.setColor(Color.black);
			g.drawRect(x + 5, y + 2, 11, 17);
			Value[] v = { Value.TRUE, Value.FALSE };
			for (int i = 0; i < 3; i++) {
				g.setColor(v[i % 2].getColor());
				g.fillOval(x + 5 - 1, y + 5 + 5 * i - 1, 3, 3);
				g.setColor(v[(i + 1) % 2].getColor());
				g.fillOval(x + 16 - 1, y + 5 + 5 * i - 1, 3, 3);
			}
		}
	}
	
	public Object getFeature(Object key, AttributeSet attrs) {
		return null;
	}

}
