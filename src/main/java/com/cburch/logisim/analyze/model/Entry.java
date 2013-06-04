/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.model;


import static com.cburch.logisim.util.LocaleString.*;

public class Entry {
	public static final Entry ZERO = new Entry("0",null);
	public static final Entry ONE = new Entry("1",null);
	public static final Entry DONT_CARE = new Entry("x",null);
	public static final Entry BUS_ERROR = new Entry(null,__("busError"));
	public static final Entry OSCILLATE_ERROR = new Entry(null,__("oscillateError"));
	
	public static Entry parse(String description) {
		if (ZERO.description.equals(description)) return ZERO;
		if (ONE.description.equals(description)) return ONE;
		if (DONT_CARE.description.equals(description)) return DONT_CARE;
		if (BUS_ERROR.description.equals(description)) return BUS_ERROR;
		return null;
	}
	
	private String description;
	private String errorMessage;
	
	private Entry(String description, String errorMessage) {
		if (description == null){
			this.description = "!!";
		} else{
			this.description = description;
		}
		if (errorMessage == null){
			this.errorMessage = null;
			} else {
			this.errorMessage = errorMessage;
		}
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isError() {
		return errorMessage != null;
	}
	
	public String getErrorMessage() {
		return errorMessage == null ? null : errorMessage.toString();
	}
	
	@Override
	public String toString() {
		return "Entry[" + description + "]";
	}
}
