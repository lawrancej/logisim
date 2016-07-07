/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.wiring;

import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.*;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.tools.key.JoinedConfigurator;
import com.cburch.logisim.util.GraphicsUtil;

import java.awt.*;

import static com.cburch.logisim.util.LocaleString.getFromLocale;

public final class BitExtender extends InstanceFactory {
    private static final Attribute<BitWidth> ATTR_IN_WIDTH
        = Attributes.forBitWidth("in_width", getFromLocale("extenderInAttr"));
    private static final Attribute<BitWidth> ATTR_OUT_WIDTH
        = Attributes.forBitWidth("out_width", getFromLocale("extenderOutAttr"));
    private static final Attribute<AttributeOption> ATTR_TYPE
        = Attributes.forOption("type", getFromLocale("extenderTypeAttr"),
            new AttributeOption[] {
                new AttributeOption("zero", "zero", getFromLocale("extenderZeroType")),
                new AttributeOption("one", "one", getFromLocale("extenderOneType")),
                new AttributeOption("sign", "sign", getFromLocale("extenderSignType")),
                new AttributeOption("input", "input", getFromLocale("extenderInputType")),
            });

    public static final BitExtender FACTORY = new BitExtender();

    private BitExtender() {
        super("Bit Extender", getFromLocale("extenderComponent"));
        setIconName("extender.svg");
        setAttributes(new Attribute[] {
                ATTR_IN_WIDTH, ATTR_OUT_WIDTH, ATTR_TYPE
            }, new Object[] {
                BitWidth.create(8), BitWidth.create(16), ATTR_TYPE.parse("zero")
            });
        setFacingAttribute(StdAttr.FACING);
        setKeyConfigurator(JoinedConfigurator.create(
                new BitWidthConfigurator(ATTR_OUT_WIDTH),
                new BitWidthConfigurator(ATTR_IN_WIDTH, 1, Value.MAX_WIDTH, 0)));
        setOffsetBounds(Bounds.create(-40, -20, 40, 40));
    }

    //
    // graphics methods
    //
    @Override
    public void paintInstance(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        FontMetrics fm = g.getFontMetrics();
        int asc = fm.getAscent();

        painter.drawBounds();

        String s0;
        String type = getType(painter.getAttributeSet());
        switch (type) {
            case "zero":
                s0 = getFromLocale("extenderZeroLabel");
                break;
            case "one":
                s0 = getFromLocale("extenderOneLabel");
                break;
            case "sign":
                s0 = getFromLocale("extenderSignLabel");
                break;
            case "input":
                s0 = getFromLocale("extenderInputLabel");
                break;

            // should never happen
            default:
                s0 = "???";
                break;
        }

        String s1 = getFromLocale("extenderMainLabel");
        Bounds bds = painter.getBounds();
        int x = bds.getX() + bds.getWidth() / 2;
        int y0 = bds.getY() + (bds.getHeight() / 2 + asc) / 2;
        int y1 = bds.getY() + (3 * bds.getHeight() / 2 + asc) / 2;
        GraphicsUtil.drawText(g, s0, x, y0,
                GraphicsUtil.H_CENTER, GraphicsUtil.V_BASELINE);
        GraphicsUtil.drawText(g, s1, x, y1,
                GraphicsUtil.H_CENTER, GraphicsUtil.V_BASELINE);

        BitWidth w0 = painter.getAttributeValue(ATTR_OUT_WIDTH);
        BitWidth w1 = painter.getAttributeValue(ATTR_IN_WIDTH);
        painter.drawPort(0, String.valueOf(w0.getWidth()), Direction.WEST);
        painter.drawPort(1, String.valueOf(w1.getWidth()), Direction.EAST);
        if (type.equals("input")) {
            painter.drawPort(2);
        }

    }

    //
    // methods for instances
    //
    @Override
    protected void configureNewInstance(Instance instance) {
        configurePorts(instance);
        instance.addAttributeListener();
    }

    @Override
    protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
        if (attr == ATTR_TYPE) {
            configurePorts(instance);
            instance.fireInvalidated();
        } else {
            instance.fireInvalidated();
        }
    }

    private static void configurePorts(Instance instance) {
        Port p0 = new Port(0, 0, Port.OUTPUT, ATTR_OUT_WIDTH);
        Port p1 = new Port(-40, 0, Port.INPUT, ATTR_IN_WIDTH);
        String type = getType(instance.getAttributeSet());
        if (type.equals("input")) {
            instance.setPorts(new Port[] { p0, p1, new Port(-20, -20, Port.INPUT, 1) });
        } else {
            instance.setPorts(new Port[] { p0, p1 });
        }
    }

    @Override
    public void propagate(InstanceState state) {
        Value in = state.getPort(1);
        BitWidth wout = state.getAttributeValue(ATTR_OUT_WIDTH);
        String type = getType(state.getAttributeSet());
        Value extend;
        switch (type) {
            case "one":
                extend = Value.TRUE;
                break;
            case "sign":
                int win = in.getWidth();
                extend = win > 0 ? in.get(win - 1) : Value.ERROR;
                break;
            case "input":
                extend = state.getPort(2);
                if (extend.getWidth() != 1) {
                    extend = Value.ERROR;
                }

                break;
            default:
                extend = Value.FALSE;
                break;
        }

        Value out = in.extendWidth(wout.getWidth(), extend);
        state.setPort(0, out, 1);
    }


    private static String getType(AttributeSet attrs) {
        AttributeOption topt = attrs.getValue(ATTR_TYPE);
        return (String) topt.getValue();
    }
}