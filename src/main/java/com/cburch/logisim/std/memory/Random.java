/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringUtil;
import static com.cburch.logisim.util.LocaleString.*;

public class Random extends InstanceFactory {
	private static final Attribute<Integer> ATTR_SEED
		= Attributes.forInteger("seed", __("randomSeedAttr"));
	
	private static final int OUT = 0;
	private static final int CK  = 1;
	private static final int NXT = 2;
	private static final int RST = 3;

	public Random() {
		super("Random", __("randomComponent"));
		setAttributes(new Attribute[] {
				StdAttr.WIDTH, ATTR_SEED, StdAttr.EDGE_TRIGGER,
				StdAttr.LABEL, StdAttr.LABEL_FONT
			}, new Object[] {
				BitWidth.create(8), Integer.valueOf(0), StdAttr.TRIG_RISING,
				"", StdAttr.DEFAULT_LABEL_FONT
			});
		setKeyConfigurator(new BitWidthConfigurator(StdAttr.WIDTH));

		setOffsetBounds(Bounds.create(-30, -20, 30, 40));
		setIconName("random.gif");
		setInstanceLogger(Logger.class);
		
		Port[] ps = new Port[4];
		ps[OUT] = new Port(  0,   0, Port.OUTPUT, StdAttr.WIDTH);
		ps[CK]  = new Port(-30, -10, Port.INPUT, 1);
		ps[NXT] = new Port(-30,  10, Port.INPUT, 1);
		ps[RST] = new Port(-20,  20, Port.INPUT, 1);
		ps[OUT].setToolTip(__("randomQTip"));
		ps[CK].setToolTip(__("randomClockTip"));
		ps[NXT].setToolTip(__("randomNextTip"));
		ps[RST].setToolTip(__("randomResetTip"));
		setPorts(ps);
	}
	
	@Override
	protected void configureNewInstance(Instance instance) {
		Bounds bds = instance.getBounds();
		instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT,
				bds.getX() + bds.getWidth() / 2, bds.getY() - 3,
				GraphicsUtil.H_CENTER, GraphicsUtil.V_BASELINE);
	}

	@Override
	public void propagate(InstanceState state) {
		StateData data = (StateData) state.getData();
		if (data == null) {
			data = new StateData(state.getAttributeValue(ATTR_SEED));
			state.setData(data);
		}

		BitWidth dataWidth = state.getAttributeValue(StdAttr.WIDTH);
		Object triggerType = state.getAttributeValue(StdAttr.EDGE_TRIGGER);
		boolean triggered = data.updateClock(state.getPort(CK), triggerType);

		if (state.getPort(RST) == Value.TRUE) {
			data.reset(state.getAttributeValue(ATTR_SEED));
		} else if (triggered && state.getPort(NXT) != Value.FALSE) {
			data.step();
		} 

		state.setPort(OUT, Value.createKnown(dataWidth, data.value), 4);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		Bounds bds = painter.getBounds();
		StateData state = (StateData) painter.getData();
		BitWidth widthVal = painter.getAttributeValue(StdAttr.WIDTH);
		int width = widthVal == null ? 8 : widthVal.getWidth();

		// draw boundary, label
		painter.drawBounds();
		painter.drawLabel();

		// draw input and output ports
		painter.drawPort(OUT, "Q", Direction.WEST);
		painter.drawPort(RST);
		painter.drawPort(NXT);
		painter.drawClock(CK, Direction.EAST);

		// draw contents
		if (painter.getShowState()) {
			int val = state == null ? 0 : state.value;
			String str = StringUtil.toHexString(width, val);
			if (str.length() <= 4) {
				GraphicsUtil.drawText(g, str,
						bds.getX() + 15, bds.getY() + 4,
						GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
			} else {
				int split = str.length() - 4;
				GraphicsUtil.drawText(g, str.substring(0, split),
						bds.getX() + 15, bds.getY() + 3,
						GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
				GraphicsUtil.drawText(g, str.substring(split),
						bds.getX() + 15, bds.getY() + 15,
						GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
			}
		}
	}
	
	private static class StateData extends ClockState implements InstanceData {
		private final static long multiplier = 0x5DEECE66DL;
		private final static long addend = 0xBL;
		private final static long mask = (1L << 48) - 1;

		private long initSeed;
		private long curSeed;
		private int value;
	
		public StateData(Object seed) {
			reset(seed);
		}
		
		void reset(Object seed) {
			long start = seed instanceof Integer ? ((Integer) seed).intValue() : 0;
			if (start == 0) {
				// Prior to 2.7.0, this would reset to the seed at the time of
				// the StateData's creation. It seems more likely that what
				// would be intended was starting a new sequence entirely...
				start = (System.currentTimeMillis() ^ multiplier) & mask;
				if (start == initSeed) {
					start = (start + multiplier) & mask;
				}
			}
			this.initSeed = start;
			this.curSeed = start;
			this.value = (int) start;
		}
		
		void step() {
			long v = curSeed;
			v = (v * multiplier + addend) & mask;
			curSeed = v;
			value = (int) (v >> 12);
		}
	}
	
	public static class Logger extends InstanceLogger {
		@Override
		public String getLogName(InstanceState state, Object option) {
			String ret = state.getAttributeValue(StdAttr.LABEL);
			return ret != null && !ret.equals("") ? ret : null;
		}
	
		@Override
		public Value getLogValue(InstanceState state, Object option) {
			BitWidth dataWidth = state.getAttributeValue(StdAttr.WIDTH);
			if (dataWidth == null) dataWidth = BitWidth.create(0);
			StateData data = (StateData) state.getData();
			if (data == null) return Value.createKnown(dataWidth, 0);
			return Value.createKnown(dataWidth, data.value);
		}
	}
}