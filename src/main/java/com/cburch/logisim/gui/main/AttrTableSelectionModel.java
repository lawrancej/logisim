/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.gui.generic.AttrTableSetException;
import com.cburch.logisim.gui.generic.AttributeSetTableModel;
import com.cburch.logisim.gui.main.AttrTableCircuitModel;
import com.cburch.logisim.gui.main.Selection;
import com.cburch.logisim.gui.main.Selection.Event;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.SetAttributeAction;
import static com.cburch.logisim.util.LocaleString.*;

class AttrTableSelectionModel extends AttributeSetTableModel
		implements Selection.Listener {
	private Project project;
	private Frame frame;
	
	public AttrTableSelectionModel(Project project, Frame frame) {
		super(frame.getCanvas().getSelection().getAttributeSet());
		this.project = project;
		this.frame = frame;
		frame.getCanvas().getSelection().addListener(this);
	}
	
	@Override
	public String getTitle() {
		ComponentFactory wireFactory = null;
		ComponentFactory factory = null;
		int factoryCount = 0;
		int totalCount = 0;
		boolean variousFound = false;
		
		Selection selection = frame.getCanvas().getSelection();
		for (Component comp : selection.getComponents()) {
			ComponentFactory fact = comp.getFactory();
			if (fact == factory) {
				factoryCount++;
			} else if (comp instanceof Wire) {
				wireFactory = fact;
				if (factory == null) {
					factoryCount++;
				}
			} else if (factory == null) {
				factory = fact;
				factoryCount = 1;
			} else {
				variousFound = true;
			}
			if (!(comp instanceof Wire)) {
				totalCount++;
			}
		}
		
		if (factory == null) {
			factory = wireFactory;
		}

		if (variousFound) {
			return _("selectionVarious", "" + totalCount);
		} else if (factoryCount == 0) {
			String circName = frame.getCanvas().getCircuit().getName();
			return _("circuitAttrTitle", circName);
		} else if (factoryCount == 1) {
			return _("selectionOne", factory.getDisplayName());
		} else {
			return _("selectionMultiple", factory.getDisplayName(),
					"" + factoryCount);
		}
	}

	@Override
	public void setValueRequested(Attribute<Object> attr, Object value)
			throws AttrTableSetException {
		Selection selection = frame.getCanvas().getSelection();
		Circuit circuit = frame.getCanvas().getCircuit();
		if (selection.isEmpty() && circuit != null) {
			AttrTableCircuitModel circuitModel = new AttrTableCircuitModel(project, circuit);
			circuitModel.setValueRequested(attr, value);
		} else {
			SetAttributeAction act = new SetAttributeAction(circuit,
					__("selectionAttributeAction"));
			for (Component comp : selection.getComponents()) {
				if (!(comp instanceof Wire)) {
					act.set(comp, attr, value);
				}
			}
			project.doAction(act);
		}
	}

	//
	// Selection.Listener methods
	public void selectionChanged(Event event) {
		fireTitleChanged();
		if (frame.getEditorView().equals(Frame.EDIT_LAYOUT)) {
			frame.setAttrTableModel(this);
		}
	}
}
