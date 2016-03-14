/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. 
 * Written by Mechtecs*/

package com.cburch.logisim.std.arith;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.util.GraphicsUtil;

import static com.cburch.logisim.util.LocaleString.*;

public class Pow extends InstanceFactory {
    static final int PER_DELAY = 1;

    private static final int IN0   = 0;
    private static final int IN1   = 1;
    private static final int OUT   = 2;

    public Pow() {
        super("Pow", getFromLocale("powComponent"));
        setAttributes(new Attribute[] {
                StdAttr.WIDTH
            }, new Object[] {
                BitWidth.create(8)
            });
        
        setKeyConfigurator(new BitWidthConfigurator(StdAttr.WIDTH));
        setOffsetBounds(Bounds.create(-40, -20, 40, 40));
        setIconName("pow.svg");

        Port[] ps = new Port[3];
        ps[IN0]   = new Port(-40,  10, Port.INPUT,  StdAttr.WIDTH);
        ps[IN1]   = new Port(-40, -10, Port.INPUT,  StdAttr.WIDTH);
        ps[OUT]   = new Port(  0,   0, Port.OUTPUT, StdAttr.WIDTH);
        ps[IN0].setToolTip(getFromLocale("powInputNumTip"));
        ps[IN1].setToolTip(getFromLocale("powInputExpTip"));
        ps[OUT].setToolTip(getFromLocale("powOutputTip"));
        setPorts(ps);
    }

    @Override
    public void propagate(InstanceState state) {
        // get attributes
        BitWidth dataWidth = state.getAttributeValue(StdAttr.WIDTH);

        // compute outputs
        Value a = state.getPort(IN0);
        Value b = state.getPort(IN1);
        Value[] outs = Pow.computeSum(dataWidth, a, b);

        // propagate them
        int delay = (dataWidth.getWidth() + 2) * PER_DELAY;
        state.setPort(OUT,   outs[0], delay);
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        painter.drawBounds();

        g.setColor(Color.GRAY);
        painter.drawPort(IN0);
        painter.drawPort(IN1);
        painter.drawPort(OUT);
        Location loc = painter.getLocation();
        int x = loc.getX();
        int y = loc.getY();
        GraphicsUtil.switchToWidth(g, 2);
        g.setColor(Color.BLACK);
        g.drawLine(x - 25, y, x - 35, y + 10 );
        g.drawLine(x - 25, y + 10, x - 35, y);
        g.drawLine(x - 20, y - 10, x - 15, y - 5);
        g.drawLine(x - 20, y + 5, x - 10, y - 10);
        GraphicsUtil.switchToWidth(g, 1);
    }

    static Value[] computeSum(BitWidth width, Value a, Value b) { //Useless functions and data type conversion because java :)
        int ia = a.toIntValue();
        int ib = b.toIntValue();
    	double da=ia;
    	double db=ib;
        double dequus = Math.pow(da,db);
        int equus = (int) Math.round(dequus);
        return new Value[] { Value.createKnown(width, equus) };
    }
}
