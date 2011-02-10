/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.gates;

import java.awt.Font;
import java.util.List;

import com.cburch.logisim.data.AbstractAttributeSet;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.instance.StdAttr;

class GateAttributes extends AbstractAttributeSet {
	static final int MAX_INPUTS = 32;
	static final int DELAY = 1;

	static final AttributeOption SIZE_NARROW
		= new AttributeOption(Integer.valueOf(30),
			Strings.getter("gateSizeNarrowOpt"));
	static final AttributeOption SIZE_MEDIUM
		= new AttributeOption(Integer.valueOf(50),
			Strings.getter("gateSizeNormalOpt"));
	static final AttributeOption SIZE_WIDE
		= new AttributeOption(Integer.valueOf(70),
			Strings.getter("gateSizeWideOpt"));
	public static final Attribute<AttributeOption> ATTR_SIZE
		= Attributes.forOption("size", Strings.getter("gateSizeAttr"),
			new AttributeOption[] { SIZE_NARROW, SIZE_MEDIUM, SIZE_WIDE });

	public static final Attribute<Integer> ATTR_INPUTS
		= Attributes.forIntegerRange("inputs", Strings.getter("gateInputsAttr"),
				2, MAX_INPUTS);

	static final AttributeOption XOR_ONE
		= new AttributeOption("1", Strings.getter("xorBehaviorOne"));
	static final AttributeOption XOR_ODD
		= new AttributeOption("odd", Strings.getter("xorBehaviorOdd"));
	public static final Attribute<AttributeOption> ATTR_XOR
		= Attributes.forOption("xor", Strings.getter("xorBehaviorAttr"),
				new AttributeOption[] { XOR_ONE, XOR_ODD });
	
	static final AttributeOption OUTPUT_01
		= new AttributeOption("01", Strings.getter("gateOutput01"));
	static final AttributeOption OUTPUT_0Z
		= new AttributeOption("0Z", Strings.getter("gateOutput0Z"));
	static final AttributeOption OUTPUT_Z1
		= new AttributeOption("Z1", Strings.getter("gateOutputZ1"));
	public static final Attribute<AttributeOption> ATTR_OUTPUT
		= Attributes.forOption("out", Strings.getter("gateOutputAttr"),
			new AttributeOption[] { OUTPUT_01, OUTPUT_0Z, OUTPUT_Z1 });
	

	Direction facing = Direction.EAST;
	BitWidth width = BitWidth.ONE;
	AttributeOption size = SIZE_MEDIUM;
	int inputs = 5;
	int negated = 0;
	AttributeOption out = OUTPUT_01;
	AttributeOption xorBehave;
	String label = "";
	Font labelFont = StdAttr.DEFAULT_LABEL_FONT;
	
	GateAttributes(boolean isXor) {
		xorBehave = isXor ? XOR_ONE : null;
	}

	@Override
	protected void copyInto(AbstractAttributeSet dest) {
		; // nothing to do
	}

	@Override
	public List<Attribute<?>> getAttributes() {
		return new GateAttributeList(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V getValue(Attribute<V> attr) {
		if (attr == StdAttr.FACING) return (V) facing;
		if (attr == StdAttr.WIDTH) return (V) width;
		if (attr == StdAttr.LABEL) return (V) label;
		if (attr == StdAttr.LABEL_FONT) return (V) labelFont;
		if (attr == ATTR_SIZE) return (V) size;
		if (attr == ATTR_INPUTS) return (V) Integer.valueOf(inputs);
		if (attr == ATTR_OUTPUT) return (V) out;
		if (attr == ATTR_XOR) return (V) xorBehave;
		if (attr instanceof NegateAttribute) {
			int index = ((NegateAttribute) attr).index;
			int bit = (negated >> index) & 1;
			return (V) Boolean.valueOf(bit == 1);
		}
		return null;
	}

	@Override
	public <V> void setValue(Attribute<V> attr, V value) {
		if (attr == StdAttr.WIDTH) {
			width = (BitWidth) value;
			int bits = width.getWidth();
			int mask = bits >= 32 ? -1 : ((1 << inputs) - 1);
			negated &= mask;
		} else if (attr == StdAttr.FACING) {
			facing = (Direction) value;
		} else if (attr == StdAttr.LABEL) {
			label = (String) value;
		} else if (attr == StdAttr.LABEL_FONT) {
			labelFont = (Font) value;
		} else if (attr == ATTR_SIZE) {
			size = (AttributeOption) value;
		} else if (attr == ATTR_INPUTS) {
			inputs = ((Integer) value).intValue();
			fireAttributeListChanged();
		} else if (attr == ATTR_XOR) {
			xorBehave = (AttributeOption) value;
		} else if (attr == ATTR_OUTPUT) {
			out = (AttributeOption) value;
		} else if (attr instanceof NegateAttribute) {
			int index = ((NegateAttribute) attr).index;
			if (((Boolean) value).booleanValue()) {
				negated |= 1 << index;
			} else {
				negated &= ~(1 << index);
			}
		} else {
			throw new IllegalArgumentException("unrecognized argument");
		}
		fireAttributeValueChanged(attr, value);
	}
}
