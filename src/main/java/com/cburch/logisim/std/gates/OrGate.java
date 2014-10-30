/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.gates;

import static com.cburch.logisim.util.LocaleString.*;

import java.awt.Graphics;

import com.cburch.logisim.analyze.model.Expression;
import com.cburch.logisim.analyze.model.Expressions;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.tools.WireRepairData;

class OrGate extends AbstractGate {
    public static OrGate FACTORY = new OrGate();

    private OrGate() {
        super("OR Gate", getFromLocale("orGateComponent"));
        setRectangularLabel("\u2265" + "1");
        setIconNames("orGate.svg", "orGateRect.svg", "dinOrGate.svg");
        setPaintInputLines(true);
    }

    @Override
    public void paintIconShaped(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        //g.drawImage(image.createImage(image.getWidth(), image.getHeight()), image.getWidth(), image.getHeight(), null);
    }

    @Override
    protected void paintShape(InstancePainter painter, int width, int height) {
        Graphics g = painter.getGraphics();
        //g.drawImage(image.createImage(image.getWidth(), image.getHeight()), image.getWidth(), image.getHeight(), null);
        PainterShaped.paintOr(painter, width, height);
    }

    @Override
    protected void paintDinShape(InstancePainter painter, int width, int height,
            int inputs) {
        PainterDin.paintOr(painter, width, height, false);
    }

    @Override
    protected Value computeOutput(Value[] inputs, int numInputs,
            InstanceState state) {
        return GateFunctions.computeOr(inputs, numInputs);
    }

    @Override
    protected boolean shouldRepairWire(Instance instance, WireRepairData data) {
        boolean ret = !data.getPoint().equals(instance.getLocation());
        return ret;
    }

    @Override
    protected Expression computeExpression(Expression[] inputs, int numInputs) {
        Expression ret = inputs[0];
        for (int i = 1; i < numInputs; i++) {
            ret = Expressions.or(ret, inputs[i]);
        }
        return ret;
    }

    @Override
    protected Value getIdentity() { return Value.FALSE; }
}
