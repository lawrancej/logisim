/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.model;

public class OutputExpressionsEvent {
	public static final int ALL_VARIABLES_REPLACED = 0;
	public static final int OUTPUT_EXPRESSION = 1;
	public static final int OUTPUT_MINIMAL = 2;
	
	private AnalyzerModel model;
	private int type;
	private String variable;
	private Object data;
	
	public OutputExpressionsEvent(AnalyzerModel model, int type, String variable, Object data) {
		this.model = model;
		this.type = type;
		this.variable = variable;
		this.data = data;
	}
	
	public AnalyzerModel getModel() {
		return model;
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
