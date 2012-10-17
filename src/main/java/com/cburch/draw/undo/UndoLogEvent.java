/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.undo;

import java.util.EventObject;

public class UndoLogEvent extends EventObject {
	public static final int ACTION_DONE = 0;
	public static final int ACTION_UNDONE = 1;
	
	private int action;
	private Action actionObject;
	
	public UndoLogEvent(UndoLog source, int action, Action actionObject) {
		super(source);
		this.action = action;
		this.actionObject = actionObject;
	}
	
	public UndoLog getUndoLog() {
		return (UndoLog) getSource();
	}
	
	public int getAction() {
		return action;
	}
	
	public Action getActionObject() {
		return actionObject;
	}
}
