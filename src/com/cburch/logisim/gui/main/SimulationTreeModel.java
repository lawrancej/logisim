/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.util.ArrayList;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.cburch.logisim.circuit.Simulator;
import com.cburch.logisim.comp.Component;

public class SimulationTreeModel implements TreeModel {
	private ArrayList<TreeModelListener> listeners;
	private SimulationTreeCircuitNode root;
	
	public SimulationTreeModel(Simulator simulator) {
		this.listeners = new ArrayList<TreeModelListener>();
		//TODO this.root = new SimulationTreeCircuitNode(this, null,
		//		simulator.getCircuitState(), null);
	}
	
	protected Object mapComponentToNode(Component comp) {
		return null;
	}

	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}
	
	protected void fireNodeChanged(Object node) {
		TreeModelEvent e = new TreeModelEvent(this, findPath(node));
		for (TreeModelListener l : listeners) {
			l.treeNodesChanged(e);
		}
	}
	
	private TreePath findPath(Object node) {
		ArrayList<Object> path = new ArrayList<Object>();
		Object current = node;
		while (current instanceof TreeNode) {
			path.add(0, current);
			current = ((TreeNode) current).getParent();
		}
		if (current != null) {
			path.add(0, current);
		}
		return new TreePath(path.toArray());
	}

	public Object getRoot() {
		return root;
	}

	public int getChildCount(Object parent) {
		if (parent instanceof TreeNode) {
			return ((TreeNode) parent).getChildCount();
		} else {
			return 0;
		}
	}

	public Object getChild(Object parent, int index) {
		if (parent instanceof TreeNode) {
			return ((TreeNode) parent).getChildAt(index);
		} else {
			return null;
		}
	}

	public int getIndexOfChild(Object parent, Object child) {
		if (parent instanceof TreeNode && child instanceof TreeNode) {
			return ((TreeNode) parent).getIndex((TreeNode) child);
		} else {
			return -1;
		}
	}

	public boolean isLeaf(Object node) {
		if (node instanceof TreeNode) {
			return ((TreeNode) node).getChildCount() == 0;
		} else {
			return true;
		}
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		throw new UnsupportedOperationException();
	}
}
