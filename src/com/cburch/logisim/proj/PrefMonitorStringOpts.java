/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.proj;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

class PrefMonitorStringOpts implements PreferenceChangeListener {
    private String name;
    private String[] opts;
    private String value;
    
    PrefMonitorStringOpts(String name, String[] opts) {
        this.name = name;
        this.opts = opts;
        this.value = opts[0];
        Preferences prefs = LogisimPreferences.getPrefs();
        set(prefs.get(name, opts[0]));
        prefs.addPreferenceChangeListener(this);
    }
    
    public String get() {
        return value;
    }
    
    public void set(String newValue) {
        String oldValue = value;
        if (!isSame(oldValue, newValue)) {
            LogisimPreferences.getPrefs().put(name, newValue);
        }
    }

    public void preferenceChange(PreferenceChangeEvent event) {
        Preferences prefs = event.getNode();
        String prop = event.getKey();
        if (prop.equals(name)) {
            String oldValue = value;
            String newValue = prefs.get(name, opts[0]);
            if (!isSame(oldValue, newValue)) {
                String[] o = opts;
                String intern = null;
                for (int i = 0; i < o.length; i++) {
                    if (isSame(o[i], newValue)) { intern = o[i]; break; }
                }
                if (intern == null) intern = o[0];
                value = intern;
                LogisimPreferences.firePropertyChange(name, oldValue, intern);
            }
        }
    }
    
    private static boolean isSame(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
