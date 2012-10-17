/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.toolbar;

public interface ToolbarModelListener {
	public void toolbarContentsChanged(ToolbarModelEvent event);
	public void toolbarAppearanceChanged(ToolbarModelEvent event);
}
