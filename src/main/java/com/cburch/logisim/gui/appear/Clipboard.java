/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import java.beans.PropertyChangeListener;

import com.cburch.logisim.util.PropertyChangeWeakSupport;

class Clipboard {
	private Clipboard() { }
	
	public static final String contentsProperty = "appearance";
	
	private static ClipboardContents current = ClipboardContents.EMPTY;
	private static PropertyChangeWeakSupport propertySupport
		= new PropertyChangeWeakSupport(Clipboard.class);
	
	public static boolean isEmpty() {
		return current == null || current.getElements().isEmpty();
	}
	
	public static ClipboardContents get() {
		return current;
	}
	
	public static void set(ClipboardContents value) {
		ClipboardContents old = current;
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
}
