/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import java.util.HashMap;
import java.util.Map;

import com.cburch.logisim.gui.main.EditHandler;
import com.cburch.logisim.gui.menu.EditPopup;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.gui.menu.LogisimMenuItem;

public class AppearanceEditPopup extends EditPopup implements EditHandler.Listener {
	private AppearanceCanvas canvas;
	private EditHandler handler;
	private Map<LogisimMenuItem, Boolean> enabled; 
	
	public AppearanceEditPopup(AppearanceCanvas canvas) {
		super(true);
		this.canvas = canvas;
		handler = new AppearanceEditHandler(canvas);
		handler.setListener(this);
		enabled = new HashMap<LogisimMenuItem, Boolean>();
		handler.computeEnabled();
		initialize();
	}
	
	public void enableChanged(EditHandler handler, LogisimMenuItem action,
			boolean value) {
		enabled.put(action, Boolean.valueOf(value));
	}


	@Override
	protected boolean shouldShow(LogisimMenuItem item) {
		if (item == LogisimMenuBar.ADD_CONTROL || item == LogisimMenuBar.REMOVE_CONTROL) {
			return canvas.getSelection().getSelectedHandle() != null;
		} else {
			return true;
		}
	}

	@Override
	protected boolean isEnabled(LogisimMenuItem item) {
		Boolean value = enabled.get(item);
		return value != null && value.booleanValue();
	}

	@Override
	protected void fire(LogisimMenuItem item) {
		if (item == LogisimMenuBar.CUT) {
			handler.cut();
		} else if (item == LogisimMenuBar.COPY) {
			handler.copy();
		} else if (item == LogisimMenuBar.DELETE) {
			handler.delete();
		} else if (item == LogisimMenuBar.DUPLICATE) {
			handler.duplicate();
		} else if (item == LogisimMenuBar.RAISE) {
			handler.raise();
		} else if (item == LogisimMenuBar.LOWER) {
			handler.lower();
		} else if (item == LogisimMenuBar.RAISE_TOP) {
			handler.raiseTop();
		} else if (item == LogisimMenuBar.LOWER_BOTTOM) {
			handler.lowerBottom();
		} else if (item == LogisimMenuBar.ADD_CONTROL) {
			handler.addControlPoint();
		} else if (item == LogisimMenuBar.REMOVE_CONTROL) {
			handler.removeControlPoint();
		}
	}
}
