/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import com.cburch.logisim.data.Value;

public class TFlipFlop extends AbstractFlipFlop {
	public TFlipFlop() {
		super("T Flip-Flop", "tFlipFlop.gif",
				Strings.getter("tFlipFlopComponent"), 1, false);
	}

	@Override
	protected String getInputName(int index) {
		return "T";
	}

	@Override
	protected Value computeValue(Value[] inputs, Value curValue) {
		if (curValue == Value.UNKNOWN) curValue = Value.FALSE;
		if (inputs[0] == Value.TRUE) {
			return curValue.not();
		} else {
			return curValue;
		}
	}
}
