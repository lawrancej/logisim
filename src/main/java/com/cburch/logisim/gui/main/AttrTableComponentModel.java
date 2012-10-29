/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.gui.generic.AttrTableSetException;
import com.cburch.logisim.gui.generic.AttributeSetTableModel;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.SetAttributeAction;
import static com.cburch.logisim.util.LocaleString.*;

class AttrTableComponentModel extends AttributeSetTableModel {
	Project proj;
	Circuit circ;
	Component comp;

	AttrTableComponentModel(Project proj, Circuit circ, Component comp) {
		super(comp.getAttributeSet());
		this.proj = proj;
		this.circ = circ;
		this.comp = comp;
	}
	
	public Circuit getCircuit() {
		return circ;
	}
	
	public Component getComponent() {
		return comp;
	}

	@Override
	public String getTitle() {
		return comp.getFactory().getDisplayName();
	}

	@Override
	public void setValueRequested(Attribute<Object> attr, Object value)
			throws AttrTableSetException {
		if (!proj.getLogisimFile().contains(circ)) {
			String msg = _("cannotModifyCircuitError");
			throw new AttrTableSetException(msg);
		} else {
			SetAttributeAction act = new SetAttributeAction(circ,
					__("changeAttributeAction"));
			act.set(comp, attr, value);
			proj.doAction(act);
		}
	}
}


