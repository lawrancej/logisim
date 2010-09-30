/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.plexers;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.tools.key.JoinedConfigurator;
import com.cburch.logisim.util.GraphicsUtil;

public class Multiplexer extends InstanceFactory {
	public Multiplexer() {
		super("Multiplexer", Strings.getter("multiplexerComponent"));
		setAttributes(new Attribute[] {
				StdAttr.FACING, Plexers.ATTR_SELECT, StdAttr.WIDTH
			}, new Object[] {
				Direction.EAST, Plexers.DEFAULT_SELECT, BitWidth.ONE
			});
		setKeyConfigurator(JoinedConfigurator.create(
				new BitWidthConfigurator(Plexers.ATTR_SELECT, 1, 5, 0),
				new BitWidthConfigurator(StdAttr.WIDTH)));
		setIconName("multiplexer.gif");
		setFacingAttribute(StdAttr.FACING);
	}
	
	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction dir = attrs.getValue(StdAttr.FACING);
		BitWidth select = attrs.getValue(Plexers.ATTR_SELECT);
		int inputs = 1 << select.getWidth();
		if (inputs == 2) {
			return Bounds.create(-30, -20, 30, 40).rotate(Direction.EAST, dir, 0, 0);
		} else {
			int offs = -(inputs / 2) * 10 - 10;
			int length = inputs * 10 + 20;
			return Bounds.create(-40, offs, 40, length).rotate(Direction.EAST, dir, 0, 0);
		}
	}
	
	@Override
	public boolean contains(Location loc, AttributeSet attrs) {
		Direction facing = attrs.getValue(StdAttr.FACING);
		return Plexers.contains(loc, getOffsetBounds(attrs), facing);
	}

	@Override
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		updatePorts(instance);
	}

	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == StdAttr.FACING || attr == Plexers.ATTR_SELECT) {
			instance.recomputeBounds();
			updatePorts(instance);
		} else if (attr == StdAttr.WIDTH) {
			updatePorts(instance);
		}
	}

	private void updatePorts(Instance instance) {
		Object dir = instance.getAttributeValue(StdAttr.FACING);
		BitWidth data = instance.getAttributeValue(StdAttr.WIDTH);
		BitWidth select = instance.getAttributeValue(Plexers.ATTR_SELECT);
		int inputs = 1 << select.getWidth();
		Port[] ps = new Port[inputs + 2];
		if (inputs == 2) {
			Location end0;
			Location end1;
			Location end2;
			if (dir == Direction.WEST) {
				end0 = Location.create(30, -10);
				end1 = Location.create(30,  10);
				end2 = Location.create(20,  20);
			} else if (dir == Direction.NORTH) {
				end0 = Location.create(-10, 30);
				end1 = Location.create( 10, 30);
				end2 = Location.create(-20, 20);
			} else if (dir == Direction.SOUTH) {
				end0 = Location.create(-10, -30);
				end1 = Location.create( 10, -30);
				end2 = Location.create(-20, -20);
			} else {
				end0 = Location.create(-30, -10);
				end1 = Location.create(-30,  10);
				end2 = Location.create(-20,  20);
			}
			ps[0] = new Port(end0.getX(), end0.getY(), Port.INPUT,  data.getWidth());
			ps[1] = new Port(end1.getX(), end1.getY(), Port.INPUT,  data.getWidth());
			ps[2] = new Port(end2.getX(), end2.getY(), Port.OUTPUT, select.getWidth());
		} else {
			Location selLoc;
			int dx = -(inputs / 2) * 10;
			int ddx = 10;
			int dy = -(inputs / 2) * 10;
			int ddy = 10; 
			if (dir == Direction.WEST) {
				dx = 40; ddx = 0;
				selLoc = Location.create(20, dy + 10 * inputs);
			} else if (dir == Direction.NORTH) {
				dy = 40; ddy = 0;
				selLoc = Location.create(dx, 20);
			} else if (dir == Direction.SOUTH) {
				dy = -40; ddy = 0;
				selLoc = Location.create(dx, -20);
			} else {
				dx = -40; ddx = 0;
				selLoc = Location.create(-20, dy + 10 * inputs);
			}
			for (int i = 0; i < inputs; i++) {
				ps[i] = new Port(dx, dy, Port.INPUT, data.getWidth());
				dx += ddx;
				dy += ddy;
			}
			ps[inputs] = new Port(selLoc.getX(), selLoc.getY(), Port.INPUT, select.getWidth());
		}
		ps[inputs + 1] = new Port(0, 0, Port.OUTPUT, data.getWidth());

		for (int i = 0; i < inputs; i++) {
			ps[i].setToolTip(Strings.getter("multiplexerInTip", "" + i));
		}
		ps[inputs].setToolTip(Strings.getter("multiplexerSelectTip"));
		ps[inputs + 1].setToolTip(Strings.getter("multiplexerOutTip"));

		instance.setPorts(ps);
	}

	@Override
	public void propagate(InstanceState state) {
		BitWidth data = state.getAttributeValue(StdAttr.WIDTH);
		BitWidth select = state.getAttributeValue(Plexers.ATTR_SELECT);
		int inputs = 1 << select.getWidth();
		Value sel = state.getPort(inputs);
		Value out;
		if (sel.isFullyDefined()) {
			out = state.getPort(sel.toIntValue());
		} else if (sel.isErrorValue()) {
			out = Value.createError(data);
		} else {
			out = Value.createUnknown(data);
		}
		state.setPort(inputs + 1, out, Plexers.DELAY);
	}
	
	@Override
	public void paintGhost(InstancePainter painter) {
		Direction facing = painter.getAttributeValue(StdAttr.FACING);
		BitWidth select = painter.getAttributeValue(Plexers.ATTR_SELECT);
		Plexers.drawTrapezoid(painter.getGraphics(), painter.getBounds(),
				facing, select.getWidth() == 1 ? 10 : 20);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		Direction facing = painter.getAttributeValue(StdAttr.FACING);
		BitWidth select = painter.getAttributeValue(Plexers.ATTR_SELECT);
		int inputs = 1 << select.getWidth();

		if (inputs == 2) { // draw select wire
			GraphicsUtil.switchToWidth(g, 3);
			Location pt = painter.getLocation();
			if (painter.getShowState()) {
				g.setColor(painter.getPort(inputs).getColor());
			}
			boolean vertical = facing != Direction.NORTH && facing != Direction.SOUTH;
			int dx = vertical ? 0 : -3;
			int dy = vertical ? 3 :  0;
			g.drawLine(pt.getX() - dx, pt.getY() - dy, pt.getX(), pt.getY());
			GraphicsUtil.switchToWidth(g, 1);
			g.setColor(Color.BLACK);
		}
		Bounds bds = painter.getBounds();
		Plexers.drawTrapezoid(g, bds, facing, select.getWidth() == 1 ? 10 : 20);
		g.setColor(Color.GRAY);
		int x0;
		int y0;
		int halign;
		if (facing == Direction.WEST) {
			x0 = bds.getX() + bds.getWidth() - 3;
			y0 = bds.getY() + 15;
			halign = GraphicsUtil.H_RIGHT;
		} else if (facing == Direction.NORTH) {
			x0 = bds.getX() + 10;
			y0 = bds.getY() + bds.getHeight() - 2;
			halign = GraphicsUtil.H_CENTER;
		} else if (facing == Direction.SOUTH) {
			x0 = bds.getX() + 10;
			y0 = bds.getY() + 12;
			halign = GraphicsUtil.H_CENTER;
		} else {
			x0 = bds.getX() + 3;
			y0 = bds.getY() + 15;
			halign = GraphicsUtil.H_LEFT;
		}
		GraphicsUtil.drawText(g, "0", x0, y0, halign, GraphicsUtil.V_BASELINE);
		g.setColor(Color.BLACK);
		GraphicsUtil.drawCenteredText(g, "MUX",
				bds.getX() + bds.getWidth() / 2,
				bds.getY() + bds.getHeight() / 2);
		painter.drawPorts();
	}
}
