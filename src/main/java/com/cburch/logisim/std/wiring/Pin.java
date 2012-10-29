/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.wiring;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.RadixOption;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.tools.key.DirectionConfigurator;
import com.cburch.logisim.tools.key.JoinedConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.Icons;
import static com.cburch.logisim.util.LocaleString.*;

public class Pin extends InstanceFactory {
	public static final Attribute<Boolean> ATTR_TRISTATE
		= Attributes.forBoolean("tristate", __("pinThreeStateAttr"));
	public static final Attribute<Boolean> ATTR_TYPE
		= Attributes.forBoolean("output", __("pinOutputAttr"));
	public static final Attribute<Direction> ATTR_LABEL_LOC
		= Attributes.forDirection("labelloc", __("pinLabelLocAttr"));
	
	public static final AttributeOption PULL_NONE
		= new AttributeOption("none", __("pinPullNoneOption"));
	public static final AttributeOption PULL_UP
		= new AttributeOption("up", __("pinPullUpOption"));
	public static final AttributeOption PULL_DOWN
		= new AttributeOption("down", __("pinPullDownOption"));
	public static final Attribute<AttributeOption> ATTR_PULL
		= Attributes.forOption("pull", __("pinPullAttr"),
				new AttributeOption[] { PULL_NONE, PULL_UP, PULL_DOWN });
	
	public static final Pin FACTORY = new Pin();

	private static final Icon ICON_IN = Icons.getIcon("pinInput.gif");
	private static final Icon ICON_OUT = Icons.getIcon("pinOutput.gif");
	private static final Font ICON_WIDTH_FONT = new Font("SansSerif", Font.BOLD, 9);
	private static final Color ICON_WIDTH_COLOR = Value.WIDTH_ERROR_COLOR.darker();

	public Pin() {
		super("Pin", __("pinComponent"));
		setFacingAttribute(StdAttr.FACING);
		setKeyConfigurator(JoinedConfigurator.create(
			new BitWidthConfigurator(StdAttr.WIDTH),
			new DirectionConfigurator(ATTR_LABEL_LOC, KeyEvent.ALT_DOWN_MASK)));
		setInstanceLogger(PinLogger.class);
		setInstancePoker(PinPoker.class);
	}

	@Override
	public AttributeSet createAttributeSet() {
		return new PinAttributes();
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction facing = attrs.getValue(StdAttr.FACING);
		BitWidth width = attrs.getValue(StdAttr.WIDTH);
		return Probe.getOffsetBounds(facing, width, RadixOption.RADIX_2);
	}
	
	//
	// graphics methods
	//
	@Override
	public void paintIcon(InstancePainter painter) {
		paintIconBase(painter);
		BitWidth w = painter.getAttributeValue(StdAttr.WIDTH);
		if (!w.equals(BitWidth.ONE)) {
			Graphics g = painter.getGraphics();
			g.setColor(ICON_WIDTH_COLOR);
			g.setFont(ICON_WIDTH_FONT);
			GraphicsUtil.drawCenteredText(g, "" + w.getWidth(), 10, 9);
			g.setColor(Color.BLACK);
		}
	}

	private void paintIconBase(InstancePainter painter) {
		PinAttributes attrs = (PinAttributes) painter.getAttributeSet();
		Direction dir = attrs.facing;
		boolean output = attrs.isOutput();
		Graphics g = painter.getGraphics();
		if (output) {
			if (ICON_OUT != null) {
				Icons.paintRotated(g, 2, 2, dir, ICON_OUT,
						painter.getDestination());
				return;
			}
		} else {
			if (ICON_IN != null) {
				Icons.paintRotated(g, 2, 2, dir, ICON_IN,
						painter.getDestination());
				return;
			}
		}
		int pinx = 16; int piny = 9;
		if (dir == Direction.EAST) { // keep defaults
		} else if (dir == Direction.WEST) { pinx = 4;
		} else if (dir == Direction.NORTH) { pinx = 9; piny = 4;
		} else if (dir == Direction.SOUTH) { pinx = 9; piny = 16;
		}

		g.setColor(Color.black);
		if (output) {
			g.drawOval(4, 4, 13, 13);
		} else {
			g.drawRect(4, 4, 13, 13);
		}
		g.setColor(Value.TRUE.getColor());
		g.fillOval(7, 7,  8,  8);
		g.fillOval(pinx, piny, 3, 3);
	}

