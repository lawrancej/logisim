/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.proj;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

class PrefMonitorBoolean implements PreferenceChangeListener {
    private String name;
    private boolean dflt;
    private boolean value;
    
    PrefMonitorBoolean(String name, boolean dflt) {
        this.name = name;
        this.dflt = dflt;
        this.value = dflt;
        Preferences prefs = LogisimPreferences.getPrefs();
        set(prefs.getBoolean(name, dflt));
        prefs.addPreferenceChangeListener(this);
    }
    
    public boolean get() {
        return value;
    }
    
    public void set(boolean newValue) {
        if (value != newValue) {
            LogisimPreferences.getPrefs().putBoolean(name, newValue);
        }
    }

    public void preferenceChange(PreferenceChangeEvent event) {
        Preferences prefs = event.getNode();
        String prop = event.getKey();
        if (prop.equals(name)) {
            boolean oldValue = value;
            boolean newValue = prefs.getBoolean(name, dflt);
            if (newValue != oldValue) {
                value = newValue;
                LogisimPreferences.firePropertyChange(name, oldValue, newValue);
            }
        }
    }
}
