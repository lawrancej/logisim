/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.cburch.logisim.circuit.CircuitEvent;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.SubcircuitFactory;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.instance.StdAttr;

class SimulationTreeCircuitNode {
		//TODO implements TreeNode, CircuitListener, Comparator<Component> {
	/* TODO
	private static class CompareByName implements Comparator<Object> {
		public int compare(Object a, Object b) {
			return a.toString().compareToIgnoreCase(b.toString());
		}
	}

	private SimulationTreeModel model;
	private SimulationTreeCircuitNode parent;
	private CircuitState circuitState;
	private Component subcircComp;
	private ArrayList<TreeNode> children;
		
	public SimulationTreeCircuitNode(SimulationTreeModel model,
			SimulationTreeCircuitNode parent, CircuitState circuitState,
			Component subcircComp) {
		this.model = model;
		this.parent = parent;
		this.circuitState = circuitState;
		this.subcircComp = subcircComp;
		this.children = new ArrayList<TreeNode>();
		circuitState.getCircuit().addCircuitListener(this);
		computeChildren();
	}
	
	@Override
	public String toString() {
		if (subcircComp != null) {
			String label = subcircComp.getAttributeSet().getValue(StdAttr.LABEL);
			if (label != null && !label.equals("")) {
				return label;
			}
		}
		String ret = circuitState.getCircuit().getName();
		if (subcircComp != null) {
			ret += subcircComp.getLocation();
		}
		return ret;
	}

	public TreeNode getChildAt(int index) {
		return children.get(index);
	}

	public int getChildCount() {
		return children.size();
	}

	public TreeNode getParent() {
		return parent;
	}

	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public boolean isLeaf() {
		return false;
	}

	public Enumeration<TreeNode> children() {
		return Collections.enumeration(children);
	}

	public void circuitChanged(CircuitEvent event) {
		int action = event.getAction();
		if (action == CircuitEvent.ACTION_SET_NAME) {
			model.fireNodeChanged(this);
		} else {
			if (computeChildren()) {
				model.fireNodeStructureChanged(this);
			} else if (action == CircuitEvent.ACTION_INVALIDATE) {
				Object o = event.getData();
				for (int i = children.size() - 1; i >= 0; i--) {
					Object o2 = children.get(i);
					if (o2 instanceof ComponentNode) {
						ComponentNode n = (ComponentNode) o2;
						if (n.comp == o) {
							int[] changed = { i };
							children.remove(i);
							model.nodesWereRemoved(this, changed, new Object[] { n });
							children.add(i, new ComponentNode(this, n.comp));
							model.nodesWereInserted(this, changed);
						}
					}
				}
			}
		}
	}
	
	// returns true if changed
	private boolean computeChildren() {
		ArrayList<TreeNode> newChildren = new ArrayList<TreeNode>();
		ArrayList<Component> subcircs = new ArrayList<Component>();
		for (Component comp : circuitState.getCircuit().getNonWires()) {
			if (comp.getFactory() instanceof SubcircuitFactory) {
				subcircs.add(comp);
			} else {
				Object toAdd = model.mapComponentToNode(comp);
				if (toAdd != null) {
					newChildren.add(toAdd);
				}
			}
		}
		Collections.sort(newChildren, new CompareByName());
		Collections.sort(subcircs, this);
		for (Component comp : subcircs) {
			SubcircuitFactory factory = (SubcircuitFactory) comp.getFactory();
			CircuitState state = factory.getSubstate(circuitState, comp);
			SimulationTreeCircuitNode toAdd = null;
			for (TreeNode o : children) {
				if (o instanceof CircuitNode) {
					CircuitNode n = (CircuitNode) o;
					if (n.circuitState == state) { toAdd = n; break; }
				}
			}
			if (toAdd == null) {
				toAdd = new CircuitNode(this, state, comp);
			}
			newChildren.add(toAdd);
		}
		
		if (!children.equals(newChildren)) {
			children = newChildren;
			return true;
		} else {
			return false;
		}
	}
	
	public int compare(Component a, Component b) {
		if (a != b) {
			String aName = a.getFactory().getDisplayName();
			String bName = b.getFactory().getDisplayName();
			int ret = aName.compareToIgnoreCase(bName);
			if (ret != 0) return ret;
		}
		return a.getLocation().toString().compareTo(b.getLocation().toString());
	}
	*/
}
