/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.gates;

import java.util.AbstractList;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.instance.StdAttr;

class GateAttributeList extends AbstractList<Attribute<?>> {
	private static final Attribute<?>[] BASE_ATTRIBUTES = {
			StdAttr.FACING, StdAttr.WIDTH,
			GateAttributes.ATTR_SIZE, GateAttributes.ATTR_INPUTS,
			GateAttributes.ATTR_OUTPUT, StdAttr.LABEL, StdAttr.LABEL_FONT,
	};

	private GateAttributes attrs;
	
	public GateAttributeList(GateAttributes attrs) {
		this.attrs = attrs;
	}

	@Override
	public Attribute<?> get(int index) {
		int len = BASE_ATTRIBUTES.length;
		if (index < len) {
			return BASE_ATTRIBUTES[index];
		}
		index -= len;
		if (attrs.xorBehave != null) {
			index--;
			if (index < 0) return GateAttributes.ATTR_XOR;
		}
		Direction facing = attrs.facing;
		int inputs = attrs.inputs;
		if (index == 0) {
			if (facing == Direction.EAST || facing == Direction.WEST) {
				return new NegateAttribute(index, Direction.NORTH);
			} else {
				return new NegateAttribute(index, Direction.WEST);
			}
		} else if (index == inputs - 1) {
			if (facing == Direction.EAST || facing == Direction.WEST) {
				return new NegateAttribute(index, Direction.SOUTH);
			} else {
				return new NegateAttribute(index, Direction.EAST);
			}
		} else if (index < inputs) {
			return new NegateAttribute(index, null);
		}
		return null;
	}

	@Override
	public int size() {
		int ret = BASE_ATTRIBUTES.length;
		if (attrs.xorBehave != null) ret++;
		ret += attrs.inputs;
		return ret;
	}
}
