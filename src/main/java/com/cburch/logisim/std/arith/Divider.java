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

public class Divider extends InstanceFactory {
	static final int PER_DELAY = 1;

	private static final int IN0   = 0;
	private static final int IN1   = 1;
	private static final int OUT   = 2;
	private static final int UPPER = 3;
	private static final int REM   = 4;

	public Divider() {
		super("Divider", __("dividerComponent"));
		setAttributes(new Attribute[] { StdAttr.WIDTH },
				new Object[] { BitWidth.create(8) });
		setKeyConfigurator(new BitWidthConfigurator(StdAttr.WIDTH));
		setOffsetBounds(Bounds.create(-40, -20, 40, 40));
		setIconName("divider.gif");
		
		Port[] ps = new Port[5];
		ps[IN0]   = new Port(-40, -10, Port.INPUT,  StdAttr.WIDTH);
		ps[IN1]   = new Port(-40,  10, Port.INPUT,  StdAttr.WIDTH);
		ps[OUT]   = new Port(  0,   0, Port.OUTPUT, StdAttr.WIDTH);
		ps[UPPER] = new Port(-20, -20, Port.INPUT,  StdAttr.WIDTH);
		ps[REM]   = new Port(-20,  20, Port.OUTPUT, StdAttr.WIDTH);
		ps[IN0].setToolTip(__("dividerDividendLowerTip"));
		ps[IN1].setToolTip(__("dividerDivisorTip"));
		ps[OUT].setToolTip(__("dividerOutputTip"));
		ps[UPPER].setToolTip(__("dividerDividendUpperTip"));
		ps[REM].setToolTip(__("dividerRemainderTip"));
		setPorts(ps);
	}

	@Override
	public void propagate(InstanceState state) {
		// get attributes
		BitWidth dataWidth = state.getAttributeValue(StdAttr.WIDTH);

		// compute outputs
		Value a = state.getPort(IN0);
		Value b = state.getPort(IN1);
		Value upper = state.getPort(UPPER);
		Value[] outs = Divider.computeResult(dataWidth, a, b, upper);

		// propagate them
		int delay = dataWidth.getWidth() * (dataWidth.getWidth() + 2) * PER_DELAY;
		state.setPort(OUT, outs[0], delay);
		state.setPort(REM, outs[1], delay);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		painter.drawBounds();

		g.setColor(Color.GRAY);
		painter.drawPort(IN0);
		painter.drawPort(IN1);
		painter.drawPort(OUT);
		painter.drawPort(UPPER, _("dividerUpperInput"),  Direction.NORTH);
		painter.drawPort(REM, _("dividerRemainderOutput"), Direction.SOUTH);

		Location loc = painter.getLocation();
		int x = loc.getX();
		int y = loc.getY();
		GraphicsUtil.switchToWidth(g, 2);
		g.setColor(Color.BLACK);
		g.fillOval(x - 12, y - 7, 4, 4);
		g.drawLine(x - 15, y, x - 5, y);
		g.fillOval(x - 12, y + 3, 4, 4);
		GraphicsUtil.switchToWidth(g, 1);
	}

	static Value[] computeResult(BitWidth width, Value a, Value b, Value upper) {
		int w = width.getWidth();
		if (upper == Value.NIL || upper.isUnknown()) upper = Value.createKnown(width, 0);
		if (a.isFullyDefined() && b.isFullyDefined() && upper.isFullyDefined()) {
			long num = ((long) upper.toIntValue() << w)
				| ((long) a.toIntValue() & 0xFFFFFFFFL);
			long den = (long) b.toIntValue() & 0xFFFFFFFFL;
			if (den == 0) den = 1;
			long result = num / den;
			long rem = num % den;
			if (rem < 0) {
				if (den >= 0) {
					rem += den;
					result--;
				} else {
					rem -= den;
					result++;
				}
			}
			return new Value[] { Value.createKnown(width, (int) result),
					Value.createKnown(width, (int) rem) };
		} else if (a.isErrorValue() || b.isErrorValue() || upper.isErrorValue()) {
			return new Value[] { Value.createError(width), Value.createError(width) };
		} else {
			return new Value[] { Value.createUnknown(width), Value.createUnknown(width) };
		}
	}
}
