/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.awt.Graphics;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.util.GraphicsUtil;

public class JKFlipFlop extends AbstractFlipFlop {
    public JKFlipFlop() {
        super("J-K Flip-Flop", Strings.getter("jkFlipFlopComponent"), 2, false);
    }

    @Override
    public void paintIcon(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        g.drawRect(2, 2, 16, 16);
        GraphicsUtil.drawCenteredText(g, "JK", 10, 8);
    }

    @Override
    protected String getInputName(int index) {
        return index == 0 ? "J" : "K";
    }

    @Override
    protected Value computeValue(Value[] inputs, Value curValue) {
        if (inputs[0] == Value.FALSE) {
            if (inputs[1] == Value.FALSE) {
                return curValue;
            } else if (inputs[1] == Value.TRUE) {
                return Value.FALSE;
            }
        } else if (inputs[0] == Value.TRUE) {
            if (inputs[1] == Value.FALSE) {
                return Value.TRUE;
            } else if (inputs[1] == Value.TRUE) {
                return curValue.not();
            }
        }
        return Value.UNKNOWN;
    }
}
