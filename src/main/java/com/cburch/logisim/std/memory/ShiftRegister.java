/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.*;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.tools.key.IntegerConfigurator;
import com.cburch.logisim.tools.key.JoinedConfigurator;
import com.cburch.logisim.util.GraphicsUtil;

import java.awt.*;

import static com.cburch.logisim.util.LocaleString.getFromLocale;

public class ShiftRegister extends InstanceFactory {
    static final Attribute<Integer> ATTR_LENGTH = Attributes.forIntegerRange("length",
            getFromLocale("shiftRegLengthAttr"), 1, 32);
    static final Attribute<Boolean> ATTR_LOAD = Attributes.forBoolean("parallel",
            getFromLocale("shiftRegParallelAttr"));

    private static final int IN  = 0;
    private static final int SH  = 1;
    private static final int CK  = 2;
    private static final int CLR = 3;
    private static final int OUT = 4;
    private static final int LD  = 5;

    public ShiftRegister() {
        super("Shift Register", getFromLocale("shiftRegisterComponent"));
        setAttributes(new Attribute[] {
                StdAttr.WIDTH, ATTR_LENGTH, ATTR_LOAD, StdAttr.EDGE_TRIGGER,
                StdAttr.LABEL, StdAttr.LABEL_FONT
            }, new Object[] {
                BitWidth.ONE, Integer.valueOf(8), Boolean.TRUE,
                StdAttr.TRIG_RISING, "", StdAttr.DEFAULT_LABEL_FONT
            });
        setKeyConfigurator(JoinedConfigurator.create(
                new IntegerConfigurator(ATTR_LENGTH, 1, 32, 0),
                new BitWidthConfigurator(StdAttr.WIDTH)));

        setIconName("shiftreg.svg");
        setInstanceLogger(ShiftRegisterLogger.class);
        setInstancePoker(ShiftRegisterPoker.class);
    }

    @Override
    public Bounds getOffsetBounds(AttributeSet attrs) {
        Object parallel = attrs.getValue(ATTR_LOAD);
        if (parallel == null || (Boolean) parallel) {
            int len = attrs.getValue(ATTR_LENGTH);
            return Bounds.create(0, -20, 20 + 10 * len, 40);
        } else {
            return Bounds.create(0, -20, 30, 40);
        }
    }

    @Override
    protected void configureNewInstance(Instance instance) {
        configurePorts(instance);
        instance.addAttributeListener();
    }

