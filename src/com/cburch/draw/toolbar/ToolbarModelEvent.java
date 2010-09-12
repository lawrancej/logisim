/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.toolbar;

import java.util.EventObject;

public class ToolbarModelEvent extends EventObject {
	public ToolbarModelEvent(ToolbarModel model) {
		super(model);
	}
}
