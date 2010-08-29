/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutator;
import com.cburch.logisim.circuit.CircuitTransaction;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;

public class XmlCircuitReader extends CircuitTransaction {
	private XmlReader.ReadContext reader;
	private Map<Element,Circuit> elementMap;
	
	public XmlCircuitReader(XmlReader.ReadContext reader,
			Map<Element,Circuit> elementMap) {
		this.reader = reader;
		this.elementMap = elementMap;
	}

	@Override
	protected Map<Circuit, Integer> getAccessedCircuits() {
		HashMap<Circuit,Integer> access = new HashMap<Circuit,Integer>();
		for (Circuit circ : elementMap.values()) {
			access.put(circ, READ_WRITE);
		}
		return access;
	}

	@Override
	protected void run(CircuitMutator mutator) {
		for (Map.Entry<Element,Circuit> entry : elementMap.entrySet()) {
			buildCircuit(entry.getValue(), mutator, entry.getKey());
		}
	}

	private void buildCircuit(Circuit dest, CircuitMutator mutator, Element elt) {
		reader.initAttributeSet(elt, dest.getStaticAttributes(), null);

		for (Element sub_elt : XmlIterator.forChildElements(elt)) {
			String sub_elt_name = sub_elt.getTagName();
			if (sub_elt_name.equals("comp")) {
				addComponent(dest, mutator, sub_elt);
			} else if (sub_elt_name.equals("wire")) {
				addWire(dest, mutator, sub_elt);
			}
		}
	}

	void addComponent(Circuit dest, CircuitMutator mutator, Element elt) {
		// Determine component class
		String name = elt.getAttribute("name");
		if (name == null || name.equals("")) {
			showError(Strings.get("compNameMissingError"));
			return;
		}
		String libName = elt.getAttribute("lib");

		Library lib = reader.findLibrary(libName);
		if (lib == null) return;

		Tool tool = lib.getTool(name);
		if (tool == null || !(tool instanceof AddTool)) {
			if (libName == null || libName.equals("")) {
				showError(Strings.get("compUnknownError", name));
			} else {
				showError(Strings.get("compAbsentError", name, libName));
			}
		} else {
			ComponentFactory source = ((AddTool) tool).getFactory();
			addComponent(dest, mutator, source, elt);
		}
	}

	void addComponent(Circuit dest, CircuitMutator mutator,
			ComponentFactory source, Element elt) {
		// Determine attributes
		String loc_str = elt.getAttribute("loc");
		AttributeSet attrs = source.createAttributeSet();
		reader.initAttributeSet(elt, attrs, source);

		// Create component if location known
		if (loc_str == null || loc_str.equals("")) {
			showError(Strings.get("compLocMissingError", source.getName()));
		} else {
			try {
				Location loc = Location.parse(loc_str);
				mutator.add(dest, source.createComponent(loc, attrs));
			} catch (NumberFormatException e) {
				showError(Strings.get("compLocInvalidError",
					source.getName(), loc_str));
			}
		}

	}

	void addWire(Circuit dest, CircuitMutator mutator, Element elt) {
		Location pt0;
		try {
			String str = elt.getAttribute("from");
			if (str == null || str.equals("")) {
				showError(Strings.get("wireStartMissingError"));
			}
			pt0 = Location.parse(str);
		} catch (NumberFormatException e) {
			showError(Strings.get("wireStartInvalidError"));
			return;
		}

		Location pt1;
		try {
			String str = elt.getAttribute("to");
			if (str == null || str.equals("")) {
				showError(Strings.get("wireEndMissingError"));
			}
			pt1 = Location.parse(str);
		} catch (NumberFormatException e) {
			showError(Strings.get("wireEndInvalidError"));
			return;
		}

		mutator.add(dest, Wire.create(pt0, pt1));
	}
	
	private void showError(String errorMessage) {
		reader.showError(errorMessage);
	}
}
