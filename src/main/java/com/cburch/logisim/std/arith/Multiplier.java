/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.arith;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import static com.cburch.logisim.util.LocaleString.*;

public class Multiplier extends InstanceFactory {
	static final int PER_DELAY = 1;

	private static final int IN0   = 0;
	private static final int IN1   = 1;
	private static final int OUT   = 2;
	private static final int C_IN  = 3;
	private static final int C_OUT = 4;

	public Multiplier() {
		super("Multiplier", __("multiplierComponent"));
		setAttributes(new Attribute[] { StdAttr.WIDTH },
				new Object[] { BitWidth.create(8) });
		setKeyConfigurator(new BitWidthConfigurator(StdAttr.WIDTH));
		setOffsetBounds(Bounds.create(-40, -20, 40, 40));
		setIconName("multiplier.gif");

		Port[] ps = new Port[5];
		ps[IN0]   = new Port(-40, -10, Port.INPUT,  StdAttr.WIDTH);
		ps[IN1]   = new Port(-40,  10, Port.INPUT,  StdAttr.WIDTH);
		ps[OUT]   = new Port(  0,   0, Port.OUTPUT, StdAttr.WIDTH);
		ps[C_IN]  = new Port(-20, -20, Port.INPUT,  StdAttr.WIDTH);
		ps[C_OUT] = new Port(-20,  20, Port.OUTPUT, StdAttr.WIDTH);
		ps[IN0].setToolTip(__("multiplierInputTip"));
		ps[IN1].setToolTip(__("multiplierInputTip"));
		ps[OUT].setToolTip(__("multiplierOutputTip"));
		ps[C_IN].setToolTip(__("multiplierCarryInTip"));
		ps[C_OUT].setToolTip(__("multiplierCarryOutTip"));
		setPorts(ps);
	}

	@Override
	public void propagate(InstanceState state) {
		// get attributes
		BitWidth dataWidth = state.getAttributeValue(StdAttr.WIDTH);

		// compute outputs
		Value a = state.getPort(IN0);
		Value b = state.getPort(IN1);
		Value c_in = state.getPort(C_IN);
		Value[] outs = Multiplier.computeProduct(dataWidth, a, b, c_in);

		// propagate them
		int delay = dataWidth.getWidth() * (dataWidth.getWidth() + 2) * PER_DELAY;
		state.setPort(OUT,   outs[0], delay);
		state.setPort(C_OUT, outs[1], delay);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		painter.drawBounds();

		g.setColor(Color.GRAY);
		painter.drawPort(IN0);
		painter.drawPort(IN1);
		painter.drawPort(OUT);
		painter.drawPort(C_IN,  "c in",  Direction.NORTH);
		painter.drawPort(C_OUT, "c out", Direction.SOUTH);

		Location loc = painter.getLocation();
		int x = loc.getX();
		int y = loc.getY();
		GraphicsUtil.switchToWidth(g, 2);
		g.setColor(Color.BLACK);
		g.drawLine(x - 15, y - 5, x - 5, y + 5);
		g.drawLine(x - 15, y + 5, x - 5, y - 5);
		GraphicsUtil.switchToWidth(g, 1);
	}


	static Value[] computeProduct(BitWidth width, Value a, Value b, Value c_in) {
		int w = width.getWidth();
		if (c_in == Value.NIL || c_in.isUnknown()) c_in = Value.createKnown(width, 0);
		if (a.isFullyDefined() && b.isFullyDefined() && c_in.isFullyDefined()) {
			long sum = (long) a.toIntValue() * (long) b.toIntValue()
				+ (long) c_in.toIntValue();
			return new Value[] { Value.createKnown(width, (int) sum),
				Value.createKnown(width, (int) (sum >> w)) };
		} else {
			Value[] avals = a.getAll();
			int aOk = findUnknown(avals);
			int aErr = findError(avals);
			int ax = getKnown(avals);
			Value[] bvals = b.getAll();
			int bOk = findUnknown(bvals);
			int bErr = findError(bvals);
			int bx = getKnown(bvals);
			Value[] cvals = c_in.getAll();
			int cOk = findUnknown(cvals);
			int cErr = findError(cvals);
			int cx = getKnown(cvals);
			
			int known = Math.min(Math.min(aOk, bOk), cOk);
			int error = Math.min(Math.min(aErr, bErr), cErr);
			int ret = ax * bx + cx;

			Value[] bits = new Value[w];
			for (int i = 0; i < w; i++) {
				if (i < known) {
					bits[i] = ((ret & (1 << i)) != 0 ? Value.TRUE : Value.FALSE);
				} else if (i < error) {
					bits[i] = Value.UNKNOWN;
				} else {
					bits[i] = Value.ERROR;
				}
			}
			return new Value[] { Value.create(bits),
					error < w ? Value.createError(width) : Value.createUnknown(width) };
		}
	}
	
	private static int findUnknown(Value[] vals) {
		for (int i = 0; i < vals.length; i++) {
			if (!vals[i].isFullyDefined()) return i;
		}
		return vals.length;
	}
	
	private static int findError(Value[] vals) {
		for (int i = 0; i < vals.length; i++) {
			if (vals[i].isErrorValue()) return i;
		}
		return vals.length;
	}
	
	private static int getKnown(Value[] vals) {
		int ret = 0;
		for (int i = 0; i < vals.length; i++) {
			int val = vals[i].toIntValue();
			if (val < 0) return ret;
			ret |= val << i;
		}
		return ret;
	}
}
