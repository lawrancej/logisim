/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.prefs;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

abstract class AbstractPrefMonitor<E> implements PrefMonitor<E> {
    private String name;

    AbstractPrefMonitor(String name) {
        this.name = name;
    }

    @Override
    public String getIdentifier() {
        return name;
    }

    @Override
    public boolean isSource(PropertyChangeEvent event) {
        return name.equals(event.getPropertyName());
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        AppPreferences.addPropertyChangeListener(name, listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        AppPreferences.removePropertyChangeListener(name, listener);
    }

    @Override
    public boolean getBoolean() {
        return ((Boolean) get()).booleanValue();
    }

    @Override
    public void setBoolean(boolean value) {
        @SuppressWarnings("unchecked")
        E valObj = (E) Boolean.valueOf(value);
        set(valObj);
    }
}
