/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.legacy;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.comp.AbstractComponentFactory;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentEvent;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentState;
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
import com.cburch.logisim.tools.Caret;
import com.cburch.logisim.tools.CaretListener;
import com.cburch.logisim.tools.Pokable;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringGetter;

public class DFlipFlop extends AbstractComponentFactory {
    private static final Bounds OFFSET_BOUNDS = Bounds.create(-30, -5, 30, 30);
    private static final int D    = 0;
    private static final int CK   = 1;
    private static final int Q    = 2;
    private static final int Qnot = 3;

    private static class StateData implements ComponentState, Cloneable {
        Value lastClock = Value.FALSE;
        Value curValue  = Value.FALSE;
        Location propLocation = null; // so that moved component emits again
        
        @Override
        public Object clone() {
            try { return super.clone(); }
            catch (CloneNotSupportedException e) { return null; }
        }
    }

    private static class PokeCaret implements Caret {
        Comp comp;
        CircuitState state;
        boolean isPressed = true;

        PokeCaret(Comp comp, CircuitState state) {
            this.comp = comp;
            this.state = state;
        }

        public void addCaretListener(CaretListener e) { }
        public void removeCaretListener(CaretListener e) { }

        // query/Graphics methods
        public String getText() { return ""; }
        public Bounds getBounds(Graphics g) {
            return comp.getBounds();
        }
        public void draw(Graphics g) { }

        // finishing
        public void commitText(String text) { }
        public void cancelEditing() { }
        public void stopEditing() { }

        // events to handle
        public void mousePressed(MouseEvent e) {
            isPressed = isInside(e.getX(), e.getY());
        }
        public void mouseDragged(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) {
            if (isPressed && isInside(e.getX(), e.getY())) {
                StateData myState = (StateData) state.getData(comp);
                if (myState == null) return;

                myState.curValue = myState.curValue.not();
                comp.repropagate();
            }
            isPressed = false;
        }
        public void keyPressed(KeyEvent e) { }
        public void keyReleased(KeyEvent e) { }
        public void keyTyped(KeyEvent e) { }

        private boolean isInside(int x, int y) {
            return comp.getBounds().contains(x, y);
        }
    }

    private class Comp extends ManagedComponent implements Pokable {
        private ComponentFactory factory;
        
        public Comp(ComponentFactory factory, Location loc, AttributeSet attrs) {
            super(loc, attrs, 4);
            this.factory = factory;

            Location pt = getLocation();
            BitWidth w = BitWidth.ONE;
            setEnd(D,    pt.translate(-30,  0), w, EndData.INPUT_ONLY);
            setEnd(CK,   pt.translate(-30, 20), w, EndData.INPUT_ONLY);
            setEnd(Q,    pt,                    w, EndData.OUTPUT_ONLY);
            setEnd(Qnot, pt.translate(  0, 20), w, EndData.OUTPUT_ONLY);
        }

        @Override
        public ComponentFactory getFactory() {
            return factory;
        }
        
        void repropagate() {
            fireComponentInvalidated(new ComponentEvent(this));
        }

        @Override
        public void propagate(CircuitState state) {
            boolean changed = false;
            StateData myState = (StateData) state.getData(this);
            if (myState == null) {
                changed = true;
                myState = new StateData();
                state.setData(this, myState);
            }

            Value lastClock = myState.lastClock;
            Value clock = state.getValue(getEndLocation(CK));
            myState.lastClock = clock;

            // it is not being cleared: Check for clock's state
            if (lastClock == Value.FALSE && clock == Value.TRUE) {
                Value newVal = state.getValue(getEndLocation(D));
                changed |= myState.curValue != newVal;
                myState.curValue = newVal;
            }

            Location loc = getLocation();
            if (changed || !loc.equals(myState.propLocation)) {
                myState.propLocation = loc;
                state.setValue(getEndLocation(Q),    myState.curValue,
                        this, 6);
                state.setValue(getEndLocation(Qnot), myState.curValue.not(),
                        this, 6);
            }
        }
        
        //
        // user interface methods
        //
        public void draw(ComponentDrawContext context) {
            Bounds bds = getBounds();
            context.drawRectangle(bds.getX(), bds.getY(),
                bds.getWidth(), bds.getHeight(), "");
            context.drawPin(this, D, "D", Direction.EAST);
            context.drawClock(this, CK, Direction.EAST);
            context.drawPin(this, Q, "Q", Direction.WEST);
            context.drawPin(this, Qnot);
        }
        
        @Override
        public Object getFeature(Object key) {
            if (key == Pokable.class) return this;
            return super.getFeature(key);
        }

        public Caret getPokeCaret(ComponentUserEvent event) {
            if (getBounds().contains(event.getX(), event.getY())) {
                PokeCaret ret = new PokeCaret(this, event.getCircuitState());
                ret.isPressed = ret.isInside(event.getX(), event.getY());
                return ret;
            } else {
                return null;
            }
        }

    }

    public DFlipFlop() { }

    @Override
    public String getName() { return "Logisim 1.0 D Flip-Flop"; }

    @Override
    public StringGetter getDisplayGetter() {
        return Strings.getter("dFlipFlopComponent");
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
    public void drawGhost(ComponentDrawContext context,
            Color color, int x, int y, AttributeSet attrs) {
        Graphics g = context.getGraphics();
        g.setColor(color);
        Bounds bds = OFFSET_BOUNDS;
        context.drawRectangle(this,
            x + bds.getX(), y + bds.getY(),
            bds.getWidth(), bds.getHeight(),
            "");
    }

    @Override
    public void paintIcon(ComponentDrawContext context,
            int x, int y, AttributeSet attrs) {
        Graphics g = context.getGraphics();
        g.drawRect(x + 2, y + 2, 16, 16);
        GraphicsUtil.drawCenteredText(g, "D", x + 10, y + 8);
    }

}
