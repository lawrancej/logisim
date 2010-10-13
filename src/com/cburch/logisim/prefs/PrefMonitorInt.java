/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.prefs;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

class PrefMonitorInt extends AbstractPrefMonitor<Integer> {
	private int dflt;
	private int value;
	
	PrefMonitorInt(String name, int dflt) {
		super(name);
		this.dflt = dflt;
		this.value = dflt;
		Preferences prefs = AppPreferences.getPrefs();
		set(Integer.valueOf(prefs.getInt(name, dflt)));
		prefs.addPreferenceChangeListener(this);
	}
	
	public Integer get() {
		return Integer.valueOf(value);
	}
	
	public void set(Integer newValue) {
		int newVal = newValue.intValue();
		if (value != newVal) {
			AppPreferences.getPrefs().putInt(getIdentifier(), newVal);
		}
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		Preferences prefs = event.getNode();
		String prop = event.getKey();
		String name = getIdentifier();
		if (prop.equals(name)) {
			int oldValue = value;
			int newValue = prefs.getInt(name, dflt);
			if (newValue != oldValue) {
				value = newValue;
				AppPreferences.firePropertyChange(name,
						Integer.valueOf(oldValue), Integer.valueOf(newValue));
			}
		}
	}
}
