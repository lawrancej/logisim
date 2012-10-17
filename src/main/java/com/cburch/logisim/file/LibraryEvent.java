/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

import com.cburch.logisim.tools.Library;

public class LibraryEvent {
	public final static int ADD_TOOL = 0;
	public final static int REMOVE_TOOL = 1;
	public final static int MOVE_TOOL = 2;
	public final static int ADD_LIBRARY = 3;
	public final static int REMOVE_LIBRARY = 4;
	public final static int SET_MAIN = 5;
	public final static int SET_NAME = 6;
	public static final int DIRTY_STATE = 7;

	private Library source;
	private int action;
	private Object data;

	LibraryEvent(Library source, int action, Object data) {
		this.source = source;
		this.action = action;
		this.data = data;
	}
	
	public Library getSource() {
		return source;
	}

	public int getAction() {
		return action;
	}

	public Object getData() {
		return data;
	}

}
