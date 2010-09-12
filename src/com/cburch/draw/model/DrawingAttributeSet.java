/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.util.EventSourceWeakSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class DrawingAttributeSet implements AttributeSet, Cloneable {
	private EventSourceWeakSupport<AttributeListener> listeners;
	private List<Attribute<?>> attrs;
	private List<Object> values;
	private List<Attribute<?>> selectedAttrs;
	private List<Attribute<?>> selectedView;
	
	public DrawingAttributeSet() {
		listeners = new EventSourceWeakSupport<AttributeListener>();
		attrs = DrawAttr.ATTRS_ALL;
		values = DrawAttr.DEFAULTS_ALL;
		selectedAttrs = new ArrayList<Attribute<?>>();
		selectedAttrs.addAll(attrs);
		selectedView = Collections.unmodifiableList(selectedAttrs);
	}
	
	public void setAttributes(List<Attribute<?>> attrs) {
		List<Attribute<?>> selected = selectedAttrs;
		if (!selected.equals(attrs)) {
			selected.clear();
			selected.addAll(attrs);
			AttributeEvent e = new AttributeEvent(this);
			for (AttributeListener listener : listeners) {
				listener.attributeListChanged(e);
			}
		}
	}
	
	public void addAttributeListener(AttributeListener l) {
		listeners.add(l);
	}

	public void removeAttributeListener(AttributeListener l) {
		listeners.remove(l);
	}
	
	@Override
	public Object clone() {
		try {
			DrawingAttributeSet ret = (DrawingAttributeSet) super.clone();
			ret.listeners = new EventSourceWeakSupport<AttributeListener>();
			ret.values = new ArrayList<Object>(this.values);
			ret.selectedAttrs = new ArrayList<Attribute<?>>(this.selectedAttrs);
			return ret;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	public List<Attribute<?>> getAttributes() {
		return selectedView;
	}

	public boolean containsAttribute(Attribute<?> attr) {
		return selectedAttrs.contains(attr);
	}
	
	public Attribute<?> getAttribute(String name) {
		for (Attribute<?> attr : selectedAttrs) {
			if(attr.getName().equals(name)) return attr;
		}
		return null;
	}

	public boolean isReadOnly(Attribute<?> attr) {
		return false;
	}
	
	public void setReadOnly(Attribute<?> attr, boolean value) {
		throw new UnsupportedOperationException("setReadOnly");
	}
	
	public boolean isToSave(Attribute<?> attr) {
		return true;
	}
	
	public <V> V getValue(Attribute<V> attr) {
		Iterator<Attribute<?>> ait = attrs.iterator();
		Iterator<Object> vit = values.iterator();
		while(ait.hasNext()) {
			Object a = ait.next();
			Object v = vit.next();
			if(a.equals(attr)) {
				@SuppressWarnings("unchecked")
				V ret = (V) v;
				return ret;
			}
		}
		return null;
	}

	public <V> void setValue(Attribute<V> attr, V value) {
		Iterator<Attribute<?>> ait = attrs.iterator();
		ListIterator<Object> vit = values.listIterator();
		while(ait.hasNext()) {
			Object a = ait.next();
			vit.next();
			if(a.equals(attr)) {
				vit.set(value);
				AttributeEvent e = new AttributeEvent(this, attr, value);
				for (AttributeListener listener : listeners) {
					listener.attributeValueChanged(e);
				}
				if (attr == DrawAttr.PAINT_TYPE) {
					e = new AttributeEvent(this);
					for (AttributeListener listener : listeners) {
						listener.attributeListChanged(e);
					}
				}
				return;
			}
		}
		throw new IllegalArgumentException(attr.toString());
	}
}
