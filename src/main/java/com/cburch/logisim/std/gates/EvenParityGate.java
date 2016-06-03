/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.gates;

import com.cburch.logisim.analyze.model.Expression;
import com.cburch.logisim.analyze.model.Expressions;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import static com.cburch.logisim.util.LocaleString.*;

class EvenParityGate extends AbstractGate {
    public static EvenParityGate FACTORY = new EvenParityGate();

    private EvenParityGate() {
        super("Even Parity", getFromLocale("evenParityComponent"));
        setRectangularLabel("2k");
        setIconNames("parityEvenGate.svg");
    }

    @Override
    public void paintIconShaped(InstancePainter painter) {
        paintIconRectangular(painter);
    }

    @Override
    protected void paintShape(InstancePainter painter, int width, int height) {
        painter.paintRectangular(width, height, this);
    }

    @Override
    protected void paintDinShape(InstancePainter painter, int width, int height,
            int inputs) {
        painter.paintRectangular(width, height, this);
    }

    @Override
    protected Value computeOutput(Value[] inputs, int numInputs, InstanceState state) {
        return GateFunctions.computeOddParity(inputs, numInputs).not();
    }

    @Override
    protected Expression computeExpression(Expression[] inputs, int numInputs) {
        Expression ret = inputs[0];
        for (int i = 1; i < numInputs; i++) {
            ret = Expressions.xor(ret, inputs[i]);
        }
        return Expressions.not(ret);
    }

    @Override
    protected Value getIdentity() { return Value.FALSE; }
}
