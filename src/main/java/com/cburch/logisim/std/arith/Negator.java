/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.arith;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import static com.cburch.logisim.util.LocaleString.*;

public class Negator extends InstanceFactory {
	private static final int IN    = 0;
	private static final int OUT   = 1;

	public Negator() {
		super("Negator", __("negatorComponent"));
		setAttributes(new Attribute[] { StdAttr.WIDTH },
					new Object[] { BitWidth.create(8) });
		setKeyConfigurator(new BitWidthConfigurator(StdAttr.WIDTH));
		setOffsetBounds(Bounds.create(-40, -20, 40, 40));
		setIconName("negator.gif");
		
		Port[] ps = new Port[2];
		ps[IN]  = new Port(-40,  0, Port.INPUT,  StdAttr.WIDTH);
		ps[OUT] = new Port(  0,  0, Port.OUTPUT, StdAttr.WIDTH);
		ps[IN].setToolTip(__("negatorInputTip"));
		ps[OUT].setToolTip(__("negatorOutputTip"));
		setPorts(ps);
	}

	@Override
	public void propagate(InstanceState state) {
		// get attributes
		BitWidth dataWidth = state.getAttributeValue(StdAttr.WIDTH);

		// compute outputs
		Value in = state.getPort(IN);
		Value out;
		if (in.isFullyDefined()) {
			out = Value.createKnown(in.getBitWidth(), -in.toIntValue());
		} else {
			Value[] bits = in.getAll();
			Value fill = Value.FALSE;
			int pos = 0;
			while (pos < bits.length) {
				if (bits[pos] == Value.FALSE) {
					bits[pos] = fill;
				} else if (bits[pos] == Value.TRUE) {
					if (fill != Value.FALSE) bits[pos] = fill;
					pos++;
					break;
				} else if (bits[pos] == Value.ERROR) {
					fill = Value.ERROR;
				} else {
					if (fill == Value.FALSE) fill = bits[pos];
					else bits[pos] = fill;
				}
				pos++;
			}
			while (pos < bits.length) {
				if (bits[pos] == Value.TRUE) {
					bits[pos] = Value.FALSE;
				} else if (bits[pos] == Value.FALSE) {
					bits[pos] = Value.TRUE;
				}
				pos++;
			}
			out = Value.create(bits);
		}

		// propagate them
		int delay = (dataWidth.getWidth() + 2) * Adder.PER_DELAY;
		state.setPort(OUT, out, delay);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		painter.drawBounds();
		painter.drawPort(IN);
		painter.drawPort(OUT, "-x", Direction.WEST);
	}
}
