/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import javax.swing.JOptionPane;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.gui.generic.AttributeTable;
import com.cburch.logisim.gui.generic.AttributeTableListener;
import com.cburch.logisim.proj.Project;

public class CircuitAttributeListener implements AttributeTableListener {
	private Project proj;
	private Circuit circ;
	
	public CircuitAttributeListener(Project proj, Circuit circ) {
		this.proj = proj;
		this.circ = circ;
	}
	
	public void valueChangeRequested(AttributeTable table,
			AttributeSet attrs, Attribute<?> attr, Object value) {
		if (!proj.getLogisimFile().contains(circ)) {
			JOptionPane.showMessageDialog(proj.getFrame(),
				Strings.get("cannotModifyCircuitError"));
		} else {
			CircuitMutation xn = new CircuitMutation(circ);
			xn.setForCircuit(attr, value);
			proj.doAction(xn.toAction(Strings.getter("changeCircuitAttrAction")));
		}
	}
}

