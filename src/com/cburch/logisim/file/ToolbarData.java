/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;


import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.AttributeSets;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.EventSourceWeakSupport;

public class ToolbarData {
	public static interface ToolbarListener {
		public void toolbarChanged();
	}

	private EventSourceWeakSupport<ToolbarListener> listeners;
	private EventSourceWeakSupport<AttributeListener> toolListeners;
	private ArrayList<Tool> contents;

	public ToolbarData() {
		listeners = new EventSourceWeakSupport<ToolbarListener>();
		toolListeners = new EventSourceWeakSupport<AttributeListener>();
		contents = new ArrayList<Tool>();
	}

	//
	// listener methods
	//
	public void addToolbarListener(ToolbarListener l) {
		listeners.add(l);
	}

	public void removeToolbarListener(ToolbarListener l) {
		listeners.remove(l);
	}
	
	public void addToolAttributeListener(AttributeListener l) {
		for (Tool tool : contents) {
			if (tool != null) {
				AttributeSet attrs = tool.getAttributeSet();
				if (attrs != null) attrs.addAttributeListener(l);
			}
		}
		toolListeners.add(l);
	}
	
	public void removeToolAttributeListener(AttributeListener l) {
		for (Tool tool : contents) {
			if (tool != null) {
				AttributeSet attrs = tool.getAttributeSet();
				if (attrs != null) attrs.removeAttributeListener(l);
			}
		}
		toolListeners.remove(l);
	}

	private void addAttributeListeners(Tool tool) {
		for (AttributeListener l : toolListeners) {
			AttributeSet attrs = tool.getAttributeSet();
			if (attrs != null) attrs.addAttributeListener(l);
		}
	}

	private void removeAttributeListeners(Tool tool) {
		for (AttributeListener l : toolListeners) {
			AttributeSet attrs = tool.getAttributeSet();
			if (attrs != null) attrs.removeAttributeListener(l);
		}
	}

	public void fireToolbarChanged() {
		for (ToolbarListener l : listeners) {
			l.toolbarChanged();
		}
	}

	//
	// query methods
	//
	public List<Tool> getContents() {
		return contents;
	}

	public Tool getFirstTool() {
		for (Tool tool : contents) {
			if (tool != null) return tool;
		}
		return null;
	}
	
	public int size() {
		return contents.size();
	}
	
	public Object get(int index) {
		return contents.get(index);
	}

	//
	// modification methods
	//
	public void copyFrom(ToolbarData other, LogisimFile file) {
		if (this == other) return;
		for (Tool tool : contents) {
			if (tool != null) {
				removeAttributeListeners(tool);
			}
		}
		this.contents.clear();
		for (Tool srcTool : other.contents) {
			if (srcTool == null) {
				this.addSeparator();
			} else {
				Tool toolCopy = file.findTool(srcTool);
				if (toolCopy != null) {
					Tool dstTool = toolCopy.cloneTool();
					AttributeSets.copy(srcTool.getAttributeSet(),
							dstTool.getAttributeSet());
					this.addTool(dstTool);
					addAttributeListeners(toolCopy);
				}
			}
		}
		fireToolbarChanged();
	}

	public void addSeparator() {
		contents.add(null);
		fireToolbarChanged();
	}

	public void addTool(Tool tool) {
		contents.add(tool);
		addAttributeListeners(tool);
		fireToolbarChanged();
	}

	public void addTool(int pos, Tool tool) {
		contents.add(pos, tool);
		addAttributeListeners(tool);
		fireToolbarChanged();
	}

	public void addSeparator(int pos) {
		contents.add(pos, null);
		fireToolbarChanged();
	}

	public Object move(int from, int to) {
		Tool moved = contents.remove(from);
		contents.add(to, moved);
		fireToolbarChanged();
		return moved;
	}

	public Object remove(int pos) {
		Object ret = contents.remove(pos);
		if (ret instanceof Tool) removeAttributeListeners((Tool) ret);
		fireToolbarChanged();
		return ret;
	}

	boolean usesToolFromSource(Tool query) {
		for (Tool tool : contents) {
			if (tool != null && tool.sharesSource(query)) return true;
		}
		return false;
	}

	//
	// package-protected methods
	//
	void replaceAll(Map<Tool,Tool> toolMap) {
		boolean changed = false;
		for (ListIterator<Tool> it = contents.listIterator(); it.hasNext(); ) {
			Object old = it.next();
			if (toolMap.containsKey(old)) {
				changed = true;
				removeAttributeListeners((Tool) old);
				Tool newTool = toolMap.get(old);
				if (newTool == null) {
					it.remove();
				} else {
					Tool addedTool = newTool.cloneTool();
					addAttributeListeners(addedTool);
					LoadedLibrary.copyAttributes(addedTool.getAttributeSet(),
							((Tool) old).getAttributeSet());
					it.set(addedTool);
				}
			}
		}
		if (changed) fireToolbarChanged();
	}
}
