/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.model;

import com.cburch.logisim.util.StringGetter;
import static com.cburch.logisim.util.LocaleString.*;

public class Entry {
	public static final Entry ZERO = new Entry("0");
	public static final Entry ONE = new Entry("1");
	public static final Entry DONT_CARE = new Entry("x");
	public static final Entry BUS_ERROR = new Entry(__("busError"));
	public static final Entry OSCILLATE_ERROR = new Entry(__("oscillateError"));
	
	public static Entry parse(String description) {
		if (ZERO.description.equals(description)) return ZERO;
		if (ONE.description.equals(description)) return ONE;
		if (DONT_CARE.description.equals(description)) return DONT_CARE;
		if (BUS_ERROR.description.equals(description)) return BUS_ERROR;
		return null;
	}
	
	private String description;
	private StringGetter errorMessage;
	
	private Entry(String description) {
		this.description = description;
		this.errorMessage = null;
	}
	
	private Entry(StringGetter errorMessage) {
		this.description = "!!";
		this.errorMessage = errorMessage;
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
