/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.plexers;

import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.*;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.util.GraphicsUtil;

import java.awt.*;

import static com.cburch.logisim.util.LocaleString.getFromLocale;

class Decoder extends InstanceFactory {
    public Decoder() {
        super("Decoder", getFromLocale("decoderComponent"));
        setAttributes(new Attribute[] {
                StdAttr.FACING, Plexers.ATTR_SELECT_LOC, Plexers.ATTR_SELECT,
                Plexers.ATTR_TRISTATE, Plexers.ATTR_DISABLED, Plexers.ATTR_ENABLE
            }, new Object[] {
                Direction.EAST, Plexers.SELECT_BOTTOM_LEFT, Plexers.DEFAULT_SELECT,
                Plexers.DEFAULT_TRISTATE, Plexers.DISABLED_FLOATING, Boolean.TRUE
            });
        setKeyConfigurator(new BitWidthConfigurator(Plexers.ATTR_SELECT, 1, 5, 0));
        setIconName("decoder.svg");
        setFacingAttribute(StdAttr.FACING);
    }

    @Override
    public Object getDefaultAttributeValue(Attribute<?> attr, LogisimVersion ver) {
        if (attr == Plexers.ATTR_ENABLE) {
            int newer = ver.compareTo(LogisimVersion.get(2, 6, 3, 220));
            return newer >= 0;
        } else {
            return super.getDefaultAttributeValue(attr, ver);
        }
    }

    @Override
    public Bounds getOffsetBounds(AttributeSet attrs) {
        Direction facing = attrs.getValue(StdAttr.FACING);
        Object selectLoc = attrs.getValue(Plexers.ATTR_SELECT_LOC);
        BitWidth select = attrs.getValue(Plexers.ATTR_SELECT);
        int outputs = 1 << select.getWidth();
        Bounds bds;
        boolean reversed = facing == Direction.WEST || facing == Direction.NORTH;
        if (selectLoc == Plexers.SELECT_TOP_RIGHT) {
            reversed = !reversed;
        }

        if (outputs == 2) {
            int y = reversed ? 0 : -40;
            bds = Bounds.create(-20, y, 30, 40);
        } else {
            int x = -20;
            int y = reversed ? -10 : -(outputs * 10 + 10);
            bds = Bounds.create(x, y, 40, outputs * 10 + 20);
        }
        return bds.rotate(Direction.EAST, facing, 0, 0);
    }

    @Override
    public boolean contains(Location loc, AttributeSet attrs) {
        Direction facing = attrs.getValue(StdAttr.FACING).reverse();
        return Plexers.contains(loc, getOffsetBounds(attrs), facing);
    }

    @Override
    protected void configureNewInstance(Instance instance) {
        instance.addAttributeListener();
        updatePorts(instance);
    }

