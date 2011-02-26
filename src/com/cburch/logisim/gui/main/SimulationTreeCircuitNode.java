/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import com.cburch.logisim.circuit.CircuitAttributes;
import com.cburch.logisim.circuit.CircuitEvent;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.SubcircuitFactory;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.instance.StdAttr;

class SimulationTreeCircuitNode extends SimulationTreeNode
		implements CircuitListener, AttributeListener, Comparator<Component> {
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
		if (subcircComp != null) {
			subcircComp.getAttributeSet().addAttributeListener(this);
		} else {
			circuitState.getCircuit().getStaticAttributes().addAttributeListener(this);
		}
		computeChildren();
	}
	
	public CircuitState getCircuitState() {
		return circuitState;
	}
	
	@Override
	public ComponentFactory getComponentFactory() {
		return circuitState.getCircuit().getSubcircuitFactory();
	}
	
	@Override
	public boolean isCurrentView(SimulationTreeModel model) {
		return model.getCurrentView() == circuitState;
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

	@Override
	public TreeNode getChildAt(int index) {
		return children.get(index);
	}

	@Override
	public int getChildCount() {
		return children.size();
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public Enumeration<TreeNode> children() {
		return Collections.enumeration(children);
	}

	public void circuitChanged(CircuitEvent event) {
		int action = event.getAction();
		if (action == CircuitEvent.ACTION_SET_NAME) {
			model.fireNodeChanged(this);
		} else {
			if (computeChildren()) {
				model.fireStructureChanged(this);
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
				TreeNode toAdd = model.mapComponentToNode(comp);
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
				if (o instanceof SimulationTreeCircuitNode) {
					SimulationTreeCircuitNode n = (SimulationTreeCircuitNode) o;
					if (n.circuitState == state) { toAdd = n; break; }
				}
			}
			if (toAdd == null) {
				toAdd = new SimulationTreeCircuitNode(model, this, state, comp);
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

	//
	// AttributeListener methods
	public void attributeListChanged(AttributeEvent e) { }

	public void attributeValueChanged(AttributeEvent e) {
		Object attr = e.getAttribute();
		if (attr == CircuitAttributes.CIRCUIT_LABEL_ATTR || attr == StdAttr.LABEL) {
			model.fireNodeChanged(this);
		}
	}
}
