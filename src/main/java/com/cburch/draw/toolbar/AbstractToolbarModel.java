/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.toolbar;

import com.cburch.draw.toolbar.ToolbarItem;
import com.cburch.draw.toolbar.ToolbarModel;
import com.cburch.draw.toolbar.ToolbarModelEvent;
import com.cburch.draw.toolbar.ToolbarModelListener;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractToolbarModel implements ToolbarModel {
	private List<ToolbarModelListener> listeners;

	public AbstractToolbarModel() {
		listeners = new ArrayList<ToolbarModelListener>();
	}
	
	public void addToolbarModelListener(ToolbarModelListener listener) {
		listeners.add(listener);
	}
	
	public void removeToolbarModelListener(ToolbarModelListener listener) {
		listeners.remove(listener);
	}
	
	protected void fireToolbarContentsChanged() {
		ToolbarModelEvent event = new ToolbarModelEvent(this);
		for (ToolbarModelListener listener : listeners) {
			listener.toolbarContentsChanged(event);
		}
	}
	
	protected void fireToolbarAppearanceChanged() {
		ToolbarModelEvent event = new ToolbarModelEvent(this);
		for (ToolbarModelListener listener : listeners) {
			listener.toolbarAppearanceChanged(event);
		}
	}
	
	public abstract List<ToolbarItem> getItems();
	
	public abstract boolean isSelected(ToolbarItem item);
	
	public abstract void itemSelected(ToolbarItem item);
}
