/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;

public class CaretEvent {
	private Caret caret;
	private String oldtext;
	private String newtext;

	public CaretEvent(Caret caret, String oldtext, String newtext) {
		this.caret = caret;
		this.oldtext = oldtext;
		this.newtext = newtext;
	}

	public Caret getCaret() {
		return caret;
	}

	public String getOldText() {
		return oldtext;
	}

	public String getText() {
		return newtext;
	}
}
