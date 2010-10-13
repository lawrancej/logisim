/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.prefs;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public interface PrefMonitor<E> extends PreferenceChangeListener {
	public String getIdentifier();
	public boolean isSource(PropertyChangeEvent event);
	public void addPropertyChangeListener(PropertyChangeListener listener);
	public void removePropertyChangeListener(PropertyChangeListener listener);
	public E get();
	public void set(E value);
	public boolean getBoolean();
	public void setBoolean(boolean value);
	public void preferenceChange(PreferenceChangeEvent e);
}
