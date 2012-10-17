/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools.key;

import java.awt.event.InputEvent;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Value;

public class BitWidthConfigurator extends NumericConfigurator<BitWidth> {
	public BitWidthConfigurator(Attribute<BitWidth> attr, int min, int max, int modifiersEx) {
		super(attr, min, max, modifiersEx);
	}
	
	public BitWidthConfigurator(Attribute<BitWidth> attr, int min, int max) {
		super(attr, min, max, InputEvent.ALT_DOWN_MASK);
	}
	
	public BitWidthConfigurator(Attribute<BitWidth> attr) {
		super(attr, 1, Value.MAX_WIDTH, InputEvent.ALT_DOWN_MASK);
	}
	
	@Override
	protected BitWidth createValue(int val) {
		return BitWidth.create(val);
	}
}
