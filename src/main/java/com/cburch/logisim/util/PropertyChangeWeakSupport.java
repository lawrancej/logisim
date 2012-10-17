/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PropertyChangeWeakSupport {
	private static final String ALL_PROPERTIES = "ALL PROPERTIES";
	
	private static class ListenerData {
		String property;
		WeakReference<PropertyChangeListener> listener;
		ListenerData(String property, PropertyChangeListener listener) {
			this.property = property;
			this.listener = new WeakReference<PropertyChangeListener>(listener);
		}
	}
	
	private Object source;
	private ConcurrentLinkedQueue<ListenerData> listeners;

	public PropertyChangeWeakSupport(Object source) {
		this.source = source;
		this.listeners = new ConcurrentLinkedQueue<ListenerData>();
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		addPropertyChangeListener(ALL_PROPERTIES, listener);
	}
	
	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		listeners.add(new ListenerData(property, listener));
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		removePropertyChangeListener(ALL_PROPERTIES, listener);
	}
	
	public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
		for (Iterator<ListenerData> it = listeners.iterator(); it.hasNext(); ) {
			ListenerData data = it.next();
			PropertyChangeListener l = data.listener.get();
			if (l == null) {
				it.remove();
			} else if (data.property.equals(property) && l == listener) {
				it.remove();
			}
		}
	}
	
	public void firePropertyChange(String property, Object oldValue, Object newValue) {
		PropertyChangeEvent e = null;
		for (Iterator<ListenerData> it = listeners.iterator(); it.hasNext(); ) {
			ListenerData data = it.next();
			PropertyChangeListener l = data.listener.get();
			if (l == null) {
				it.remove();
			} else if (data.property == ALL_PROPERTIES
					|| data.property.equals(property)) {
				if (e == null) {
					e = new PropertyChangeEvent(source, property, oldValue, newValue);
				}
				l.propertyChange(e);
			}
		}
	}
	
	public void firePropertyChange(String property, int oldValue, int newValue) {
		PropertyChangeEvent e = null;
		for (Iterator<ListenerData> it = listeners.iterator(); it.hasNext(); ) {
			ListenerData data = it.next();
			PropertyChangeListener l = data.listener.get();
			if (l == null) {
				it.remove();
			} else if (data.property == ALL_PROPERTIES
					|| data.property.equals(property)) {
				if (e == null) {
					e = new PropertyChangeEvent(source, property,
						Integer.valueOf(oldValue), Integer.valueOf(newValue));
				}
				l.propertyChange(e);
			}
		}
	}
	
	public void firePropertyChange(String property, boolean oldValue, boolean newValue) {
		PropertyChangeEvent e = null;
		for (Iterator<ListenerData> it = listeners.iterator(); it.hasNext(); ) {
			ListenerData data = it.next();
			PropertyChangeListener l = data.listener.get();
			if (l == null) {
				it.remove();
			} else if (data.property == ALL_PROPERTIES
					|| data.property.equals(property)) {
				if (e == null) {
					e = new PropertyChangeEvent(source, property,
						Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
				}
				l.propertyChange(e);
			}
		}
	}
	
}
