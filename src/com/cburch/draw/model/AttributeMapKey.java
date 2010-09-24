/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import com.cburch.logisim.data.Attribute;

public class AttributeMapKey {
	private Attribute<?> attr;
	private CanvasObject object;
	
	public AttributeMapKey(Attribute<?> attr, CanvasObject object) {
		this.attr = attr;
		this.object = object;
	}
	
	public Attribute<?> getAttribute() {
		return attr;
	}
	
	public CanvasObject getObject() {
		return object;
	}
	
	@Override
	public int hashCode() {
		int a = attr == null ? 0 : attr.hashCode();
		int b = object == null ? 0 : object.hashCode();
		return a ^ b;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof AttributeMapKey)) return false;
		AttributeMapKey o = (AttributeMapKey) other;
		return (attr == null ? o.attr == null : attr.equals(o.attr))
			&& (object == null ? o.object == null : object.equals(o.object));
	}
}
