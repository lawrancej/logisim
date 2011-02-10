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
import com.cburch.logisim.util.GraphicsUtil;

public class Decoder extends InstanceFactory {
	public Decoder() {
		super("Decoder", Strings.getter("decoderComponent"));
		setAttributes(new Attribute[] {
				StdAttr.FACING, Plexers.ATTR_SELECT,
				Plexers.ATTR_TRISTATE, Plexers.ATTR_DISABLED
			}, new Object[] {
				Direction.EAST, Plexers.DEFAULT_SELECT,
				Plexers.DEFAULT_TRISTATE, Plexers.DISABLED_FLOATING
			});
		setKeyConfigurator(new BitWidthConfigurator(Plexers.ATTR_SELECT, 1, 5, 0));
		setIconName("decoder.gif");
		setFacingAttribute(StdAttr.FACING);
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction facing = attrs.getValue(StdAttr.FACING);
		BitWidth select = attrs.getValue(Plexers.ATTR_SELECT);
		int outputs = 1 << select.getWidth();
		Bounds bds;
		boolean reversed = facing == Direction.WEST || facing == Direction.NORTH;
		if (outputs == 2) {
			int y = reversed ? 0 : -40;
			bds = Bounds.create(-20, y, 30, 40);
		} else {
			int x = -20;
			int y = reversed ? -10 : -(outputs * 10 + 10);
			bds = Bounds.create(x, y, 40, outputs * 10 + 20);
		}
		return bds.rotate(Direction.EAST, facing, 0, 0);
	}
	
