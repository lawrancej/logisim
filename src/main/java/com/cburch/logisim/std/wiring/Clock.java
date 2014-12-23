/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.wiring;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.Icon;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.RadixOption;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.Icons;
import static com.cburch.logisim.util.LocaleString.*;

public class Clock extends InstanceFactory {
    public static final Attribute<Integer> ATTR_HIGH
        = new DurationAttribute("highDuration", getFromLocale("clockHighAttr"),
                1, Integer.MAX_VALUE);

    public static final Attribute<Integer> ATTR_LOW
        = new DurationAttribute("lowDuration", getFromLocale("clockLowAttr"),
                1, Integer.MAX_VALUE);

    public static final Clock FACTORY = new Clock();

    private static final Icon toolIcon = Icons.getIcon("clock.svg");

    private static class ClockState implements InstanceData, Cloneable {
        Value sending = Value.FALSE;
        int clicks = 0;

        @Override
        public ClockState clone() {
            try { return (ClockState) super.clone(); }
            catch (CloneNotSupportedException e) { return null; }
        }
    }

    public static class ClockLogger extends InstanceLogger {
        @Override
        public String getLogName(InstanceState state, Object option) {
            return state.getAttributeValue(StdAttr.LABEL);
        }

        @Override
        public Value getLogValue(InstanceState state, Object option) {
            ClockState s = getState(state);
            return s.sending;
        }
    }

    public static class ClockPoker extends InstancePoker {
        boolean isPressed = true;

        @Override
        public void mousePressed(InstanceState state, MouseEvent e) {
            isPressed = isInside(state, e);
        }

        @Override
        public void mouseReleased(InstanceState state, MouseEvent e) {
            if (isPressed && isInside(state, e)) {
                ClockState myState = (ClockState) state.getData();
                myState.sending = myState.sending.not();
                myState.clicks++;
                state.fireInvalidated();
            }
            isPressed = false;
        }

        private boolean isInside(InstanceState state, MouseEvent e) {
            Bounds bds = state.getInstance().getBounds();
            return bds.contains(e.getX(), e.getY());
        }
    }

    public Clock() {
        super("Clock", getFromLocale("clockComponent"));
        setAttributes(new Attribute[] {
                    StdAttr.FACING, ATTR_HIGH, ATTR_LOW,
                    StdAttr.LABEL, Pin.ATTR_LABEL_LOC, StdAttr.LABEL_FONT
                }, new Object[] {
                    Direction.EAST, Integer.valueOf(1), Integer.valueOf(1),
                    "", Direction.WEST, StdAttr.DEFAULT_LABEL_FONT
                });
        setFacingAttribute(StdAttr.FACING);
        setInstanceLogger(ClockLogger.class);
        setInstancePoker(ClockPoker.class);
    }

    @Override
    public Bounds getOffsetBounds(AttributeSet attrs) {
        return Probe.getOffsetBounds(
                attrs.getValue(StdAttr.FACING),
                BitWidth.ONE, RadixOption.RADIX_2);
    }

    //
    // graphics methods
    //
    @Override
    public void paintIcon(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        if (toolIcon != null) {
            toolIcon.paintIcon(painter.getDestination(), g, 2, 2);
        } else {
            g.drawRect(4, 4, 13, 13);
            g.setColor(Value.FALSE.getColor());
            g.drawPolyline(new int[] { 6, 6, 10, 10, 14, 14 },
                    new int[] { 10, 6, 6, 14, 14, 10 }, 6);
        }

        Direction dir = painter.getAttributeValue(StdAttr.FACING);
        int pinx = 15; int piny = 8;
        // keep defaults
        if (dir == Direction.EAST) {
        } else if (dir == Direction.WEST) { pinx = 3;
        } else if (dir == Direction.NORTH) { pinx = 8; piny = 3;
        } else if (dir == Direction.SOUTH) { pinx = 8; piny = 15;
        }
        g.setColor(Value.TRUE.getColor());
        g.fillOval(pinx, piny, 3, 3);
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        java.awt.Graphics g = painter.getGraphics();
        // intentionally with no graphics object - we don't want label included
        Bounds bds = painter.getInstance().getBounds();
        int x = bds.getX();
        int y = bds.getY();
        GraphicsUtil.switchToWidth(g, 2);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, bds.getWidth(), bds.getHeight());

        painter.drawLabel();

        boolean drawUp;
        if (painter.getShowState()) {
            ClockState state = getState(painter);
            g.setColor(state.sending.getColor());
            drawUp = state.sending == Value.TRUE;
        } else {
            g.setColor(Color.BLACK);
            drawUp = true;
        }
        x += 10;
        y += 10;
        int[] xs = { x - 6, x - 6, x, x, x + 6, x + 6 };
        int[] ys;
        if (drawUp) {
            ys = new int[] { y, y - 4, y - 4, y + 4, y + 4, y };
        } else {
            ys = new int[] { y, y + 4, y + 4, y - 4, y - 4, y };
        }
        g.drawPolyline(xs, ys, xs.length);

        painter.drawPorts();
    }

    //
    // methods for instances
    //
    @Override
    protected void configureNewInstance(Instance instance) {
        instance.addAttributeListener();
        instance.setPorts(new Port[] { new Port(0, 0, Port.OUTPUT, BitWidth.ONE) });
        configureLabel(instance);
    }

    @Override
    protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
        if (attr == Pin.ATTR_LABEL_LOC) {
            configureLabel(instance);
        } else if (attr == StdAttr.FACING) {
            instance.recomputeBounds();
            configureLabel(instance);
        }
    }

    @Override
    public void propagate(InstanceState state) {
        Value val = state.getPort(0);
        ClockState q = getState(state);
        // ignore if no change
        if (!val.equals(q.sending)) {
            state.setPort(0, q.sending, 1);
        }
    }

    //
    // package methods
    //
    public static boolean tick(CircuitState circState, int ticks, Component comp) {
        AttributeSet attrs = comp.getAttributeSet();
        int durationHigh = attrs.getValue(ATTR_HIGH).intValue();
        int durationLow = attrs.getValue(ATTR_LOW).intValue();
        ClockState state = (ClockState) circState.getData(comp);
        if (state == null) {
            state = new ClockState();
            circState.setData(comp, state);
        }
        boolean curValue = ticks % (durationHigh + durationLow) < durationLow;
        if (state.clicks % 2 == 1) {
            curValue = !curValue;
        }

        Value desired = (curValue ? Value.FALSE : Value.TRUE);
        if (!state.sending.equals(desired)) {
            state.sending = desired;
            Instance.getInstanceFor(comp).fireInvalidated();
            return true;
        } else {
            return false;
        }
    }

    //
    // private methods
    //
    private void configureLabel(Instance instance) {
        Direction facing = instance.getAttributeValue(StdAttr.FACING);
        Direction labelLoc = instance.getAttributeValue(Pin.ATTR_LABEL_LOC);
        Probe.configureLabel(instance, labelLoc, facing);
    }

    private static ClockState getState(InstanceState state) {
        ClockState ret = (ClockState) state.getData();
        if (ret == null) {
            ret = new ClockState();
            state.setData(ret);
        }
        return ret;
    }
}
