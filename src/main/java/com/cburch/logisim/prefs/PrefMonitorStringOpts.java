/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.prefs;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

class PrefMonitorStringOpts extends AbstractPrefMonitor<String> {
	private String[] opts;
	private String value;
	private String dflt;
	
	PrefMonitorStringOpts(String name, String[] opts, String dflt) {
		super(name);
		this.opts = opts;
		this.value = opts[0];
		this.dflt = dflt;
		Preferences prefs = AppPreferences.getPrefs();
		set(prefs.get(name, dflt));
		prefs.addPreferenceChangeListener(this);
	}
	
	public String get() {
		return value;
	}
	
	public void set(String newValue) {
		String oldValue = value;
		if (!isSame(oldValue, newValue)) {
			AppPreferences.getPrefs().put(getIdentifier(), newValue);
		}
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		Preferences prefs = event.getNode();
		String prop = event.getKey();
		String name = getIdentifier();
		if (prop.equals(name)) {
			String oldValue = value;
			String newValue = prefs.get(name, dflt);
			if (!isSame(oldValue, newValue)) {
				String[] o = opts;
				String chosen = null;
				for (int i = 0; i < o.length; i++) {
					if (isSame(o[i], newValue)) { chosen = o[i]; break; }
				}
				if (chosen == null) chosen = dflt;
				value = chosen;
				AppPreferences.firePropertyChange(name, oldValue, chosen);
			}
		}
	}
	
	private static boolean isSame(String a, String b) {
		return a == null ? b == null : a.equals(b);
	}
}
