/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.data;

import com.cburch.logisim.util.StringGetter;

public class AttributeOption implements AttributeOptionInterface {
	private Object value;
	private String name;
	private StringGetter desc;

	public AttributeOption(Object value, StringGetter desc) {
		this.value = value;
		this.name = value.toString();
		this.desc = desc;
	}

	public AttributeOption(Object value, String name, StringGetter desc) {
		this.value = value;
		this.name = name;
		this.desc = desc;
	}

	public Object getValue() { return value; }

	@Override
	public String toString() { return name; }

	public String toDisplayString() { return desc.toString(); }
}
