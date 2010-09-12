/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.cburch.draw.model.DrawingMember;
import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.Main;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.appear.AppearanceSvgReader;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeDefaultProvider;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.std.base.Pin;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.InputEventUtil;
import com.cburch.logisim.util.StringUtil;

class XmlReader {
	static class CircuitData {
		Element circuitElement;
		Circuit circuit;
		Map<Element, Component> knownComponents;
		List<DrawingMember> appearance;
		
		public CircuitData(Element circuitElement, Circuit circuit) {
			this.circuitElement = circuitElement;
			this.circuit = circuit;
		}
	}
	
	class ReadContext {
		LogisimFile file;
		LogisimVersion sourceVersion;
		HashMap<String,Library> libs = new HashMap<String,Library>();

		ReadContext(LogisimFile file) {
			this.file = file;
		}

		private void toLogisimFile(Element elt) {
			// determine the version producing this file
			String versionString = elt.getAttribute("source");
			if (versionString.equals("")) {
				sourceVersion = Main.VERSION;
			} else {
				sourceVersion = LogisimVersion.parse(versionString);
			}

			// first, load the sublibraries
			for (Element o : XmlIterator.forChildElements(elt, "lib")) {
				Library lib = toLibrary(o);
				if (lib != null) file.addLibrary(lib);
			}
			
			// second, create the circuits - empty for now
			List<CircuitData> circuitsData = new ArrayList<CircuitData>();
			for (Element circElt : XmlIterator.forChildElements(elt, "circuit")) {
				String name = circElt.getAttribute("name");
				if (name == null || name.equals("")) {
					showError(Strings.get("circNameMissingError"));
				}
				CircuitData circData = new CircuitData(circElt, new Circuit(name));
				file.addCircuit(circData.circuit);
				circData.knownComponents = loadKnownComponents(circElt);
				for (Element appearElt : XmlIterator.forChildElements(circElt, "appear")) {
					loadAppearance(appearElt, circData);
				}
				circuitsData.add(circData);
			}

			// third, process the other child elements
			for (Element sub_elt : XmlIterator.forChildElements(elt)) {
				String name = sub_elt.getTagName();
				if (name.equals("circuit") || name.equals("lib")) {
					; // Nothing to do: Done earlier.
				} else if (name.equals("options")) {
					initAttributeSet(elt, file.getOptions().getAttributeSet(), null);
				} else if (name.equals("mappings")) {
					initMouseMappings(sub_elt);
				} else if (name.equals("toolbar")) {
					initToolbarData(sub_elt);
				} else if (name.equals("main")) {
					String main = sub_elt.getAttribute("name");
					Circuit circ = file.getCircuit(main);
					if (circ != null) {
						file.setMainCircuit(circ);
					}
				} else if (name.equals("message")) {
					file.addMessage(sub_elt.getAttribute("value"));
				}
			}
			
			// fourth, execute a transaction that initializes all the circuits
			XmlCircuitReader builder;
			builder = new XmlCircuitReader(this, circuitsData);
			builder.execute();
		}

		private Library toLibrary(Element elt) {
			if (!elt.hasAttribute("name")) {
				loader.showError(Strings.get("libNameMissingError"));
				return null;
			}
			if (!elt.hasAttribute("desc")) {
				loader.showError(Strings.get("libDescMissingError"));
				return null;
			}
			String name = elt.getAttribute("name");
			String desc = elt.getAttribute("desc");
			Library ret = loader.loadLibrary(desc);
			if (ret == null) return null;
			libs.put(name, ret);
			for (Element sub_elt : XmlIterator.forChildElements(elt, "tool")) {
				if (!sub_elt.hasAttribute("name")) {
					loader.showError(Strings.get("toolNameMissingError"));
				} else {
					String tool_str = sub_elt.getAttribute("name");
					Tool tool = ret.getTool(tool_str);
					if (tool != null) {
						initAttributeSet(sub_elt, tool.getAttributeSet(), tool);
					}
				}
			}
			return ret;
		}
		
		private Map<Element, Component> loadKnownComponents(Element elt) {
			Map<Element, Component> known = new HashMap<Element, Component>();
			for (Element sub : XmlIterator.forChildElements(elt, "comp")) {
				try {
					Component comp = XmlCircuitReader.getComponent(sub, this);
					known.put(sub, comp);
				} catch (RuntimeException e) { }
			}
			return known;
		}
		
		private void loadAppearance(Element appearElt, CircuitData circData) {
			Map<Location, Instance> pins = new HashMap<Location, Instance>();
			for (Component comp : circData.knownComponents.values()) {
				if (comp.getFactory() == Pin.FACTORY) {
					Instance instance = Instance.getInstanceFor(comp);
					pins.put(comp.getLocation(), instance);
				}
			}
			
			List<DrawingMember> shapes = new ArrayList<DrawingMember>();
			for (Element sub : XmlIterator.forChildElements(appearElt)) {
				try {
					DrawingMember m = AppearanceSvgReader.createShape(sub, pins);
					if (m == null) {
						showError(Strings.get("fileAppearanceNotFound", sub.getTagName()));
					} else {
						shapes.add(m);
					}
				} catch (RuntimeException e) {
					showError(Strings.get("fileAppearanceError", sub.getTagName()));
				}
			}
			if (!shapes.isEmpty()) {
				if (circData.appearance == null) {
					circData.appearance = shapes;
				} else {
					circData.appearance.addAll(shapes);
				}
			}
		}

