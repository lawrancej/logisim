/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.util.PropertyChangeWeakSupport;

class Clipboard {
	public static final String contentsProperty = "contents";
	
	private static Clipboard current = null;
	private static PropertyChangeWeakSupport propertySupport = new PropertyChangeWeakSupport(Clipboard.class);
	
	public static boolean isEmpty() {
		return current == null || current.components.isEmpty();
	}
	
	public static Clipboard get() {
		return current;
	}
	
	public static void set(Selection value, AttributeSet oldAttrs) {
		set(new Clipboard(value, oldAttrs));
	}
	
	public static void set(Clipboard value) {
		Clipboard old = current;
		current = value;
		propertySupport.firePropertyChange(contentsProperty, old, current);
	}
	
	//
	// PropertyChangeSource methods
	//
	public static void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}
	public static void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(propertyName, listener);
	}
	public static void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}
	public static void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
	}

	//
	// instance variables and methods
	//
	private HashSet<Component> components;
	private AttributeSet oldAttrs;
	private AttributeSet newAttrs;
	
	private Clipboard(Selection sel, AttributeSet viewAttrs) {
		components = new HashSet<Component>();
		oldAttrs = null;
		newAttrs = null;
		for (Component base : sel.getComponents()) {
			AttributeSet baseAttrs = base.getAttributeSet();
			AttributeSet copyAttrs = (AttributeSet) baseAttrs.clone();
			Component copy = base.getFactory().createComponent(base.getLocation(),
					copyAttrs);
			components.add(copy);
			if (baseAttrs == viewAttrs) {
				oldAttrs = baseAttrs;
				newAttrs = copyAttrs;
			}
		}
	}
	
	public Collection<Component> getComponents() {
		return components;
	}
	
	public AttributeSet getOldAttributeSet() {
		return oldAttrs;
	}
	
	public AttributeSet getNewAttributeSet() {
		return newAttrs;
	}
	
	void setOldAttributeSet(AttributeSet value) {
		oldAttrs = value;
	}
}