	@Override
	public boolean contains(Location loc, AttributeSet attrs) {
		Direction facing = attrs.getValue(StdAttr.FACING).reverse();
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
		} else if (attr == Plexers.ATTR_SELECT) {
			updatePorts(instance);
		} else if (attr == Plexers.ATTR_TRISTATE || attr == Plexers.ATTR_DISABLED) {
			instance.fireInvalidated();
		}
	}

	private void updatePorts(Instance instance) {
		Direction facing = instance.getAttributeValue(StdAttr.FACING);
		BitWidth select = instance.getAttributeValue(Plexers.ATTR_SELECT);
		int outputs = 1 << select.getWidth();
		Port[] ps = new Port[outputs + 2];
		if (outputs == 2) {
			Location end0;
			Location end1;
			if (facing == Direction.WEST) {
				end0 = Location.create(-10, -30);
				end1 = Location.create(-10, -10);
			} else if (facing == Direction.NORTH) {
				end0 = Location.create(10, -10);
				end1 = Location.create(30, -10);
			} else if (facing == Direction.SOUTH) {
				end0 = Location.create(10, 10);
				end1 = Location.create(30, 10);
			} else {
				end0 = Location.create(10, -30);
				end1 = Location.create(10, -10);
			}
			ps[0] = new Port(end0.getX(), end0.getY(), Port.OUTPUT, 1);
			ps[1] = new Port(end1.getX(), end1.getY(), Port.OUTPUT, 1);
		} else {
			int dx = 0;
			int ddx = 10;
			int dy = -outputs * 10;
			int ddy = 10;
			if (facing == Direction.WEST) {
				dx = -20; ddx = 0;
			} else if (facing == Direction.NORTH) {
				dy = -20; ddy = 0;
			} else if (facing == Direction.SOUTH) {
				dy = 20; ddy = 0;
			} else {
				dx = 20; ddx = 0;
			}
			for (int i = 0; i < outputs; i++) {
				ps[i] = new Port(dx, dy, Port.OUTPUT, 1);
				dx += ddx;
				dy += ddy;
			}
		}
		Location en = Location.create(0, 0).translate(facing, -10);
		ps[outputs] = new Port(0, 0, Port.INPUT, select.getWidth());
		ps[outputs + 1] = new Port(en.getX(), en.getY(), Port.INPUT, BitWidth.ONE);
		for (int i = 0; i < outputs; i++) {
			ps[i].setToolTip(Strings.getter("decoderOutTip", "" + i));
		}
		ps[outputs].setToolTip(Strings.getter("decoderSelectTip"));
		ps[outputs + 1].setToolTip(Strings.getter("decoderEnableTip"));
		instance.setPorts(ps);
	}

	@Override
	public void propagate(InstanceState state) {
		// get attributes
		BitWidth data = BitWidth.ONE;
		BitWidth select = state.getAttributeValue(Plexers.ATTR_SELECT);
		Boolean threeState = state.getAttributeValue(Plexers.ATTR_TRISTATE);
		int outputs = 1 << select.getWidth();
		
		// determine default output values
		Value others; // the default output
		if (threeState.booleanValue()) {
			others = Value.UNKNOWN;
		} else {
			others = Value.FALSE;
		}

		// determine selected output value
		int outIndex = -1; // the special output
		Value out = null;
		Value en = state.getPort(outputs + 1);
		if (en == Value.FALSE) {
			Object opt = state.getAttributeValue(Plexers.ATTR_DISABLED);
			Value base = opt == Plexers.DISABLED_ZERO ? Value.FALSE : Value.UNKNOWN;
			others = Value.repeat(base, data.getWidth());
		} else if (en == Value.ERROR && state.isPortConnected(outputs + 1)) {
			others = Value.createError(data);
		} else {
			Value sel = state.getPort(outputs);
			if (sel.isFullyDefined()) {
				outIndex = sel.toIntValue();
				out = Value.TRUE;
			} else if (sel.isErrorValue()) {
				others = Value.createError(data);
			} else {
				others = Value.createUnknown(data);
			}
		}

		// now propagate them
		for (int i = 0; i < outputs; i++) {
			state.setPort(i, i == outIndex ? out : others, Plexers.DELAY);
		}
	}

	@Override
	public void paintGhost(InstancePainter painter) {
		Direction facing = painter.getAttributeValue(StdAttr.FACING);
		BitWidth select = painter.getAttributeValue(Plexers.ATTR_SELECT);
		Plexers.drawTrapezoid(painter.getGraphics(), painter.getBounds(),
				facing.reverse(), select.getWidth() == 1 ? 10 : 20);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		Bounds bds = painter.getBounds();
		Direction facing = painter.getAttributeValue(StdAttr.FACING);
		BitWidth select = painter.getAttributeValue(Plexers.ATTR_SELECT);
		int outputs = 1 << select.getWidth();

		// draw stubs for select and enable ports
		GraphicsUtil.switchToWidth(g, 3);
		boolean vertical = facing == Direction.NORTH || facing == Direction.SOUTH;
		int dx = vertical ? 1 : 0;
		int dy = vertical ? 0 : -1;
		if (outputs == 2) { // draw select wire
			if (painter.getShowState()) {
				g.setColor(painter.getPort(outputs).getColor());
			}
			Location pt = painter.getInstance().getPortLocation(outputs);
			g.drawLine(pt.getX(), pt.getY(), pt.getX() + 2 * dx, pt.getY() + 2 * dy);
		}
		Location en = painter.getInstance().getPortLocation(outputs + 1);
		int len = outputs == 2 ? 6 : 4;
		if (painter.getShowState()) {
			g.setColor(painter.getPort(outputs + 1).getColor());
		}
		g.drawLine(en.getX(), en.getY(), en.getX() + len * dx, en.getY() + len * dy);
		GraphicsUtil.switchToWidth(g, 1);
		
		// draw a circle indicating where the select input is located
		Multiplexer.drawSelectCircle(g, bds, painter.getInstance().getPortLocation(outputs));
		
		// draw "0"
		int x0;
		int y0;
		int halign;
		if (facing == Direction.WEST) {
			x0 = 3;
			y0 = 15;
			halign = GraphicsUtil.H_LEFT;
		} else if (facing == Direction.NORTH) {
			x0 = 10;
			y0 = 15;
			halign = GraphicsUtil.H_CENTER;
		} else if (facing == Direction.SOUTH) {
			x0 = 10;
			y0 = bds.getHeight() - 3;
			halign = GraphicsUtil.H_CENTER;
		} else {
			x0 = bds.getWidth() - 3;
			y0 = 15;
			halign = GraphicsUtil.H_RIGHT;
		}
		g.setColor(Color.GRAY);
		GraphicsUtil.drawText(g, "0", bds.getX() + x0, bds.getY() + y0,
				halign, GraphicsUtil.V_BASELINE);
		
		// draw trapezoid, "Decd", and ports
		g.setColor(Color.BLACK);
		Plexers.drawTrapezoid(g, bds, facing.reverse(), outputs == 2 ? 10 : 20);
		GraphicsUtil.drawCenteredText(g, "Decd",
				bds.getX() + bds.getWidth() / 2,
				bds.getY() + bds.getHeight() / 2);
		painter.drawPorts();
	}
}

