/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.opts;

import javax.swing.JComboBox;

import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.util.StringGetter;

class ComboOption {
	private Object value;
	private StringGetter getter;
	
	ComboOption(String value, StringGetter getter) {
		this.value = value;
		this.getter = getter;
	}
	
	ComboOption(AttributeOption value) {
		this.value = value;
		this.getter = null;
	}
	
	@Override
	public String toString() {
		if (getter != null) return getter.toString();
		if (value instanceof AttributeOption) return ((AttributeOption) value).toDisplayString();
		return "???";
	}
	
	public Object getValue() {
		return value;
	}
	
	static void setSelected(JComboBox combo, Object value) {
		for (int i = combo.getItemCount() - 1; i >= 0; i--) {
			ComboOption opt = (ComboOption) combo.getItemAt(i);
			if (opt.getValue().equals(value)) {
				combo.setSelectedItem(opt);
				return;
			}
		}
		combo.setSelectedItem(combo.getItemAt(0));
	}

}
