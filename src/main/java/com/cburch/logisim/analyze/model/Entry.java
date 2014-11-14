/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.model;

import static com.cburch.logisim.util.LocaleString.*;

public class Entry {
    public static final Entry ZERO = new Entry("0");
    public static final Entry ONE = new Entry("1");
    public static final Entry DONT_CARE = new Entry("x");
    public static final Entry BUS_ERROR = new Entry(getFromLocale("busError"), true);
    public static final Entry OSCILLATE_ERROR = new Entry(getFromLocale("oscillateError"), true);

    public static Entry parse(String description) {
        if (ZERO.description.equals(description)) {
            return ZERO;
        }

        if (ONE.description.equals(description)) {
            return ONE;
        }

        if (DONT_CARE.description.equals(description)) {
            return DONT_CARE;
        }

        if (BUS_ERROR.description.equals(description)) {
            return BUS_ERROR;
        }

        return null;
    }

    private String description;
    private String errorMessage;

    private Entry(String message, boolean... isError) {
    	if (isError.length > 0 && isError[0]) {
    		this.description = "!!";
            this.errorMessage = message;
    	} else {
    		this.description = message;
    		this.errorMessage = null;
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
