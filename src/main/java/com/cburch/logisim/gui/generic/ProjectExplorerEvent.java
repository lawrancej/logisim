/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.generic;

import javax.swing.tree.TreePath;

public class ProjectExplorerEvent {
	private TreePath path;
	
	public ProjectExplorerEvent(TreePath path) {
		this.path = path;
	}
	
	public TreePath getTreePath() {
		return path;
	}
	
	public Object getTarget() {
		return path == null ? null : path.getLastPathComponent();
	}
}
