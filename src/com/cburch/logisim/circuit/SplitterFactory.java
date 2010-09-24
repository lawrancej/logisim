/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.InputEvent;

import javax.swing.Icon;

import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.circuit.Strings;
import com.cburch.logisim.comp.AbstractComponentFactory;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.tools.key.IntegerConfigurator;
import com.cburch.logisim.tools.key.JoinedConfigurator;
import com.cburch.logisim.tools.key.KeyConfigurator;
import com.cburch.logisim.tools.key.ParallelConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.Icons;
import com.cburch.logisim.util.StringGetter;

public class SplitterFactory extends AbstractComponentFactory {
	public static final SplitterFactory instance = new SplitterFactory();

	private static final Icon toolIcon = Icons.getIcon("splitter.gif");

	private SplitterFactory() { }

	@Override
	public String getName() { return "Splitter"; }

	@Override
	public StringGetter getDisplayGetter() {
		return Strings.getter("splitterComponent");
	}

	@Override
	public AttributeSet createAttributeSet() {
		return new SplitterAttributes();
	}

	@Override
	public Object getDefaultAttributeValue(Attribute<?> attr, LogisimVersion ver) {
		if (attr instanceof SplitterAttributes.BitOutAttribute) {
			SplitterAttributes.BitOutAttribute a;
			a = (SplitterAttributes.BitOutAttribute) attr;
			return a.getDefault();
		} else {
			return super.getDefaultAttributeValue(attr, ver);
		}
	}

	@Override
	public Component createComponent(Location loc, AttributeSet attrs) {
		return new Splitter(loc, attrs);
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction dir = attrs.getValue(StdAttr.FACING);
		int fanout = attrs.getValue(SplitterAttributes.ATTR_FANOUT).intValue();
		int offs = -(fanout / 2) * 10;

		if (dir == Direction.EAST) {
			return Bounds.create(0, offs, 20, 10 * (fanout - 1));
		} else if (dir == Direction.WEST) {
			return Bounds.create(-20, offs, 20, 10 * (fanout - 1));
		} else if (dir == Direction.NORTH) {
			return Bounds.create(offs, -20, 10 * (fanout - 1), 20);
		} else if (dir == Direction.SOUTH) {
			return Bounds.create(offs, 0, 10 * (fanout - 1), 20);
		} else {
			throw new IllegalArgumentException("unrecognized direction");
		}
	}

