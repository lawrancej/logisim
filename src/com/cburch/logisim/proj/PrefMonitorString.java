/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.proj;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

class PrefMonitorString implements PreferenceChangeListener {
	private String name;
	private String dflt;
	private String value;
	
	PrefMonitorString(String name, String dflt) {
		this.name = name;
		this.dflt = dflt;
		this.value = dflt;
		Preferences prefs = LogisimPreferences.getPrefs();
		set(prefs.get(name, dflt));
		prefs.addPreferenceChangeListener(this);
	}
	
	public String get() {
		return value;
	}
	
	public void set(String newValue) {
		String oldValue = value;
		if (!isSame(oldValue, newValue)) {
			value = newValue;
			LogisimPreferences.getPrefs().put(name, newValue);
		}
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		Preferences prefs = event.getNode();
		String prop = event.getKey();
		if (prop.equals(name)) {
			String oldValue = value;
			String newValue = prefs.get(name, dflt);
			if (!isSame(oldValue, newValue)) {
				value = newValue;
				LogisimPreferences.firePropertyChange(name, oldValue, newValue);
			}
		}
	}
	
	private static boolean isSame(String a, String b) {
		return a == null ? b == null : a.equals(b);
	}
}
