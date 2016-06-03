/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.plexers;

import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.*;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.util.GraphicsUtil;

import java.awt.*;

import static com.cburch.logisim.util.LocaleString.getFromLocale;

class PriorityEncoder extends InstanceFactory {
    private static final int OUT = 0;
    private static final int EN_IN = 1;
    private static final int EN_OUT = 2;
    private static final int GS = 3;

    public PriorityEncoder() {
        super("Priority Encoder", getFromLocale("priorityEncoderComponent"));
        setAttributes(new Attribute[] {
                StdAttr.FACING, Plexers.ATTR_SELECT, Plexers.ATTR_DISABLED
            }, new Object[] {
                Direction.EAST, BitWidth.create(3), Plexers.DISABLED_FLOATING
            });
        setKeyConfigurator(new BitWidthConfigurator(Plexers.ATTR_SELECT, 1, 5, 0));
        setIconName("priencod.svg");
        setFacingAttribute(StdAttr.FACING);
    }

    @Override
    public Bounds getOffsetBounds(AttributeSet attrs) {
        Direction dir = attrs.getValue(StdAttr.FACING);
        BitWidth select = attrs.getValue(Plexers.ATTR_SELECT);
        int inputs = 1 << select.getWidth();
        int offs = -5 * inputs;
        int len = 10 * inputs + 10;
        if (dir == Direction.NORTH) {
            return Bounds.create(offs,   0, len, 40);
        } else if (dir == Direction.SOUTH) {
            return Bounds.create(offs, -40, len, 40);
        } else if (dir == Direction.WEST) {
            return Bounds.create(  0, offs, 40, len);
        // dir == Direction.EAST
        } else {
            return Bounds.create(-40, offs, 40, len);
        }
    }

    @Override
    protected void configureNewInstance(Instance instance) {
        instance.addAttributeListener();
        updatePorts(instance);
    }

    @Override
    protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
        if (attr == StdAttr.FACING || attr == Plexers.ATTR_SELECT) {
            instance.recomputeBounds();
            updatePorts(instance);
        } else if (attr == Plexers.ATTR_SELECT) {
            updatePorts(instance);
        } else if (attr == Plexers.ATTR_DISABLED) {
            instance.fireInvalidated();
        }
    }

    private static void updatePorts(Instance instance) {
        Object dir = instance.getAttributeValue(StdAttr.FACING);
        BitWidth select = instance.getAttributeValue(Plexers.ATTR_SELECT);
        int n = 1 << select.getWidth();
        Port[] ps = new Port[n + 4];
        if (dir == Direction.NORTH || dir == Direction.SOUTH) {
            int x = -5 * n + 10;
            int y = dir == Direction.NORTH ? 40 : -40;
            for (int i = 0; i < n; i++) {
                ps[i] = new Port(x + 10 * i, y, Port.INPUT, 1);
            }
            ps[n + OUT] = new Port(0, 0, Port.OUTPUT, select.getWidth());
            ps[n + EN_IN] = new Port(x + 10 * n, y / 2, Port.INPUT, 1);
            ps[n + EN_OUT] = new Port(x - 10, y / 2, Port.OUTPUT, 1);
            ps[n + GS] = new Port(10, 0, Port.OUTPUT, 1);
        } else {
            int x = dir == Direction.EAST ? -40 : 40;
            int y = -5 * n + 10;
            for (int i = 0; i < n; i++) {
                ps[i] = new Port(x, y + 10 * i, Port.INPUT, 1);
            }
            ps[n + OUT] = new Port(0, 0, Port.OUTPUT, select.getWidth());
            ps[n + EN_IN] = new Port(x / 2, y + 10 * n, Port.INPUT, 1);
            ps[n + EN_OUT] = new Port(x / 2, y - 10, Port.OUTPUT, 1);
            ps[n + GS] = new Port(0, 10, Port.OUTPUT, 1);
        }

        for (int i = 0; i < n; i++) {
            ps[i].setToolTip(getFromLocale("priorityEncoderInTip", String.valueOf(i)));
        }
        ps[n + OUT].setToolTip(getFromLocale("priorityEncoderOutTip"));
        ps[n + EN_IN].setToolTip(getFromLocale("priorityEncoderEnableInTip"));
        ps[n + EN_OUT].setToolTip(getFromLocale("priorityEncoderEnableOutTip"));
        ps[n + GS].setToolTip(getFromLocale("priorityEncoderGroupSignalTip"));

        instance.setPorts(ps);
    }

    @Override
    public void propagate(InstanceState state) {
        BitWidth select = state.getAttributeValue(Plexers.ATTR_SELECT);
        int n = 1 << select.getWidth();
        boolean enabled = state.getPort(n + EN_IN) != Value.FALSE;

        int out = -1;
        Value outDefault;
        if (enabled) {
            outDefault = Value.createUnknown(select);
            for (int i = n - 1; i >= 0; i--) {
                if (state.getPort(i) == Value.TRUE) {
                    out = i;
                    break;
                }
            }
        } else {
            Object opt = state.getAttributeValue(Plexers.ATTR_DISABLED);
            Value base = opt == Plexers.DISABLED_ZERO ? Value.FALSE : Value.UNKNOWN;
            outDefault = Value.repeat(base, select.getWidth());
        }
        if (out < 0) {
            state.setPort(n + OUT, outDefault, Plexers.DELAY);
            state.setPort(n + EN_OUT, enabled ? Value.TRUE : Value.FALSE, Plexers.DELAY);
            state.setPort(n + GS, Value.FALSE, Plexers.DELAY);
        } else {
            state.setPort(n + OUT, Value.createKnown(select, out), Plexers.DELAY);
            state.setPort(n + EN_OUT, Value.FALSE, Plexers.DELAY);
            state.setPort(n + GS, Value.TRUE, Plexers.DELAY);
        }
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        Direction facing = painter.getAttributeValue(StdAttr.FACING);

        painter.drawBounds();
        Bounds bds = painter.getBounds();
        g.setColor(Color.GRAY);
        int x0;
        int y0;
        int halign;
        if (facing == Direction.WEST) {
            x0 = bds.getX() + bds.getWidth() - 3;
            y0 = bds.getY() + 15;
            halign = GraphicsUtil.H_RIGHT;
        } else if (facing == Direction.NORTH) {
            x0 = bds.getX() + 10;
            y0 = bds.getY() + bds.getHeight() - 2;
            halign = GraphicsUtil.H_CENTER;
        } else if (facing == Direction.SOUTH) {
            x0 = bds.getX() + 10;
            y0 = bds.getY() + 12;
            halign = GraphicsUtil.H_CENTER;
        } else {
            x0 = bds.getX() + 3;
            y0 = bds.getY() + 15;
            halign = GraphicsUtil.H_LEFT;
        }
        GraphicsUtil.drawText(g, "0", x0, y0, halign, GraphicsUtil.V_BASELINE);
        g.setColor(Color.BLACK);
        GraphicsUtil.drawCenteredText(g, "Pri",
                bds.getX() + bds.getWidth() / 2,
                bds.getY() + bds.getHeight() / 2);
        painter.drawPorts();
    }
}

