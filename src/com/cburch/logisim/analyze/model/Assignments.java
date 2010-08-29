/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.model;

import java.util.HashMap;
import java.util.Map;

class Assignments {
	private Map<String,Boolean> map = new HashMap<String,Boolean>();
	
	public Assignments() { }
	
	public boolean get(String variable) {
		Boolean value = map.get(variable);
		return value != null ? value.booleanValue() : false;
	}
	
	public void put(String variable, boolean value) {
		map.put(variable, Boolean.valueOf(value));
	}
}
