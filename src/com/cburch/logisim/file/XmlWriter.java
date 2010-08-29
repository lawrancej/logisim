/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.Main;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeDefaultProvider;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.InputEventUtil;
import com.cburch.logisim.util.StringUtil;

class XmlWriter {
	private class WriteContext {
		LogisimFile file;
		HashMap<Library,String> libs = new HashMap<Library,String>();

		WriteContext(LogisimFile file) {
			this.file = file;
		}

		Element fromLogisimFile() {
			Element ret = new Element("project");
			ret.addContent("This file is intended to be loaded by Logisim "
						+ "(http://www.cburch.com/logisim/).");
			ret.setAttribute("version", "1.0");
			ret.setAttribute("source", Main.VERSION_NAME);

			for (Library lib : file.getLibraries()) {
				Element elt = fromLibrary(lib);
				if (elt != null) ret.addContent(elt);
			}

			if (file.getMainCircuit() != null) {
				Element mainElt = new Element("main");
				mainElt.setAttribute("name", file.getMainCircuit().getName());
				ret.addContent(mainElt);
			}

			ret.addContent(fromOptions());
			ret.addContent(fromMouseMappings());
			ret.addContent(fromToolbarData());

			for (AddTool tool : file.getTools()) {
				Circuit circ = (Circuit) tool.getFactory();
				ret.addContent(fromCircuit(circ));
			}
			return ret;
		}

		Element fromLibrary(Library lib) {
			Element ret = new Element("lib");
			if (libs.containsKey(lib)) return null;
			String name = "" + libs.size();
			String desc = loader.getDescriptor(lib);
			if (desc == null) {
				loader.showError("library location unknown: "
					+ lib.getName());
				return null;
			}
			libs.put(lib, name);
			ret.setAttribute("name", name);
			ret.setAttribute("desc", desc);
			for (Tool t : lib.getTools()) {
				AttributeSet attrs = t.getAttributeSet();
				if (attrs != null) {
					Element to_add = new Element("tool");
					to_add.setAttribute("name", t.getName());
					addAttributeSetContent(to_add, attrs, t);
					if (to_add.getChildren().size() > 0) {
						ret.addContent(to_add);
					}
				}
			}
			return ret;
		}

		Element fromOptions() {
			Element elt = new Element("options");
			addAttributeSetContent(elt, file.getOptions().getAttributeSet(), null);
			return elt;
		}

		Element fromMouseMappings() {
			Element elt = new Element("mappings");
			MouseMappings map = file.getOptions().getMouseMappings();
			for (Map.Entry<Integer,Tool> entry : map.getMappings().entrySet()) {
				Integer mods = entry.getKey();
				Tool tool = entry.getValue();
				Element toolElt = fromTool(tool);
				String mapValue = InputEventUtil.toString(mods.intValue());
				toolElt.setAttribute("map", mapValue);
				elt.addContent(toolElt);
			}
			return elt;
		}

		Element fromToolbarData() {
			Element elt = new Element("toolbar");
			ToolbarData toolbar = file.getOptions().getToolbarData();
			for (Tool tool : toolbar.getContents()) {
				if (tool == null) {
					elt.addContent(new Element("sep"));
				} else {
					elt.addContent(fromTool(tool));
				}
			}
			return elt;
		}

		Element fromTool(Tool tool) {
			Library lib = findLibrary(tool);
			String lib_name;
			if (lib == null) {
				loader.showError(StringUtil.format("tool `%s' not found",
					tool.getDisplayName()));
				return null;
			} else if (lib == file) {
				lib_name = null;
			} else {
				lib_name = libs.get(lib);
				if (lib_name == null) {
					loader.showError("unknown library within file");
					return null;
				}
			}

			Element elt = new Element("tool");
			if (lib_name != null) elt.setAttribute("lib", lib_name);
			elt.setAttribute("name", tool.getName());
			addAttributeSetContent(elt, tool.getAttributeSet(), tool);
			return elt;
		}

		Element fromCircuit(Circuit circuit) {
			Element ret = new Element("circuit");
			ret.setAttribute("name", circuit.getName());
			addAttributeSetContent(ret, circuit.getStaticAttributes(), null);
			for (Wire w : circuit.getWires()) {
				ret.addContent(fromWire(w));
			}
			for (Component comp : circuit.getNonWires()) {
				Element elt = fromComponent(comp);
				if (elt != null) ret.addContent(elt);
			}
			return ret;
		}

		Element fromComponent(Component comp) {
			ComponentFactory source = comp.getFactory();
			Library lib = findLibrary(source);
			String lib_name;
			if (lib == null) {
				loader.showError(source.getName() + " component not found");
				return null;
			} else if (lib == file) {
				lib_name = null;
			} else {
				lib_name = libs.get(lib);
				if (lib_name == null) {
					loader.showError("unknown library within file");
					return null;
				}
			}

			Element ret = new Element("comp");
			if (lib_name != null) ret.setAttribute("lib", lib_name);
			ret.setAttribute("name", source.getName());
			ret.setAttribute("loc", comp.getLocation().toString());
			addAttributeSetContent(ret, comp.getAttributeSet(), comp.getFactory());
			return ret;
		}

		Element fromWire(Wire w) {
			Element ret = new Element("wire");
			ret.setAttribute("from", w.getEnd0().toString());
			ret.setAttribute("to", w.getEnd1().toString());
			return ret;
		}

		void addAttributeSetContent(Element elt, AttributeSet attrs,
				AttributeDefaultProvider source) {
			if (attrs == null) return;
			LogisimVersion ver = Main.VERSION;
			if (source != null && source.isAllDefaultValues(attrs, ver)) return;
			for (Attribute<?> attrBase : attrs.getAttributes()) {
				@SuppressWarnings("unchecked")
				Attribute<Object> attr = (Attribute<Object>) attrBase;
				Object val = attrs.getValue(attr);
				if (attrs.isToSave(attr) && val != null) {
					Object dflt = source == null ? null : source.getDefaultAttributeValue(attr, ver);
					if (dflt == null || !dflt.equals(val)) {
						Element a = new Element("a");
						a.setAttribute("name", attr.getName());
						String value = attr.toStandardString(val);
						if (value.indexOf("\n") >= 0) {
						    a.addContent(value);
						} else {
						    a.setAttribute("val", attr.toStandardString(val));
						}
						elt.addContent(a);
					}
				}
			}
		}

		Library findLibrary(Tool tool) {
			if (libraryContains(file, tool)) {
				return file;
			}
			for (Library lib : file.getLibraries()) {
				if (libraryContains(lib, tool)) return lib;
			}
			return null;
		}

		Library findLibrary(ComponentFactory source) {
			if (file.contains(source)) {
				return file;
			}
			for (Library lib : file.getLibraries()) {
				if (lib.contains(source)) return lib;
			}
			return null;
		}

		boolean libraryContains(Library lib, Tool query) {
			for (Tool tool : lib.getTools()) {
				if (tool.sharesSource(query)) return true;
			}
			return false;
		}
	}

	private XMLOutputter outputter;
	private LibraryLoader loader;

	XmlWriter(LibraryLoader loader) {
		this.loader = loader;
		Format format = Format.getPrettyFormat();
//      format.setLineSeparator("\r\n");
//      format.setIndent(" ");
		outputter = new XMLOutputter(format);
	}

	Object initialize(LogisimFile file) {
		WriteContext context = new WriteContext(file);
		return new Document(context.fromLogisimFile());
	}

	void output(Object data, java.io.Writer writer)
			throws java.io.IOException {
		outputter.output((Document) data, writer);
	}

}