    @Override
    protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
        if (attr == StdAttr.FACING || attr == Plexers.ATTR_SELECT_LOC
                || attr == Plexers.ATTR_SELECT) {
            instance.recomputeBounds();
            updatePorts(instance);
        } else if (attr == Plexers.ATTR_SELECT || attr == Plexers.ATTR_ENABLE) {
            updatePorts(instance);
        } else if (attr == Plexers.ATTR_TRISTATE || attr == Plexers.ATTR_DISABLED) {
            instance.fireInvalidated();
        }
    }

    private static void updatePorts(Instance instance) {
        Direction facing = instance.getAttributeValue(StdAttr.FACING);
        Object selectLoc = instance.getAttributeValue(Plexers.ATTR_SELECT_LOC);
        BitWidth select = instance.getAttributeValue(Plexers.ATTR_SELECT);
        boolean enable = instance.getAttributeValue(Plexers.ATTR_ENABLE);
        int outputs = 1 << select.getWidth();
        Port[] ps = new Port[outputs + (enable ? 2 : 1)];
        if (outputs == 2) {
            Location end0;
            Location end1;
            if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                int y = facing == Direction.NORTH ? -10 : 10;
                if (selectLoc == Plexers.SELECT_TOP_RIGHT) {
                    end0 = Location.create(-30, y);
                    end1 = Location.create(-10, y);
                } else {
                    end0 = Location.create(10, y);
                    end1 = Location.create(30, y);
                }
            } else {
                int x = facing == Direction.WEST ? -10 : 10;
                if (selectLoc == Plexers.SELECT_TOP_RIGHT) {
                    end0 = Location.create(x, 10);
                    end1 = Location.create(x, 30);
                } else {
                    end0 = Location.create(x, -30);
                    end1 = Location.create(x, -10);
                }
            }
            ps[0] = new Port(end0.getX(), end0.getY(), Port.OUTPUT, 1);
            ps[1] = new Port(end1.getX(), end1.getY(), Port.OUTPUT, 1);
        } else {
            int dx;
            int ddx;
            int dy;
            int ddy;
            if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                dy = facing == Direction.NORTH ? -20 : 20;
                ddy = 0;
                dx = selectLoc == Plexers.SELECT_TOP_RIGHT ? -10 * outputs : 0;
                ddx = 10;
            } else {
                dx = facing == Direction.WEST ? -20 : 20;
                ddx = 0;
                dy = selectLoc == Plexers.SELECT_TOP_RIGHT ? 0 : -10 * outputs;
                ddy = 10;
            }
            for (int i = 0; i < outputs; i++) {
                ps[i] = new Port(dx, dy, Port.OUTPUT, 1);
                dx += ddx;
                dy += ddy;
            }
        }
        Location en = Location.create(0, 0).translate(facing, -10);
        ps[outputs] = new Port(0, 0, Port.INPUT, select.getWidth());
        if (enable) {
            ps[outputs + 1] = new Port(en.getX(), en.getY(), Port.INPUT, BitWidth.ONE);
        }
        for (int i = 0; i < outputs; i++) {
            ps[i].setToolTip(getFromLocale("decoderOutTip", String.valueOf(i)));
        }
        ps[outputs].setToolTip(getFromLocale("decoderSelectTip"));
        if (enable) {
            ps[outputs + 1].setToolTip(getFromLocale("decoderEnableTip"));
        }
        instance.setPorts(ps);
    }

    @Override
    public void propagate(InstanceState state) {
        // get attributes
        BitWidth data = BitWidth.ONE;
        BitWidth select = state.getAttributeValue(Plexers.ATTR_SELECT);
        Boolean threeState = state.getAttributeValue(Plexers.ATTR_TRISTATE);
        boolean enable = state.getAttributeValue(Plexers.ATTR_ENABLE);
        int outputs = 1 << select.getWidth();

        // determine default output values
        // the default output
        Value others;
        if (threeState) {
            others = Value.UNKNOWN;
        } else {
            others = Value.FALSE;
        }

        // determine selected output value
        // the special output
        int outIndex = -1;
        Value out = null;
        Value en = enable ? state.getPort(outputs + 1) : Value.TRUE;
        if (en == Value.FALSE) {
            Object opt = state.getAttributeValue(Plexers.ATTR_DISABLED);
            Value base = opt == Plexers.DISABLED_ZERO ? Value.FALSE : Value.UNKNOWN;
            others = Value.repeat(base, data.getWidth());
        } else if (en == Value.ERROR && state.isPortConnected(outputs + 1)) {
            others = Value.createError(data);
        } else {
            Value sel = state.getPort(outputs);
            if (sel.isFullyDefined()) {
                outIndex = sel.toIntValue();
                out = Value.TRUE;
            } else if (sel.isErrorValue()) {
                others = Value.createError(data);
            } else {
                others = Value.createUnknown(data);
            }
        }

        // now propagate them
        for (int i = 0; i < outputs; i++) {
            state.setPort(i, i == outIndex ? out : others, Plexers.DELAY);
        }
    }

    @Override
    public void paintGhost(InstancePainter painter) {
        Direction facing = painter.getAttributeValue(StdAttr.FACING);
        BitWidth select = painter.getAttributeValue(Plexers.ATTR_SELECT);
        Plexers.drawTrapezoid(painter.getGraphics(), painter.getBounds(),
                facing.reverse(), select.getWidth() == 1 ? 10 : 20);
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        Bounds bds = painter.getBounds();
        Direction facing = painter.getAttributeValue(StdAttr.FACING);
        Object selectLoc = painter.getAttributeValue(Plexers.ATTR_SELECT_LOC);
        BitWidth select = painter.getAttributeValue(Plexers.ATTR_SELECT);
        boolean enable = painter.getAttributeValue(Plexers.ATTR_ENABLE);
        int selMult = selectLoc == Plexers.SELECT_TOP_RIGHT ? -1 : 1;
        int outputs = 1 << select.getWidth();

        // draw stubs for select and enable ports
        GraphicsUtil.switchToWidth(g, 3);
        boolean vertical = facing == Direction.NORTH || facing == Direction.SOUTH;
        int dx = vertical ? selMult : 0;
        int dy = vertical ? 0 : -selMult;
        // draw select wire
        if (outputs == 2) {
            if (painter.getShowState()) {
                g.setColor(painter.getPort(outputs).getColor());
            }
            Location pt = painter.getInstance().getPortLocation(outputs);
            g.drawLine(pt.getX(), pt.getY(), pt.getX() + 2 * dx, pt.getY() + 2 * dy);
        }
        if (enable) {
            Location en = painter.getInstance().getPortLocation(outputs + 1);
            int len = outputs == 2 ? 6 : 4;
            if (painter.getShowState()) {
                g.setColor(painter.getPort(outputs + 1).getColor());
            }
            g.drawLine(en.getX(), en.getY(), en.getX() + len * dx, en.getY() + len * dy);
        }
        GraphicsUtil.switchToWidth(g, 1);

        // draw a circle indicating where the select input is located
        Multiplexer.drawSelectCircle(g, bds, painter.getInstance().getPortLocation(outputs));

        // draw "0"
        int x0;
        int y0;
        int halign;
        if (facing == Direction.WEST) {
            x0 = 3;
            y0 = 15;
            halign = GraphicsUtil.H_LEFT;
        } else if (facing == Direction.NORTH) {
            x0 = 10;
            y0 = 15;
            halign = GraphicsUtil.H_CENTER;
        } else if (facing == Direction.SOUTH) {
            x0 = 10;
            y0 = bds.getHeight() - 3;
            halign = GraphicsUtil.H_CENTER;
        } else {
            x0 = bds.getWidth() - 3;
            y0 = 15;
            halign = GraphicsUtil.H_RIGHT;
        }
        g.setColor(Color.GRAY);
        GraphicsUtil.drawText(g, "0", bds.getX() + x0, bds.getY() + y0,
                halign, GraphicsUtil.V_BASELINE);

        // draw trapezoid, "Decd", and ports
        g.setColor(Color.BLACK);
        Plexers.drawTrapezoid(g, bds, facing.reverse(), outputs == 2 ? 10 : 20);
        GraphicsUtil.drawCenteredText(g, "Decd",
                bds.getX() + bds.getWidth() / 2,
                bds.getY() + bds.getHeight() / 2);
        painter.drawPorts();
    }
}

