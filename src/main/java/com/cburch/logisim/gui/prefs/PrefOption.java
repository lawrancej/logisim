/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

import javax.swing.JComboBox;

import com.cburch.logisim.util.StringGetter;

class PrefOption {
	private Object value;
	private StringGetter getter;
	
	PrefOption(String value, StringGetter getter) {
		this.value = value;
		this.getter = getter;
	}
	
	@Override
	public String toString() {
		return getter.toString();
	}
	
	public Object getValue() {
		return value;
	}
	
	static void setSelected(JComboBox combo, Object value) {
		for (int i = combo.getItemCount() - 1; i >= 0; i--) {
			PrefOption opt = (PrefOption) combo.getItemAt(i);
			if (opt.getValue().equals(value)) {
				combo.setSelectedItem(opt);
				return;
			}
		}
		combo.setSelectedItem(combo.getItemAt(0));
	}

}
