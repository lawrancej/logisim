/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools.key;

import java.awt.event.KeyEvent;

import com.cburch.logisim.data.AttributeSet;

public class KeyConfigurationEvent {
	public static final int KEY_PRESSED = 0;
	public static final int KEY_RELEASED = 1;
	public static final int KEY_TYPED = 2;
	
	private int type;
	private AttributeSet attrs;
	private KeyEvent event;
	private Object data;
	private boolean consumed;
	
	public KeyConfigurationEvent(int type, AttributeSet attrs, KeyEvent event, Object data) {
		this.type = type;
		this.attrs = attrs;
		this.event = event;
		this.data = data;
		this.consumed = false;
	}
	
	public int getType() {
		return type;
	}
	
	public KeyEvent getKeyEvent() {
		return event;
	}
	
	public AttributeSet getAttributeSet() {
		return attrs;
	}
	
	public void consume() {
		consumed = true;
	}
	
	public boolean isConsumed() {
		return consumed;
	}

	public Object getData() {
		return data;
	}
}
