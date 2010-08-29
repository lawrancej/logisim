/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.legacy;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.comp.AbstractComponentFactory;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentEvent;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentUserEvent;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.comp.ManagedComponent;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.AttributeSets;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.tools.AbstractCaret;
import com.cburch.logisim.tools.Caret;
import com.cburch.logisim.tools.Pokable;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringGetter;

public class Register extends AbstractComponentFactory {
	private static final Bounds OFFSET_BOUNDS = Bounds.create(-30, -5, 30, 90);
	private static final int CK  = 16;

	private static class State {
		private int value;
		private Value lastClock;
		private boolean isEditing;

		public State() {
			value = 0;
			lastClock = Value.FALSE;
			isEditing = false;
		}
	}

	private static class CompCaret extends AbstractCaret {
		Comp comp;
		State state;
		int initValue;
		int curValue;
		CircuitState circState;

		CompCaret(Comp comp, State state, CircuitState circState) {
			this.comp = comp;
			this.state = state;
			this.circState = circState;
			initValue = state.value;
			curValue = initValue;
		}

		@Override
		public void stopEditing() {
			state.isEditing = false;
		}

		@Override
		public void cancelEditing() {
			state.value = initValue;
			state.isEditing = false;
		}

		@Override
		public void keyTyped(KeyEvent e) {
			int val = Character.digit(e.getKeyChar(), 16);
			if (val < 0) return;

			curValue = (curValue * 16 + val) & 0xFF;
			state.value = curValue;

			comp.repropagate();
		}
	}

	private class Comp extends ManagedComponent implements Pokable {
		private ComponentFactory factory;
		
		public Comp(ComponentFactory factory, Location loc, AttributeSet attrs) {
			super(loc, attrs, 4);
			this.factory = factory;

			for (int i = 0; i < 8; i++) {
				setEnd(i, loc.translate(-30, 10 * i), BitWidth.ONE, EndData.INPUT_ONLY);
			}
			for (int i = 0; i < 8; i++) {
				setEnd(8 + i, loc.translate(0, 10 * i), BitWidth.ONE, EndData.OUTPUT_ONLY);
			}
			setEnd(CK, loc.translate(-30, 80), BitWidth.ONE, EndData.INPUT_ONLY);
		}
		
		void repropagate() {
			fireComponentInvalidated(new ComponentEvent(this));
		}

		@Override
		public ComponentFactory getFactory() {
			return factory;
		}

		@Override
		public void propagate(CircuitState state) {
			State myState = getState(state);

			Value ckValue  = state.getValue(getEndLocation(CK));
			if (myState.lastClock == Value.FALSE
					&& ckValue == Value.TRUE) {
				int value = 0;
				for (int i = 7; i >= 0; i--) {
					Value inValue = state.getValue(getEndLocation(i));
					value = value * 2 + (inValue == Value.TRUE ? 1 : 0);
				}
				myState.value = value;
			} 

			myState.lastClock = ckValue;

			int val = myState.value;
			for (int i = 0; i < 8; i++) {
				state.setValue(this.getEndLocation(8 + i),
					((val >> i) & 1) == 1 ? Value.TRUE : Value.FALSE,
					this, 8);
			}
		}

		private State getState(CircuitState state) {
			State myState = (State) state.getData(this);
			if (myState == null) {
				myState = new State();
				state.setData(this, myState);
			}
			return myState;
		}

		//
		// user interface methods
		//
		public void draw(ComponentDrawContext context) {
			Graphics g = context.getGraphics();
			Bounds bds = getBounds();
			State state = getState(context.getCircuitState());

			// draw boundary
			context.drawBounds(this);

			// draw input and output pins
			for (int i = 0; i < CK; i++) {
				context.drawPin(this, i);
			}
			context.drawClock(this, CK, Direction.EAST);

			// draw contents
			if (context.getShowState()) {
				GraphicsUtil.drawText(g, toHexString(state.value),
						bds.getX() + 15, bds.getY() + 4,
						GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
				if (state.isEditing) {
					g.setColor(Color.RED);
					g.drawRect(bds.getX() + 5, bds.getY() + 4, 20, 15);
					g.setColor(Color.BLACK);
				}
			}
		}
		
		@Override
		public Object getFeature(Object key) {
			if (key == Pokable.class) return this;
			return super.getFeature(key);
		}
		
		public Caret getPokeCaret(ComponentUserEvent event) {
			Bounds bds = getBounds();
			CircuitState circState = event.getCircuitState();
			State state = getState(circState);
			CompCaret ret = new CompCaret(this, state, circState);
			ret.setBounds(bds);
			state.isEditing = true;
			return ret;
		}
	}

	public Register() { }

	@Override
	public String getName() { return "Logisim 1.0 Register"; }

	@Override
	public StringGetter getDisplayGetter() {
		return Strings.getter("registerComponent");
	}
	
	@Override
	public AttributeSet createAttributeSet() {
		return AttributeSets.EMPTY;
	}

	@Override
	public Component createComponent(Location loc, AttributeSet attrs) {
		return new Comp(this, loc, attrs);
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		return OFFSET_BOUNDS;
	}
	
	//
	// user interface methods
	//
	@Override
	public void paintIcon(ComponentDrawContext context,
			int x, int y, AttributeSet attrs) {
		Graphics g = context.getGraphics();
		Font old = g.getFont();
		g.setFont(old.deriveFont(9.0f));
		GraphicsUtil.drawCenteredText(g, "Reg", x + 10, y + 9);
		g.setFont(old);
		g.drawRect(x, y + 4, 19, 12);
		for (int dx = 2; dx < 20; dx += 5) {
			g.drawLine(x + dx, y +  2, x + dx, y +  4);
			g.drawLine(x + dx, y + 16, x + dx, y + 18);
		}
	}

	private static String toHexString(int value) {
		String ret = Integer.toHexString(value);
		int len = ret.length();
		if (len < 2)      return "0" + ret;
		else if (len > 2) return ret.substring(len - 2);
		else             return ret;
	}
}
