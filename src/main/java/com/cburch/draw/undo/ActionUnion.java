/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.undo;

class ActionUnion extends Action {
	Action first;
	Action second;

	ActionUnion(Action first, Action second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean isModification() {
		return first.isModification() || second.isModification();
	}

	@Override
	public String getName() { return first.getName(); }

	@Override
	public void doIt() {
		first.doIt();
		second.doIt();
	}

	@Override
	public void undo() {
		second.undo();
		first.undo();
	}
}
