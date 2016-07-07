/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.*;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringUtil;

import java.awt.*;

import static com.cburch.logisim.util.LocaleString.getFromLocale;

public class Counter extends InstanceFactory {
    static final AttributeOption ON_GOAL_WRAP = new AttributeOption("wrap",
            "wrap", getFromLocale("counterGoalWrap"));
    private static final AttributeOption ON_GOAL_STAY = new AttributeOption("stay",
            "stay", getFromLocale("counterGoalStay"));
    private static final AttributeOption ON_GOAL_CONT = new AttributeOption("continue",
            "continue", getFromLocale("counterGoalContinue"));
    private static final AttributeOption ON_GOAL_LOAD = new AttributeOption("load",
            "load", getFromLocale("counterGoalLoad"));

    static final Attribute<Integer> ATTR_MAX = Attributes.forHexInteger("max",
            getFromLocale("counterMaxAttr"));
    static final Attribute<AttributeOption> ATTR_ON_GOAL = Attributes.forOption("ongoal",
            getFromLocale("counterGoalAttr"),
            new AttributeOption[] { ON_GOAL_WRAP, ON_GOAL_STAY, ON_GOAL_CONT,
                ON_GOAL_LOAD });

    private static final int DELAY = 8;
    private static final int OUT = 0;
    private static final int IN  = 1;
    private static final int CK  = 2;
    private static final int CLR = 3;
    private static final int LD  = 4;
    private static final int CT  = 5;
    private static final int CARRY = 6;

    public Counter() {
        super("Counter", getFromLocale("counterComponent"));
        setOffsetBounds(Bounds.create(-30, -20, 30, 40));
        setIconName("counter.svg");
        setInstancePoker(RegisterPoker.class);
        setInstanceLogger(RegisterLogger.class);
        setKeyConfigurator(new BitWidthConfigurator(StdAttr.WIDTH));

        Port[] ps = new Port[7];
        ps[OUT] = new Port(  0,   0, Port.OUTPUT, StdAttr.WIDTH);
        ps[IN]  = new Port(-30,   0, Port.INPUT, StdAttr.WIDTH);
        ps[CK]  = new Port(-20,  20, Port.INPUT, 1);
        ps[CLR] = new Port(-10,  20, Port.INPUT, 1);
        ps[LD]  = new Port(-30, -10, Port.INPUT, 1);
        ps[CT]  = new Port(-30,  10, Port.INPUT, 1);
        ps[CARRY] = new Port(0,  10, Port.OUTPUT, 1);
        ps[OUT].setToolTip(getFromLocale("counterQTip"));
        ps[IN].setToolTip(getFromLocale("counterDataTip"));
        ps[CK].setToolTip(getFromLocale("counterClockTip"));
        ps[CLR].setToolTip(getFromLocale("counterResetTip"));
        ps[LD].setToolTip(getFromLocale("counterLoadTip"));
        ps[CT].setToolTip(getFromLocale("counterEnableTip"));
        ps[CARRY].setToolTip(getFromLocale("counterCarryTip"));
        setPorts(ps);
    }

