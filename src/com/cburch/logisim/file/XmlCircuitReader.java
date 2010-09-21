/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.cburch.draw.model.AbstractCanvasObject;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutator;
import com.cburch.logisim.circuit.CircuitTransaction;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;

public class XmlCircuitReader extends CircuitTransaction {
	private XmlReader.ReadContext reader;
	private List<XmlReader.CircuitData> circuitsData;
	
	public XmlCircuitReader(XmlReader.ReadContext reader,
			List<XmlReader.CircuitData> circDatas) {
		this.reader = reader;
		this.circuitsData = circDatas;
	}

	@Override
	protected Map<Circuit, Integer> getAccessedCircuits() {
		HashMap<Circuit, Integer> access = new HashMap<Circuit,Integer>();
		for (XmlReader.CircuitData data : circuitsData) {
			access.put(data.circuit, READ_WRITE);
		}
		return access;
	}

	@Override
	protected void run(CircuitMutator mutator) {
		for (XmlReader.CircuitData circuitData : circuitsData) {
			buildCircuit(circuitData, mutator);
		}
	}

	private void buildCircuit(XmlReader.CircuitData circData, CircuitMutator mutator) {
		Element elt = circData.circuitElement;
		Circuit dest = circData.circuit;
		Map<Element, Component> knownComponents = circData.knownComponents;
		if (knownComponents == null) knownComponents = Collections.emptyMap();
		reader.initAttributeSet(circData.circuitElement, dest.getStaticAttributes(), null);

		for (Element sub_elt : XmlIterator.forChildElements(elt)) {
			String sub_elt_name = sub_elt.getTagName();
			if (sub_elt_name.equals("comp")) {
				try {
					Component comp = knownComponents.get(sub_elt);
					if (comp == null) {
						comp = getComponent(sub_elt, reader);
					}
					mutator.add(dest, comp);
				} catch (RuntimeException e) {
					showError(e.getMessage());
				}
			} else if (sub_elt_name.equals("wire")) {
				addWire(dest, mutator, sub_elt);
			}
		}
		
		List<AbstractCanvasObject> appearance = circData.appearance;
		if (appearance != null && !appearance.isEmpty()) {
			dest.getAppearance().setObjectsForce(appearance);
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
	
	static Component getComponent(Element elt, XmlReader.ReadContext reader)
			throws RuntimeException {
		// Determine the factory that creates this element
		String name = elt.getAttribute("name");
		if (name == null || name.equals("")) {
			throw new RuntimeException(Strings.get("compNameMissingError"));
		}

		String libName = elt.getAttribute("lib");
		Library lib = reader.findLibrary(libName);
		if (lib == null) {
			throw new RuntimeException(Strings.get("compUnknownError", "no-lib"));
		}

		Tool tool = lib.getTool(name);
		if (tool == null || !(tool instanceof AddTool)) {
			if (libName == null || libName.equals("")) {
				throw new RuntimeException(Strings.get("compUnknownError", name));
			} else {
				throw new RuntimeException(Strings.get("compAbsentError", name, libName));
			}
		}
		ComponentFactory source = ((AddTool) tool).getFactory();

		// Determine attributes
		String loc_str = elt.getAttribute("loc");
		AttributeSet attrs = source.createAttributeSet();
		reader.initAttributeSet(elt, attrs, source);

		// Create component if location known
		if (loc_str == null || loc_str.equals("")) {
			throw new RuntimeException(Strings.get("compLocMissingError", source.getName()));
		} else {
			try {
				Location loc = Location.parse(loc_str);
				return source.createComponent(loc, attrs);
			} catch (NumberFormatException e) {
				throw new RuntimeException(Strings.get("compLocInvalidError",
					source.getName(), loc_str));
			}
		}
	}
}
