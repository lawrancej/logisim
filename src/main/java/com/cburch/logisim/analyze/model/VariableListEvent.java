/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.model;

public class VariableListEvent {
	public static final int ALL_REPLACED = 0;
	public static final int ADD = 1;
	public static final int REMOVE = 2;
	public static final int MOVE = 3;
	public static final int REPLACE = 4;
	
	private VariableList source;
	private int type;
	private String variable;
	private Object data;
	
	public VariableListEvent(VariableList source, int type, String variable, Object data) {
		this.source = source;
		this.type = type;
		this.variable = variable;
		this.data = data;
	}
	
	public VariableList getSource() {
		return source;
	}
	
	public int getType() {
		return type;
	}
	
	public String getVariable() {
		return variable;
	}
	
	public Object getData() {
		return data;
	}
}
