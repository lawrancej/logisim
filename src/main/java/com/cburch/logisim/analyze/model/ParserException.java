/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.model;

@SuppressWarnings("serial")
public  class ParserException extends Exception {
    private String message;
    private int start;
    private int length;

    public ParserException(String message, int start, int length) {
        super(message.toString());
        this.message = message;
        this.start = start;
        this.length = length;
    }

    @Override
    public String getMessage() {
        return message.toString();
    }

    public String getMessageGetter() {
        return message;
    }

    public int getOffset() {
        return start;
    }

    public int getEndOffset() {
        return start + length;
    }
}