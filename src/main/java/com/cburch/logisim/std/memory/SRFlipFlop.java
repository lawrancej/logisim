/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import com.cburch.logisim.data.Value;
import static com.cburch.logisim.util.LocaleString.*;

public class SRFlipFlop extends AbstractFlipFlop {
	public SRFlipFlop() {
		super("S-R Flip-Flop", "srFlipFlop.gif",
				__("srFlipFlopComponent"), 2, true);
	}

	@Override
	protected String getInputName(int index) {
		return index == 0 ? "S" : "R";
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
				return Value.ERROR;
			}
		}
		return Value.UNKNOWN;
	}
}
