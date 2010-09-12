/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

public class LogisimMenuItem {
	private String name;
	
	LogisimMenuItem(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