	//
	// user interface methods
	//
	@Override
	public void drawGhost(ComponentDrawContext context,
			Color color, int x, int y, AttributeSet attrs) {
		Graphics g = context.getGraphics();
		Direction dir = attrs.getValue(StdAttr.FACING);
		int fanout = attrs.getValue(SplitterAttributes.ATTR_FANOUT).intValue();

		g.setColor(color);
		GraphicsUtil.switchToWidth(g, 3);
		int offs = -(fanout / 2) * 10;
		if (dir == Direction.EAST) {
			g.drawLine(x, y, x + 10, y);
			if (fanout <= 3) {
				for (int i = 0; i < fanout; i++) {
					g.drawLine(x + 10, y, x + 20, y + offs + i * 10);
				}
			} else {
				for (int i = 0; i < fanout; i++) {
					int ty = y + offs + i * 10;
					int ty2 = ty + (ty > y ? -10 : (ty < y ? 10 : 0));
					g.drawLine(x + 10, ty2, x + 20, ty);
				}
				GraphicsUtil.switchToWidth(g, 4);
				g.drawLine(x + 10, y + offs + 10,
						x + 10, y + offs + (fanout - 2) * 10);
			}
		} else if (dir == Direction.WEST) {
			g.drawLine(x, y, x - 10, y);
			if (fanout <= 3) {
				for (int i = 0; i < fanout; i++) {
					g.drawLine(x - 10, y, x - 20, y + offs + i * 10);
				}
			} else {
				for (int i = 0; i < fanout; i++) {
					int ty = y + offs + i * 10;
					int ty2 = ty + (ty > y ? -10 : (ty < y ? 10 : 0));
					g.drawLine(x - 10, ty2, x - 20, ty);
				}
				GraphicsUtil.switchToWidth(g, 4);
				g.drawLine(x - 10, y + offs + 10,
						x - 10, y + offs + (fanout - 2) * 10);
			}
		} else if (dir == Direction.NORTH) {
			g.drawLine(x, y, x, y - 10);
			if (fanout <= 3) {
				for (int i = 0; i < fanout; i++) {
					g.drawLine(x, y - 10, x + offs + i * 10, y - 20);
				}
			} else {
				for (int i = 0; i < fanout; i++) {
					int tx = x + offs + i * 10;
					int tx2 = tx + (tx > x ? -10 : (tx < x ? 10 : 0));
					g.drawLine(tx2, y - 10, tx, y - 20);
				}
				GraphicsUtil.switchToWidth(g, 4);
				g.drawLine(x + offs + 10, y - 10,
						x + offs + (fanout - 2) * 10, y - 10);
			}
		} else if (dir == Direction.SOUTH) {
			g.drawLine(x, y, x, y + 10);
			if (fanout <= 3) {
				for (int i = 0; i < fanout; i++) {
					g.drawLine(x, y + 10, x + offs + i * 10, y + 20);
				}
			} else {
				for (int i = 0; i < fanout; i++) {
					int tx = x + offs + i * 10;
					int tx2 = tx + (tx > x ? -10 : (tx < x ? 10 : 0));
					g.drawLine(tx2, y + 10, tx, y + 20);
				}
				GraphicsUtil.switchToWidth(g, 4);
				g.drawLine(x + offs + 10, y + 10,
						x + offs + (fanout - 2) * 10, y + 10);
			}
		} else {
			super.drawGhost(context, color, x, y, attrs);
		}
	}

	@Override
	public void paintIcon(ComponentDrawContext c,
			int x, int y, AttributeSet attrs) {
		Graphics g = c.getGraphics();
		Direction dir = attrs.getValue(StdAttr.FACING);
		if (toolIcon != null) {
			Icons.paintRotated(g, x + 2, y + 2, dir, toolIcon,
					c.getDestination());
			return;
		}

		g.setColor(Color.black);
		GraphicsUtil.switchToWidth(g, 2);
		if (dir == Direction.WEST) {
			g.drawLine(x + 7, y +  5, x + 12, y + 10);
			g.drawLine(x + 7, y + 10, x + 12, y + 10);
			g.drawLine(x + 7, y + 15, x + 12, y + 10);
		} else if (dir == Direction.EAST) {
			g.drawLine(x + 7, y + 10, x + 12, y +  5);
			g.drawLine(x + 7, y + 10, x + 12, y + 10);
			g.drawLine(x + 7, y + 10, x + 12, y + 15);
		} else if (dir == Direction.SOUTH) {
			g.drawLine(x + 10, y + 7, x +  5, y + 12);
			g.drawLine(x + 10, y + 7, x + 10, y + 12);
			g.drawLine(x + 10, y + 7, x + 15, y + 12);
		} else if (dir == Direction.NORTH) {
			g.drawLine(x +  5, y + 7, x + 10, y + 12);
			g.drawLine(x + 10, y + 7, x + 10, y + 12);
			g.drawLine(x + 15, y + 7, x + 10, y + 12);
		}
	}

	@Override
	public Object getFeature(Object key, AttributeSet attrs) {
		if (key == FACING_ATTRIBUTE_KEY) {
			return StdAttr.FACING;
		} else if (key == KeyConfigurator.class) {
			KeyConfigurator altConfig = ParallelConfigurator.create(
					new BitWidthConfigurator(SplitterAttributes.ATTR_WIDTH),
					new IntegerConfigurator(SplitterAttributes.ATTR_FANOUT,
							1, 32, InputEvent.ALT_DOWN_MASK));
			return JoinedConfigurator.create(
				new IntegerConfigurator(SplitterAttributes.ATTR_FANOUT, 1, 32, 0),
				altConfig);
		}
		return super.getFeature(key, attrs);
	}
}
