/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.generic;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;

class ProjectExplorerModel extends DefaultTreeModel implements ProjectListener {
	static abstract class Node<T> extends DefaultMutableTreeNode {
		ProjectExplorerModel model;
		int oldIndex;
		int newIndex;
		
		Node(ProjectExplorerModel model, T userObject) {
			super(userObject);
			this.model = model;
		}
		
		ProjectExplorerModel getModel() {
			return model;
		}
		
		abstract Node<T> create(T userObject);
		
		public T getValue() {
			@SuppressWarnings("unchecked") T val = (T) getUserObject();
			return val;
		}
		
		abstract void decommission();
		
		void fireNodeChanged() {
			Node<?> parent = (Node<?>) this.getParent();
			if (parent == null) {
				model.fireTreeStructureChanged(this, this.getPath(), null, null);
			} else {
				int[] indices = new int[] { parent.getIndex(this) };
				Object[] items = new Object[] { this.getUserObject() };
				model.fireTreeNodesChanged(this, parent.getPath(), indices, items);
			}
		}
		
		void fireNodesChanged(int[] indices, Node<?>[] children) {
			model.fireTreeNodesChanged(model, this.getPath(), indices, children);
		}
		
		void fireNodesInserted(int[] indices, Node<?>[] children) {
			model.fireTreeNodesInserted(model, this.getPath(), indices, children);
		}
		
		void fireNodesRemoved(int[] indices, Node<?>[] children) {
			model.fireTreeNodesRemoved(model, this.getPath(), indices, children);
		}

		void fireStructureChanged() {
			model.fireStructureChanged();
		}
	}
	
	private Project proj;
	
	ProjectExplorerModel(Project proj) {
		super(null);
		this.proj = proj;
		setRoot(new ProjectExplorerLibraryNode(this, proj.getLogisimFile()));
		proj.addProjectListener(this);
	}
	
	public void setProject(Project value) {
		Project old = proj;
		if (old != null) {
			old.removeProjectListener(this);
		}
		setLogisimFile(null);
		proj = value;
		if (value != null) {
			value.addProjectListener(this);
			setLogisimFile(value.getLogisimFile());
		}
	}
	
	private void setLogisimFile(LogisimFile file) {
		Node<?> oldRoot = (Node<?>) getRoot();
		oldRoot.decommission();
		if (file == null) {
			setRoot(null);
		} else {
			setRoot(new ProjectExplorerLibraryNode(this, file));
		}
		fireStructureChanged();
	}
	
	void fireStructureChanged() {
		Node<?> root = (Node<?>) getRoot();
		if (root != null) {
			this.fireTreeStructureChanged(this, root.getUserObjectPath(), null, null);
		} else {
			this.fireTreeStructureChanged(this, null, null, null);
		}
	}

	// ProjectListener methods
	public void projectChanged(ProjectEvent event) {
		int act = event.getAction();
		if (act == ProjectEvent.ACTION_SET_FILE) {
			setLogisimFile(proj.getLogisimFile());
		}
	}
}
