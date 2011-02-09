/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.wiring;

import java.util.Arrays;
import java.util.List;

import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.instance.StdAttr;

class PinAttributes extends ProbeAttributes {
	public static PinAttributes instance = new PinAttributes();

	private static final List<Attribute<?>> ATTRIBUTES
		= Arrays.asList(new Attribute<?>[] {
			StdAttr.FACING, Pin.ATTR_TYPE, StdAttr.WIDTH, Pin.ATTR_TRISTATE,
			Pin.ATTR_PULL, StdAttr.LABEL, Pin.ATTR_LABEL_LOC, StdAttr.LABEL_FONT
		});

	BitWidth width = BitWidth.ONE;
	boolean threeState = true;
	int type = EndData.INPUT_ONLY;
	Object pull = Pin.PULL_NONE;

	public PinAttributes() { }

	@Override
	public List<Attribute<?>> getAttributes() {
		return ATTRIBUTES;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V getValue(Attribute<V> attr) {
		if (attr == StdAttr.WIDTH) return (V) width;
		if (attr == Pin.ATTR_TRISTATE) return (V) Boolean.valueOf(threeState);
		if (attr == Pin.ATTR_TYPE) return (V) Boolean.valueOf(type == EndData.OUTPUT_ONLY);
		if (attr == Pin.ATTR_PULL) return (V) pull;
		return super.getValue(attr);
	}
	
	boolean isOutput() {
		return type != EndData.INPUT_ONLY;
	}
	
	boolean isInput() {
		return type != EndData.OUTPUT_ONLY;
	}

	@Override
	public <V> void setValue(Attribute<V> attr, V value) {
		if (attr == StdAttr.WIDTH) {
			width = (BitWidth) value;
		} else if (attr == Pin.ATTR_TRISTATE) {
			threeState = ((Boolean) value).booleanValue();
		} else if (attr == Pin.ATTR_TYPE) {
			type = ((Boolean) value).booleanValue() ? EndData.OUTPUT_ONLY : EndData.INPUT_ONLY;
		} else if (attr == Pin.ATTR_PULL) {
			pull = value;
		} else {
			super.setValue(attr, value);
			return;
		}
		fireAttributeValueChanged(attr, value);
	}
}