	@Override
	public void paintGhost(InstancePainter painter) {
		PinAttributes attrs = (PinAttributes) painter.getAttributeSet();
		Location loc = painter.getLocation();
		Bounds bds = painter.getOffsetBounds();
		int x = loc.getX();
		int y = loc.getY();
		Graphics g = painter.getGraphics();
		GraphicsUtil.switchToWidth(g, 2);
		boolean output = attrs.isOutput();
		if (output) {
			BitWidth width = attrs.getValue(StdAttr.WIDTH);
			if (width == BitWidth.ONE) {
				g.drawOval(x + bds.getX() + 1, y + bds.getY() + 1,
					bds.getWidth() - 1, bds.getHeight() - 1);
			} else {
				g.drawRoundRect(x + bds.getX() + 1, y + bds.getY() + 1,
					bds.getWidth() - 1, bds.getHeight() - 1, 6, 6);
			}
		} else {
			g.drawRect(x + bds.getX() + 1, y + bds.getY() + 1,
				bds.getWidth() - 1, bds.getHeight() - 1);
		}
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		PinAttributes attrs = (PinAttributes) painter.getAttributeSet();
		Graphics g = painter.getGraphics();
		Bounds bds = painter.getInstance().getBounds(); // intentionally with no graphics object - we don't want label included
		int x = bds.getX();
		int y = bds.getY();
		GraphicsUtil.switchToWidth(g, 2);
		g.setColor(Color.black);
		if (attrs.type == EndData.OUTPUT_ONLY) {
			if (attrs.width.getWidth() == 1) {
				g.drawOval(x + 1, y + 1,
					bds.getWidth() - 1, bds.getHeight() - 1);
			} else {
				g.drawRoundRect(x + 1, y + 1,
					bds.getWidth() - 1, bds.getHeight() - 1, 6, 6);
			}
		} else {
			g.drawRect(x + 1, y + 1,
				bds.getWidth() - 1, bds.getHeight() - 1);
		}
	
		painter.drawLabel();
	
		if (!painter.getShowState()) {
			g.setColor(Color.BLACK);
			GraphicsUtil.drawCenteredText(g, "x" + attrs.width.getWidth(),
					bds.getX() + bds.getWidth() / 2, bds.getY() + bds.getHeight() / 2);
		} else {
			PinState state = getState(painter);
			if (attrs.width.getWidth() <= 1) {
				Value found = state.foundValue;
				g.setColor(found.getColor());
				g.fillOval(x + 4, y + 4, 13, 13);
	
				if (attrs.width.getWidth() == 1) {
					g.setColor(Color.WHITE);
					GraphicsUtil.drawCenteredText(g,
						state.intendedValue.toDisplayString(), x + 11, y + 9);
				}
			} else {
				Probe.paintValue(painter, state.intendedValue);
			}
		}
	
		painter.drawPorts();
	}
	