    @Override
    public AttributeSet createAttributeSet() {
        return new CounterAttributes();
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
        RegisterData data = (RegisterData) state.getData();
        if (data == null) {
            data = new RegisterData();
            state.setData(data);
        }

        BitWidth dataWidth = state.getAttributeValue(StdAttr.WIDTH);
        Object triggerType = state.getAttributeValue(StdAttr.EDGE_TRIGGER);
        int max = state.getAttributeValue(ATTR_MAX);
        Value clock = state.getPort(CK);
        boolean triggered = data.updateClock(clock, triggerType);

        Value newValue;
        boolean carry;
        if (state.getPort(CLR) == Value.TRUE) {
            newValue = Value.createKnown(dataWidth, 0);
            carry = false;
        } else {
            boolean ld = state.getPort(LD) == Value.TRUE;
            boolean ct = state.getPort(CT) != Value.FALSE;
            int oldVal = data.value;
            int newVal;
            if (!triggered) {
                newVal = oldVal;
            // trigger, enable = 1: should increment or decrement
            } else if (ct) {
                int goal = ld ? 0 : max;
                if (oldVal == goal) {
                    Object onGoal = state.getAttributeValue(ATTR_ON_GOAL);
                    if (onGoal == ON_GOAL_WRAP) {
                        newVal = ld ? max : 0;
                    } else if (onGoal == ON_GOAL_STAY) {
                        newVal = oldVal;
                    } else if (onGoal == ON_GOAL_LOAD) {
                        Value in = state.getPort(IN);
                        newVal = in.isFullyDefined() ? in.toIntValue() : 0;
                        if (newVal > max) {
                            newVal &= max;
                        }

                    } else if (onGoal == ON_GOAL_CONT) {
                        newVal = ld ? oldVal - 1 : oldVal + 1;
                    } else {
                        //OK
                        System.err.println("Invalid goal attribute " + onGoal);
                        newVal = ld ? max : 0;
                    }
                } else {
                    newVal = ld ? oldVal - 1 : oldVal + 1;
                }
            // trigger, enable = 0, load = 1: should load
            } else if (ld) {
                Value in = state.getPort(IN);
                newVal = in.isFullyDefined() ? in.toIntValue() : 0;
                if (newVal > max) {
                    newVal &= max;
                }

            // trigger, enable = 0, load = 0: no change
            } else {
                newVal = oldVal;
            }
            newValue = Value.createKnown(dataWidth, newVal);
            newVal = newValue.toIntValue();
            carry = newVal == (ld && ct ? 0 : max);
            /* I would want this if I were worried about the carry signal
             * outrunning the clock. But the component's delay should be
             * enough to take care of it.
            if (carry) {
                if (triggerType == StdAttr.TRIG_FALLING) {
                    carry = clock == Value.TRUE;
                } else {
                    carry = clock == Value.FALSE;
                }
            }
            */
        }

        data.value = newValue.toIntValue();
        state.setPort(OUT, newValue, DELAY);
        state.setPort(CARRY, carry ? Value.TRUE : Value.FALSE, DELAY);
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        Bounds bds = painter.getBounds();
        RegisterData state = (RegisterData) painter.getData();
        BitWidth widthVal = painter.getAttributeValue(StdAttr.WIDTH);
        int width = widthVal == null ? 8 : widthVal.getWidth();

        // determine text to draw in label
        String a;
        String b = null;
        if (painter.getShowState()) {
            int val = state == null ? 0 : state.value;
            String str = StringUtil.toHexString(width, val);
            if (str.length() <= 4) {
                a = str;
            } else {
                int split = str.length() - 4;
                a = str.substring(0, split);
                b = str.substring(split);
            }
        } else {
            a = getFromLocale("counterLabel");
            b = getFromLocale("registerWidthLabel", String.valueOf(widthVal.getWidth()));
        }

        // draw boundary, label
        painter.drawBounds();
        painter.drawLabel();

        // draw input and output ports
        if (b == null) {
            painter.drawPort(IN,  "D", Direction.EAST);
            painter.drawPort(OUT, "Q", Direction.WEST);
        } else {
            painter.drawPort(IN);
            painter.drawPort(OUT);
        }
        g.setColor(Color.GRAY);
        painter.drawPort(LD);
        painter.drawPort(CARRY);
        painter.drawPort(CLR, "0", Direction.SOUTH);
        painter.drawPort(CT, getFromLocale("counterEnableLabel"), Direction.EAST);
        g.setColor(Color.BLACK);
        painter.drawClock(CK, Direction.NORTH);

        // draw contents
        if (b == null) {
            GraphicsUtil.drawText(g, a, bds.getX() + 15, bds.getY() + 4,
                    GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
        } else {
            GraphicsUtil.drawText(g, a, bds.getX() + 15, bds.getY() + 3,
                    GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
            GraphicsUtil.drawText(g, b, bds.getX() + 15, bds.getY() + 15,
                    GraphicsUtil.H_CENTER, GraphicsUtil.V_TOP);
        }
    }
}