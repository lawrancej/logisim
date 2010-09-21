/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.cburch.draw.model.CanvasObject;
import com.cburch.logisim.util.PropertyChangeWeakSupport;
import com.cburch.logisim.util.UnmodifiableList;

class Clipboard {
	public static final String contentsProperty = "appearance";
	
	private static Clipboard current
		= new Clipboard(Collections.<CanvasObject>emptySet());
	private static PropertyChangeWeakSupport propertySupport
		= new PropertyChangeWeakSupport(Clipboard.class);
	
	public static boolean isEmpty() {
		return current == null || current.objects.isEmpty();
	}
	
	public static Clipboard get() {
		return current;
	}
	
	public static void set(Collection<CanvasObject> value) {
		set(new Clipboard(value));
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
	private List<CanvasObject> objects;
	
	private Clipboard(Collection<CanvasObject> selected) {
		CanvasObject[] sel = new CanvasObject[selected.size()];
		sel = selected.toArray(sel);
		objects = UnmodifiableList.create(sel);
	}
	
	public List<CanvasObject> getObjects() {
		return objects;
	}
}
