/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.gui.generic.AttrTableSetException;
import com.cburch.logisim.gui.generic.AttributeSetTableModel;
import com.cburch.logisim.proj.Project;
import static com.cburch.logisim.util.LocaleString.*;

public class AttrTableCircuitModel extends AttributeSetTableModel {
	private Project proj;
	private Circuit circ;
	
	public AttrTableCircuitModel(Project proj, Circuit circ) {
		super(circ.getStaticAttributes());
		this.proj = proj;
		this.circ = circ;
	}

	@Override
	public String getTitle() {
		return _("circuitAttrTitle", circ.getName());
	}
	
	@Override
	public void setValueRequested(Attribute<Object> attr, Object value)
			throws AttrTableSetException {
		if (!proj.getLogisimFile().contains(circ)) {
			String msg = _("cannotModifyCircuitError");
			throw new AttrTableSetException(msg);
		} else {
			CircuitMutation xn = new CircuitMutation(circ);
			xn.setForCircuit(attr, value);
			proj.doAction(xn.toAction(__("changeCircuitAttrAction")));
		}
	}
}

