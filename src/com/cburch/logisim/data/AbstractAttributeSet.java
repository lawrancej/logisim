/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.data;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAttributeSet implements Cloneable, AttributeSet {
	private ArrayList<AttributeListener> listeners = null;
	
	public AbstractAttributeSet() { }
	
	@Override
	public Object clone() {
		AbstractAttributeSet ret;
		try {
			ret = (AbstractAttributeSet) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new UnsupportedOperationException();
		}
		ret.listeners = new ArrayList<AttributeListener>();
		this.copyInto(ret);
		return ret;
	}
	
	public void addAttributeListener(AttributeListener l) {
		if (listeners == null) listeners = new ArrayList<AttributeListener>();
		listeners.add(l);
	}
	public void removeAttributeListener(AttributeListener l) {
		listeners.remove(l);
		if (listeners.isEmpty()) listeners = null;
	}
	protected <V> void fireAttributeValueChanged(Attribute<? super V> attr, V value) {
		if (listeners != null) {
			AttributeEvent event = new AttributeEvent(this, attr, value);
			List<AttributeListener> ls = new ArrayList<AttributeListener>(listeners);
			for (AttributeListener l : ls) {
				l.attributeValueChanged(event);
			}
		}
	}
	protected void fireAttributeListChanged() {
		if (listeners != null) {
			AttributeEvent event = new AttributeEvent(this);
			List<AttributeListener> ls = new ArrayList<AttributeListener>(listeners);
			for (AttributeListener l : ls) {
				l.attributeListChanged(event);
			}
		}
	}

	public boolean containsAttribute(Attribute<?> attr) {
		return getAttributes().contains(attr);
	}
	public Attribute<?> getAttribute(String name) {
		for (Attribute<?> attr : getAttributes()) {
			if (attr.getName().equals(name)) {
				return attr;
			}
		}
		return null;
	}

	public boolean isReadOnly(Attribute<?> attr) {
		return false;
	}
	public void setReadOnly(Attribute<?> attr, boolean value) {
		throw new UnsupportedOperationException();
	}
	public boolean isToSave(Attribute<?> attr) {
		return true;
	}

	protected abstract void copyInto(AbstractAttributeSet dest);
	public abstract List<Attribute<?>> getAttributes();
	public abstract <V> V getValue(Attribute<V> attr);
	public abstract <V> void setValue(Attribute<V> attr, V value);

}
