/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.plexers;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.LogisimVersion;
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
import static com.cburch.logisim.util.LocaleString.*;

public class Demultiplexer extends InstanceFactory {
	public Demultiplexer() {
		super("Demultiplexer", __("demultiplexerComponent"));
		setAttributes(new Attribute[] {
				StdAttr.FACING, Plexers.ATTR_SELECT_LOC, Plexers.ATTR_SELECT, StdAttr.WIDTH,
				Plexers.ATTR_TRISTATE, Plexers.ATTR_DISABLED, Plexers.ATTR_ENABLE
			}, new Object[] {
				Direction.EAST, Plexers.SELECT_BOTTOM_LEFT, Plexers.DEFAULT_SELECT, BitWidth.ONE,
				Plexers.DEFAULT_TRISTATE, Plexers.DISABLED_FLOATING, Boolean.TRUE
			});
		setKeyConfigurator(JoinedConfigurator.create(
				new BitWidthConfigurator(Plexers.ATTR_SELECT, 1, 5, 0),
				new BitWidthConfigurator(StdAttr.WIDTH)));
		setFacingAttribute(StdAttr.FACING);
		setIconName("demultiplexer.gif");
	}
	
	@Override
	public Object getDefaultAttributeValue(Attribute<?> attr, LogisimVersion ver) {
		if (attr == Plexers.ATTR_ENABLE) {
			int newer = ver.compareTo(LogisimVersion.get(2, 6, 3, 220));
			return Boolean.valueOf(newer >= 0);
		} else {
			return super.getDefaultAttributeValue(attr, ver);
		}
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction facing = attrs.getValue(StdAttr.FACING);
		BitWidth select = attrs.getValue(Plexers.ATTR_SELECT);
		int outputs = 1 << select.getWidth();
		Bounds bds;
		if (outputs == 2) {
			bds = Bounds.create(0, -20, 30, 40);
		} else {
			bds = Bounds.create(0, -(outputs / 2) * 10 - 10,
					40, outputs * 10 + 20);
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
		if (attr == StdAttr.FACING || attr == Plexers.ATTR_SELECT_LOC
				|| attr == Plexers.ATTR_SELECT) {
			instance.recomputeBounds();
			updatePorts(instance);
		} else if (attr == StdAttr.WIDTH || attr == Plexers.ATTR_ENABLE) {
			updatePorts(instance);
		} else if (attr == Plexers.ATTR_TRISTATE || attr == Plexers.ATTR_DISABLED) {
			instance.fireInvalidated();
		}
	}

	private void updatePorts(Instance instance) {
		Direction facing = instance.getAttributeValue(StdAttr.FACING);
		Object selectLoc = instance.getAttributeValue(Plexers.ATTR_SELECT_LOC);
		BitWidth data = instance.getAttributeValue(StdAttr.WIDTH);
		BitWidth select = instance.getAttributeValue(Plexers.ATTR_SELECT);
		boolean enable = instance.getAttributeValue(Plexers.ATTR_ENABLE).booleanValue();
		int outputs = 1 << select.getWidth();
		Port[] ps = new Port[outputs + (enable ? 3 : 2)];
		Location sel;
		int selMult = selectLoc == Plexers.SELECT_BOTTOM_LEFT ? 1 : -1;
		if (outputs == 2) {
			Location end0;
			Location end1;
			if (facing == Direction.WEST) {
				end0 = Location.create(-30, -10);
				end1 = Location.create(-30,  10);
				sel = Location.create(-20,  selMult * 20);
			} else if (facing == Direction.NORTH) {
				end0 = Location.create(-10, -30);
				end1 = Location.create( 10, -30);
				sel = Location.create(selMult * -20, -20);
			} else if (facing == Direction.SOUTH) {
				end0 = Location.create(-10,  30);
				end1 = Location.create( 10,  30);
				sel = Location.create(selMult * -20,  20);
			} else {
				end0 = Location.create(30, -10);
				end1 = Location.create(30,  10);
				sel = Location.create(20,  selMult * 20);
			}
			ps[0] = new Port(end0.getX(), end0.getY(), Port.OUTPUT, data.getWidth());
			ps[1] = new Port(end1.getX(), end1.getY(), Port.OUTPUT, data.getWidth());
		} else {
			int dx = -(outputs / 2) * 10;
			int ddx = 10;
			int dy = dx;
			int ddy = 10;
			if (facing == Direction.WEST) {
				dx = -40; ddx = 0;
				sel = Location.create(-20, selMult * (dy + 10 * outputs));
			} else if (facing == Direction.NORTH) {
				dy = -40; ddy = 0;
				sel = Location.create(selMult * dx, -20);
			} else if (facing == Direction.SOUTH) {
				dy = 40; ddy = 0;
				sel = Location.create(selMult * dx, 20);
			} else {
				dx = 40; ddx = 0;
				sel = Location.create(20, selMult * (dy + 10 * outputs));
			}
			for (int i = 0; i < outputs; i++) {
				ps[i] = new Port(dx, dy, Port.OUTPUT, data.getWidth());
				dx += ddx;
				dy += ddy;
			}
		}
		Location en = sel.translate(facing, -10);
		ps[outputs] = new Port(sel.getX(), sel.getY(), Port.INPUT, select.getWidth());
		if (enable) {
			ps[outputs + 1] = new Port(en.getX(), en.getY(), Port.INPUT, BitWidth.ONE);
		}
		ps[ps.length - 1] = new Port(0, 0, Port.INPUT, data.getWidth());
		
		for (int i = 0; i < outputs; i++) {
			ps[i].setToolTip(__("demultiplexerOutTip", "" + i));
		}
		ps[outputs].setToolTip(__("demultiplexerSelectTip"));
		if (enable) {
			ps[outputs + 1].setToolTip(__("demultiplexerEnableTip"));
		}
		ps[ps.length - 1].setToolTip(__("demultiplexerInTip"));

		instance.setPorts(ps);
	}

	@Override
	public void propagate(InstanceState state) {
		// get attributes
		BitWidth data = state.getAttributeValue(StdAttr.WIDTH);
		BitWidth select = state.getAttributeValue(Plexers.ATTR_SELECT);
		Boolean threeState = state.getAttributeValue(Plexers.ATTR_TRISTATE);
		boolean enable = state.getAttributeValue(Plexers.ATTR_ENABLE).booleanValue();
		int outputs = 1 << select.getWidth();
		Value en = enable ? state.getPort(outputs + 1) : Value.TRUE;

		// determine output values
		Value others; // the default output
		if (threeState.booleanValue()) {
			others = Value.createUnknown(data);
		} else {
			others = Value.createKnown(data, 0);
		}
		int outIndex = -1; // the special output
		Value out = null;
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
				out = state.getPort(outputs + (enable ? 2 : 1));
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
		boolean enable = painter.getAttributeValue(Plexers.ATTR_ENABLE).booleanValue();
		int outputs = 1 << select.getWidth();

		// draw select and enable inputs
		GraphicsUtil.switchToWidth(g, 3);
		boolean vertical = facing == Direction.NORTH || facing == Direction.SOUTH;
		Object selectLoc = painter.getAttributeValue(Plexers.ATTR_SELECT_LOC);
		int selMult = selectLoc == Plexers.SELECT_BOTTOM_LEFT ? 1 : -1;
		int dx = vertical ? selMult : 0;
		int dy = vertical ? 0 : -selMult;
		if (outputs == 2) { // draw select wire
			Location sel = painter.getInstance().getPortLocation(outputs);
			if (painter.getShowState()) {
				g.setColor(painter.getPort(outputs).getColor());
			}
			g.drawLine(sel.getX(), sel.getY(), sel.getX() + 2 * dx, sel.getY() + 2 * dy);
		}
		if (enable) {
			Location en = painter.getInstance().getPortLocation(outputs + 1);
			if (painter.getShowState()) {
				g.setColor(painter.getPort(outputs + 1).getColor());
			}
			int len = outputs == 2 ? 6 : 4;
			g.drawLine(en.getX(), en.getY(), en.getX() + len * dx, en.getY() + len * dy);
		}
		GraphicsUtil.switchToWidth(g, 1);
		
		// draw a circle indicating where the select input is located
		Multiplexer.drawSelectCircle(g, bds, painter.getInstance().getPortLocation(outputs));

		// draw "0" next to first input
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

		// draw trapezoid, "DMX" label, and ports
		g.setColor(Color.BLACK);
		Plexers.drawTrapezoid(g, bds, facing.reverse(), select.getWidth() == 1 ? 10 : 20);
		GraphicsUtil.drawCenteredText(g, "DMX",
				bds.getX() + bds.getWidth() / 2,
				bds.getY() + bds.getHeight() / 2);
		painter.drawPorts();
	}
}