	//
	// methods for instances
	//
	@Override
	protected void configureNewInstance(Instance instance) {
		PinAttributes attrs = (PinAttributes) instance.getAttributeSet();
		instance.addAttributeListener();
		configurePorts(instance);
		Probe.configureLabel(instance, attrs.labelloc, attrs.facing);
	}
	
	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == ATTR_TYPE) {
			configurePorts(instance);
		} else if (attr == StdAttr.WIDTH || attr == StdAttr.FACING
				|| attr == Pin.ATTR_LABEL_LOC) {
			instance.recomputeBounds();
			PinAttributes attrs = (PinAttributes) instance.getAttributeSet();
			Probe.configureLabel(instance, attrs.labelloc, attrs.facing);
		} else if (attr == Pin.ATTR_TRISTATE || attr == Pin.ATTR_PULL) {
			instance.fireInvalidated();
		}
	}
	
	private void configurePorts(Instance instance) {
		PinAttributes attrs = (PinAttributes) instance.getAttributeSet();
		String endType = attrs.isOutput() ? Port.INPUT : Port.OUTPUT;
		Port port = new Port(0, 0, endType, StdAttr.WIDTH);
		if (attrs.isOutput()) {
			port.setToolTip(__("pinOutputToolTip"));
		} else {
			port.setToolTip(__("pinInputToolTip"));
		}
		instance.setPorts(new Port[] { port });
	}
	
	@Override
	public void propagate(InstanceState state) {
		PinAttributes attrs = (PinAttributes) state.getAttributeSet();
		PinState q = getState(state);
		if (attrs.type == EndData.OUTPUT_ONLY) {
			Value found = state.getPort(0);
			q.intendedValue = found;
			q.foundValue = found;
			state.setPort(0, Value.createUnknown(attrs.width), 1);
		} else {
			Value found = state.getPort(0);
			Value toSend = q.intendedValue;

			Object pull = attrs.pull;
			Value pullTo = null;
			if (pull == PULL_DOWN) {
				pullTo = Value.FALSE;
			} else if (pull == PULL_UP) {
				pullTo = Value.TRUE;
			} else if (!attrs.threeState && !state.isCircuitRoot()) {
				pullTo = Value.FALSE;
			}
			if (pullTo != null) {
				toSend = pull2(toSend, attrs.width, pullTo);
				if (state.isCircuitRoot()) {
					q.intendedValue = toSend;
				}
			}
			
			q.foundValue = found;
			if (!toSend.equals(found)) { // ignore if no change
				state.setPort(0, toSend, 1);
			}
		}
	}

	private static Value pull2(Value mod, BitWidth expectedWidth, Value pullTo) {
		if (mod.getWidth() == expectedWidth.getWidth()) {
			Value[] vs = mod.getAll();
			for (int i = 0; i < vs.length; i++) {
				if (vs[i] == Value.UNKNOWN) vs[i] = pullTo;
			}
			return Value.create(vs);
		} else {
			return Value.createKnown(expectedWidth, 0);
		}
	}
	
	//
	// basic information methods
	//
	public BitWidth getWidth(Instance instance) {
		PinAttributes attrs = (PinAttributes) instance.getAttributeSet();
		return attrs.width;
	}

	public int getType(Instance instance) {
		PinAttributes attrs = (PinAttributes) instance.getAttributeSet();
		return attrs.type;
	}

	public boolean isInputPin(Instance instance) {
		PinAttributes attrs = (PinAttributes) instance.getAttributeSet();
		return attrs.type != EndData.OUTPUT_ONLY;
	}
	
	//
	// state information methods
	//
	public Value getValue(InstanceState state) {
		return getState(state).intendedValue;
	}
	
	public void setValue(InstanceState state, Value value) {
		PinAttributes attrs = (PinAttributes) state.getAttributeSet();
		Object pull = attrs.pull;
		
		PinState myState = getState(state);
		if (value == Value.NIL) {
			myState.intendedValue = Value.createUnknown(attrs.width);
		} else {
			Value sendValue;
			if (pull == PULL_NONE || pull == null || value.isFullyDefined()) {
				sendValue = value;
			} else {
				Value[] bits = value.getAll();
				if (pull == PULL_UP) {
					for (int i = 0; i < bits.length; i++) {
						if (bits[i] != Value.FALSE) bits[i] = Value.TRUE;
					}
				} else if (pull == PULL_DOWN) {
					for (int i = 0; i < bits.length; i++) {
						if (bits[i] != Value.TRUE) bits[i] = Value.FALSE;
					}
				}
				sendValue = Value.create(bits);
			}
			myState.intendedValue = sendValue;
		}
	}
	
	private static PinState getState(InstanceState state) {
		PinAttributes attrs = (PinAttributes) state.getAttributeSet();
		BitWidth width = attrs.width;
		PinState ret = (PinState) state.getData();
		if (ret == null) {
			Value val = attrs.threeState ? Value.UNKNOWN : Value.FALSE;
			if (width.getWidth() > 1) {
				Value[] arr = new Value[width.getWidth()];
				java.util.Arrays.fill(arr, val);
				val = Value.create(arr);
			}
			ret = new PinState(val, val);
			state.setData(ret);
		}
		if (ret.intendedValue.getWidth() != width.getWidth()) {
			ret.intendedValue = ret.intendedValue.extendWidth(width.getWidth(),
					attrs.threeState ? Value.UNKNOWN : Value.FALSE);
		}
		if (ret.foundValue.getWidth() != width.getWidth()) {
			ret.foundValue = ret.foundValue.extendWidth(width.getWidth(), Value.UNKNOWN);
		}
		return ret;
	}

	private static class PinState implements InstanceData, Cloneable {
		Value intendedValue;
		Value foundValue;
		
		public PinState(Value sending, Value receiving) {
			this.intendedValue = sending;
			this.foundValue = receiving;
		}
		
		@Override
		public Object clone() {
			try { return super.clone(); }
			catch (CloneNotSupportedException e) { return null; }
		}
	}
	
	public static class PinPoker extends InstancePoker {
		int bitPressed = -1;

		@Override
		public void mousePressed(InstanceState state, MouseEvent e) {
			bitPressed = getBit(state, e);
		}
		
		@Override
		public void mouseReleased(InstanceState state, MouseEvent e) {
			int bit = getBit(state, e);
			if (bit == bitPressed && bit >= 0) {
				handleBitPress(state, bit, e);
			}
			bitPressed = -1;
		}
		
		private void handleBitPress(InstanceState state, int bit, MouseEvent e) {
			PinAttributes attrs = (PinAttributes) state.getAttributeSet();
			if (!attrs.isInput()) return;
	
			java.awt.Component sourceComp = e.getComponent();
			if (sourceComp instanceof Canvas && !state.isCircuitRoot()) {
				Canvas canvas = (Canvas) e.getComponent();
				CircuitState circState = canvas.getCircuitState();
				java.awt.Component frame = SwingUtilities.getRoot(canvas);
				int choice = JOptionPane.showConfirmDialog(frame,
						_("pinFrozenQuestion"),
						_("pinFrozenTitle"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (choice == JOptionPane.OK_OPTION) {
					circState = circState.cloneState();
					canvas.getProject().setCircuitState(circState);
					state = circState.getInstanceState(state.getInstance());
				} else {
					return;
				}
			}
	
			PinState pinState = getState(state);
			Value val = pinState.intendedValue.get(bit);
			if (val == Value.FALSE) {
				val = Value.TRUE;
			} else if (val == Value.TRUE) {
				val = attrs.threeState && attrs.pull == PULL_NONE ? Value.UNKNOWN : Value.FALSE;
			} else {
				val = Value.FALSE;
			}
			pinState.intendedValue = pinState.intendedValue.set(bit, val);
			state.fireInvalidated();
		}
	
		private int getBit(InstanceState state, MouseEvent e) {
			BitWidth width = state.getAttributeValue(StdAttr.WIDTH);
			if (width.getWidth() == 1) {
				return 0;
			} else {
				Bounds bds = state.getInstance().getBounds(); // intentionally with no graphics object - we don't want label included
				int i = (bds.getX() + bds.getWidth() - e.getX()) / 10;
				int j = (bds.getY() + bds.getHeight() - e.getY()) / 20;
				int bit = 8 * j + i;
				if (bit < 0 || bit >= width.getWidth()) {
					return -1;
				} else {
					return bit;
				}
			}
		}
	}
		
	public static class PinLogger extends InstanceLogger {
		@Override
		public String getLogName(InstanceState state, Object option) {
			PinAttributes attrs = (PinAttributes) state.getAttributeSet();
			String ret = attrs.label;
			if (ret == null || ret.equals("")) {
				String type = attrs.type == EndData.INPUT_ONLY
					? _("pinInputName") : _("pinOutputName");
				return type + state.getInstance().getLocation();
			} else {
				return ret;
			}
		}
		
		@Override
		public Value getLogValue(InstanceState state, Object option) {
			PinState s = getState(state);
			return s.intendedValue;
		}
	}
}