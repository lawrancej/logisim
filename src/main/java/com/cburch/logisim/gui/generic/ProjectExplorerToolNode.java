/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.generic;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitEvent;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.circuit.SubcircuitFactory;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Tool;

public class ProjectExplorerToolNode extends ProjectExplorerModel.Node<Tool>
		implements CircuitListener {
	private Circuit circuit;
	
	public ProjectExplorerToolNode(ProjectExplorerModel model, Tool tool) {
		super(model, tool);
		if (tool instanceof AddTool) {
			Object factory = ((AddTool) tool).getFactory();
			if (factory instanceof SubcircuitFactory) {
				circuit = ((SubcircuitFactory) factory).getSubcircuit();
				circuit.addCircuitListener(this);
			}
		}
	}
	
	@Override ProjectExplorerToolNode create(Tool userObject) {
		return new ProjectExplorerToolNode(getModel(), userObject);
	}
	
	@Override void decommission() {
		if (circuit != null) {
			circuit.removeCircuitListener(this);
		}
	}

	public void circuitChanged(CircuitEvent event) {
		int act = event.getAction();
		if (act == CircuitEvent.ACTION_SET_NAME) {
			fireStructureChanged();
			// The following almost works - but the labels aren't made
			// bigger, so you get "..." behavior with longer names.
			// fireNodesChanged(findPath(this));
		}
	}
}
