/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import com.cburch.logisim.gui.menu.LogisimMenuItem;

public abstract class EditHandler {
	public static interface Listener {
		void enableChanged(EditHandler handler, LogisimMenuItem action, boolean value);
	}
	
	private Listener listener;
	
	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	protected void setEnabled(LogisimMenuItem action, boolean value) {
		Listener l = listener;
		if (l != null) {
			l.enableChanged(this, action, value);
		}
	}
	
	public abstract void computeEnabled();
	public abstract void cut();
	public abstract void copy();
	public abstract void paste();
	public abstract void delete();
	public abstract void duplicate();
	public abstract void selectAll();
	public abstract void raise();
	public abstract void lower();
	public abstract void raiseTop();
	public abstract void lowerBottom();
	public abstract void addControlPoint();
	public abstract void removeControlPoint();
}