		private void initMouseMappings(Element elt) {
			MouseMappings map = file.getOptions().getMouseMappings();
			for (Element sub_elt : XmlIterator.forChildElements(elt, "tool")) {
				Tool tool = toTool(sub_elt);
				if (tool == null) continue;

				String mods_str = sub_elt.getAttribute("map");
				if (mods_str == null || mods_str.equals("")) {
					loader.showError(Strings.get("mappingMissingError"));
					continue;
				}
				int mods;
				try {
					mods = InputEventUtil.fromString(mods_str);
				} catch (NumberFormatException e) {
					loader.showError(StringUtil.format(
						Strings.get("mappingBadError"), mods_str));
					continue;
				}

				tool = tool.cloneTool();
				initAttributeSet(sub_elt, tool.getAttributeSet(), tool);

				map.setToolFor(mods, tool);
			}
		}

		private void initToolbarData(Element elt) {
			ToolbarData toolbar = file.getOptions().getToolbarData();
			for (Element sub_elt : XmlIterator.forChildElements(elt)) {
				if (sub_elt.getTagName().equals("sep")) {
					toolbar.addSeparator();
				} else if (sub_elt.getTagName().equals("tool")) {
					Tool tool = toTool(sub_elt);
					if (tool == null) continue;
					tool = tool.cloneTool();
					initAttributeSet(sub_elt, tool.getAttributeSet(), tool);
					toolbar.addTool(tool);
				}
			}
		}

		Tool toTool(Element elt) {
			Library lib = findLibrary(elt.getAttribute("lib"));
			if (lib == null) return null;
			String tool_name = elt.getAttribute("name");
			if (tool_name == null || tool_name.equals("")) return null;
			return lib.getTool(tool_name);
		}
		
		void initAttributeSet(Element parentElt, AttributeSet attrs,
				AttributeDefaultProvider defaults) {
			HashMap<String,String> attrsDefined = new HashMap<String,String>();
			for (Element attrElt : XmlIterator.forChildElements(parentElt, "a")) {
				if (!attrElt.hasAttribute("name")) {
					loader.showError(Strings.get("attrNameMissingError"));
				} else {
					String attrName = attrElt.getAttribute("name");
					String attrVal;
					if (attrElt.hasAttribute("val")) {
						attrVal = attrElt.getAttribute("val");
					} else {
						attrVal = attrElt.getTextContent();
					}
					attrsDefined.put(attrName, attrVal);
				}
			}

			if (attrs == null) return;
			
			LogisimVersion ver = sourceVersion;
			boolean setDefaults = defaults != null
				&& !defaults.isAllDefaultValues(attrs, ver);
			// We need to process this in order, and we have to refetch the
			// attribute list each time because it may change as we iterate
			// (as it will for a splitter).
			for (int i = 0; true; i++) {
				List<Attribute<?>> attrList = attrs.getAttributes();
				if (i >= attrList.size()) break;
				@SuppressWarnings("unchecked")
				Attribute<Object> attr = (Attribute<Object>) attrList.get(i);
				String attrName = attr.getName();
				String attrVal = attrsDefined.get(attrName);
				if (attrVal == null) {
					if (setDefaults) {
						Object val = defaults.getDefaultAttributeValue(attr, ver);
						if (val != null) {
						    attrs.setValue(attr, val);
						}
					}
				} else {
					try {
						Object val = attr.parse(attrVal);
						attrs.setValue(attr, val);
					} catch (NumberFormatException e) {
						loader.showError(StringUtil.format(
						    Strings.get("attrValueInvalidError"),
						    attrVal, attrName));
						continue;
					}
				}
			}
		}

		Library findLibrary(String lib_name) {
			if (lib_name == null || lib_name.equals("")) {
				return file;
			}

			Library ret = libs.get(lib_name);
			if (ret == null) {
				loader.showError(StringUtil.format(
					Strings.get("libMissingError"), lib_name));
				return null;
			} else {
				return ret;
			}
		}

		void showError(String errorMessage) {
			loader.showError(errorMessage);
		}
	}

	private LibraryLoader loader;

	XmlReader(Loader loader) {
		this.loader = loader;
	}

	LogisimFile readLibrary(InputStream is) throws IOException, SAXException {
		Document doc = loadXmlFrom(is);
		Element elt = doc.getDocumentElement();
		considerRepairs(elt);
		LogisimFile file = new LogisimFile((Loader) loader);
		ReadContext context = new ReadContext(file);
		context.toLogisimFile(elt);
		if (file.getCircuitCount() == 0) {
			file.addCircuit(new Circuit("main"));
		}
		return file;
	}
	
	private Document loadXmlFrom(InputStream is) throws SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) { }
		return builder.parse(is);
	}
	
	private void considerRepairs(Element root) {
		LogisimVersion version = LogisimVersion.parse(root.getAttribute("source"));
		if (version.compareTo(LogisimVersion.get(2, 3, 0)) < 0) {
			// This file was saved before an Edit tool existed. Most likely
			// we should replace the Select and Wiring tools in the toolbar
			// with the Edit tool instead.
			for (Element toolbar : XmlIterator.forChildElements(root, "toolbar")) {
				Element wiring = null;
				Element select = null;
				Element edit = null;
				for (Element elt : XmlIterator.forChildElements(toolbar, "tool")) {
					String eltName = elt.getAttribute("name");
					if (eltName != null && !eltName.equals("")) {
						if (eltName.equals("Select Tool")) select = elt;
						if (eltName.equals("Wiring Tool")) wiring = elt;
						if (eltName.equals("Edit Tool")) edit = elt;
					}
				}
				if (select != null && wiring != null && edit == null) {
					select.setAttribute("name", "Edit Tool");
					toolbar.removeChild(wiring);
				}
			}
		}
	}

}
