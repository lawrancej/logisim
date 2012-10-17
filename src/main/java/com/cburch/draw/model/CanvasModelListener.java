/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import java.util.EventListener;

public interface CanvasModelListener extends EventListener {
	public void modelChanged(CanvasModelEvent event);
}
