/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.proj;

public abstract class Action {
	public boolean isModification() { return true; }

	public abstract String getName();

	public abstract void doIt(Project proj);

	public abstract void undo(Project proj);

	public boolean shouldAppendTo(Action other) { return false; }

	public Action append(Action other) {
		return new JoinedAction(this, other);
	}
}
