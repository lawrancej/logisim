/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import com.cburch.draw.canvas.CanvasObject;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.EventSourceWeakSupport;
import com.cburch.logisim.util.GraphicsUtil;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class DrawingMember
		implements AttributeSet, CanvasObject, Cloneable {
	private EventSourceWeakSupport<AttributeListener> listeners;
	
	public DrawingMember() {
		listeners = new EventSourceWeakSupport<AttributeListener>();
	}
	
	public AttributeSet getAttributeSet() {
		return this;
	}
	
	public abstract Element toSvgElement(Document doc);
	protected abstract void updateValue(Attribute<?> attr, Object value);
	public abstract void translate(int dx, int dy);
	public abstract void moveHandle(Location handle, int dx, int dy);
	
	public boolean canRemove() {
		return true;
	}
	
	public boolean canMoveHandle(Location handle) {
		return true;
	}
	
	public boolean canInsertHandle(Location handle) {
		return false;
	}
	
	public boolean canDeleteHandle(Location handle) {
		return false;
	}
	
	public Location insertHandle(Location handle, Location desiredLocation) {
		throw new UnsupportedOperationException("insertHandle");
	}
	
	public Location deleteHandle(Location handle) {
		throw new UnsupportedOperationException("deleteHandle");
	}

	// methods required by AttributeSet interface
	public abstract List<Attribute<?>> getAttributes();
	public abstract <V> V getValue(Attribute<V> attr);

	public void addAttributeListener(AttributeListener l) {
		listeners.add(l);
	}

	public void removeAttributeListener(AttributeListener l) {
		listeners.remove(l);
	}
	
	@Override
	public CanvasObject clone() {
		try {
			DrawingMember ret = (DrawingMember) super.clone();
			ret.listeners = new EventSourceWeakSupport<AttributeListener>();
			return ret;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public boolean containsAttribute(Attribute<?> attr) {
		return getAttributes().contains(attr);
	}
	
	public Attribute<?> getAttribute(String name) {
		for (Attribute<?> attr : getAttributes()) {
			if (attr.getName().equals(name)) return attr;
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

	public final <V> void setValue(Attribute<V> attr, V value) {
		Object old = getValue(attr);
		boolean same = old == null ? value == null : old.equals(value);
		if(!same) {
			updateValue(attr, value);
			AttributeEvent e = new AttributeEvent(this, attr, value);
			for (AttributeListener listener : listeners) {
				listener.attributeValueChanged(e);
			}
		}
	}
	
	protected void fireAttributeListChanged() {
		AttributeEvent e = new AttributeEvent(this);
		for (AttributeListener listener : listeners) {
			listener.attributeListChanged(e);
		}
	}
	
	protected boolean setForStroke(Graphics g) {
		List<Attribute<?>> attrs = getAttributes();
		if (attrs.contains(DrawAttr.PAINT_TYPE)) {
			Object value = getValue(DrawAttr.PAINT_TYPE);
			if (value == DrawAttr.PAINT_FILL) return false;
		}

		Integer width = getValue(DrawAttr.STROKE_WIDTH);
		if (width != null && width.intValue() > 0) {
			Color color = getValue(DrawAttr.STROKE_COLOR);
			if (color != null && color.getAlpha() == 0) {
				return false;
			} else {
				GraphicsUtil.switchToWidth(g, width);
				if (color != null) g.setColor(color);
				return true;
			}
		} else {
			return false;
		}
	}
	
	protected boolean setForFill(Graphics g) {
		List<Attribute<?>> attrs = getAttributes();
		if (attrs.contains(DrawAttr.PAINT_TYPE)) {
			Object value = getValue(DrawAttr.PAINT_TYPE);
			if (value == DrawAttr.PAINT_STROKE) return false;
		}

		Color color = getValue(DrawAttr.FILL_COLOR);
		if (color != null && color.getAlpha() == 0) {
			return false;
		} else {
			if (color != null) g.setColor(color);
			return true;
		}
	}

}