    @Override
    protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
        if (attr == ATTR_LOAD || attr == ATTR_LENGTH || attr == StdAttr.WIDTH) {
            instance.recomputeBounds();
            configurePorts(instance);
        }
    }

    private static void configurePorts(Instance instance) {
        BitWidth widthObj = instance.getAttributeValue(StdAttr.WIDTH);
        int width = widthObj.getWidth();
        Boolean parallelObj = instance.getAttributeValue(ATTR_LOAD);
        Bounds bds = instance.getBounds();
        Port[] ps;
        if (parallelObj == null || parallelObj) {
            Integer lenObj = instance.getAttributeValue(ATTR_LENGTH);
            int len = lenObj == null ? 8 : lenObj;
            ps = new Port[6 + 2 * len];
            ps[LD] = new Port(10, -20, Port.INPUT, 1);
            ps[LD].setToolTip(getFromLocale("shiftRegLoadTip"));
            for (int i = 0; i < len; i++) {
                ps[6 + 2 * i]     = new Port(20 + 10 * i, -20, Port.INPUT, width);
                ps[6 + 2 * i + 1] = new Port(20 + 10 * i,  20, Port.OUTPUT, width);
            }
        } else {
            ps = new Port[5];
        }
        ps[OUT] = new Port(bds.getWidth(), 0, Port.OUTPUT, width);
        ps[SH]  = new Port( 0, -10, Port.INPUT, 1);
        ps[IN]  = new Port( 0,   0, Port.INPUT, width);
        ps[CK]  = new Port( 0,  10, Port.INPUT, 1);
        ps[CLR] = new Port(10,  20, Port.INPUT, 1);
        ps[OUT].setToolTip(getFromLocale("shiftRegOutTip"));
        ps[SH].setToolTip(getFromLocale("shiftRegShiftTip"));
        ps[IN].setToolTip(getFromLocale("shiftRegInTip"));
        ps[CK].setToolTip(getFromLocale("shiftRegClockTip"));
        ps[CLR].setToolTip(getFromLocale("shiftRegClearTip"));
        instance.setPorts(ps);

        instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT,
                bds.getX() + bds.getWidth() / 2,
                bds.getY() + bds.getHeight() / 4,
                GraphicsUtil.H_CENTER, GraphicsUtil.V_CENTER);
    }

    private static ShiftRegisterData getData(InstanceState state) {
        BitWidth width = state.getAttributeValue(StdAttr.WIDTH);
        Integer lenObj = state.getAttributeValue(ATTR_LENGTH);
        int length = lenObj == null ? 8 : lenObj;
        ShiftRegisterData data = (ShiftRegisterData) state.getData();
        if (data == null) {
            data = new ShiftRegisterData(width, length);
            state.setData(data);
        } else {
            data.setDimensions(width, length);
        }
        return data;
    }

    @Override
    public void propagate(InstanceState state) {
        Object triggerType = state.getAttributeValue(StdAttr.EDGE_TRIGGER);
        boolean parallel = state.getAttributeValue(ATTR_LOAD);
        ShiftRegisterData data = getData(state);
        int len = data.getLength();

        boolean triggered = data.updateClock(state.getPort(CK), triggerType);
        if (state.getPort(CLR) == Value.TRUE) {
            data.clear();
        } else if (triggered) {
            if (parallel && state.getPort(LD) == Value.TRUE) {
                data.clear();
                for (int i = len - 1; i >= 0; i--) {
                    data.push(state.getPort(6 + 2 * i));
                }
            } else if (state.getPort(SH) != Value.FALSE) {
                data.push(state.getPort(IN));
            }
        }

        state.setPort(OUT, data.get(0), 4);
        if (parallel) {
            for (int i = 0; i < len; i++) {
                state.setPort(6 + 2 * i + 1, data.get(len - 1 - i), 4);
            }
        }
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        // draw boundary, label
        painter.drawBounds();
        painter.drawLabel();

        // draw state
        boolean parallel = painter.getAttributeValue(ATTR_LOAD);
        if (parallel) {
            BitWidth widObj = painter.getAttributeValue(StdAttr.WIDTH);
            int wid = widObj.getWidth();
            Integer lenObj = painter.getAttributeValue(ATTR_LENGTH);
            int len = lenObj == null ? 8 : lenObj;
            if (painter.getShowState()) {
                if (wid <= 4) {
                    ShiftRegisterData data = getData(painter);
                    Bounds bds = painter.getBounds();
                    int x = bds.getX() + 20;
                    int y = bds.getY();
                    Object label = painter.getAttributeValue(StdAttr.LABEL);
                    if (label == null || label.equals("")) {
                        y += bds.getHeight() / 2;
                    } else {
                        y += 3 * bds.getHeight() / 4;
                    }
                    Graphics g = painter.getGraphics();
                    for (int i = 0; i < len; i++) {
                        String s = data.get(len - 1 - i).toHexString();
                        GraphicsUtil.drawCenteredText(g, s, x, y);
                        x += 10;
                    }
                }
            } else {
                Bounds bds = painter.getBounds();
                int x = bds.getX() + bds.getWidth() / 2;
                int y = bds.getY();
                int h = bds.getHeight();
                Graphics g = painter.getGraphics();
                Object label = painter.getAttributeValue(StdAttr.LABEL);
                if (label == null || label.equals("")) {
                    String a = getFromLocale("shiftRegisterLabel1");
                    GraphicsUtil.drawCenteredText(g, a, x, y + h / 4);
                }
                String b = getFromLocale("shiftRegisterLabel2", String.valueOf(len),
                        String.valueOf(wid));
                GraphicsUtil.drawCenteredText(g, b, x, y + 3 * h / 4);
            }
        }

        // draw input and output ports
        int ports = painter.getInstance().getPorts().size();
        for (int i = 0; i < ports; i++) {
            if (i != CK) {
                painter.drawPort(i);
            }

        }
        painter.drawClock(CK, Direction.EAST);
    }
}