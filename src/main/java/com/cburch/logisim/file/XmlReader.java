/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

import com.cburch.draw.model.AbstractCanvasObject;
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
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.InputEventUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cburch.logisim.util.LocaleString.getFromLocale;

class XmlReader {
    static class CircuitData {
        final Element circuitElement;
        final Circuit circuit;
        Map<Element, Component> knownComponents;
        List<AbstractCanvasObject> appearance;

        public CircuitData(Element circuitElement, Circuit circuit) {
            this.circuitElement = circuitElement;
            this.circuit = circuit;
        }
    }

    class ReadContext {
        final LogisimFile file;
        LogisimVersion sourceVersion;
        final Map<String, Library> libs = new HashMap<>();
        private final List<String> messages;

        ReadContext(LogisimFile file) {
            this.file = file;
            this.messages = new ArrayList<>();
        }

        void addError(String message, String context) {
            messages.add(message + " [" + context + ']');
        }

        void addErrors(XmlReaderException exception, String context) {
            for (String msg : exception.getMessages()) {
                messages.add(msg + " [" + context + ']');
            }
        }

        private void toLogisimFile(Element elt) {
            // determine the version producing this file
            String versionString = elt.getAttribute("source");
            if (versionString.isEmpty()) {
                sourceVersion = Main.VERSION;
            } else {
                sourceVersion = LogisimVersion.parse(versionString);
            }

            // first, load the sublibraries
            for (Element o : XmlIterator.forChildElements(elt, "lib")) {
                Library lib = toLibrary(o);
                if (lib != null) {
                    file.addLibrary(lib);
                }

            }

            // second, create the circuits - empty for now
            List<CircuitData> circuitsData = new ArrayList<>();
            for (Element circElt : XmlIterator.forChildElements(elt, "circuit")) {
                String name = circElt.getAttribute("name");
                if (name == null || name.isEmpty()) {
                    addError(getFromLocale("circNameMissingError"), "C??");
                }
                CircuitData circData = new CircuitData(circElt, new Circuit(name));
                file.addCircuit(circData.circuit);
                circData.knownComponents = loadKnownComponents(circElt);
                for (Element appearElt : XmlIterator.forChildElements(circElt, "appear")) {
                    loadAppearance(appearElt, circData, name + ".appear");
                }
                circuitsData.add(circData);
            }

            // third, process the other child elements
            for (Element sub_elt : XmlIterator.forChildElements(elt)) {
                String name = sub_elt.getTagName();
                switch (name) {
                    case "circuit":
                    case "lib":
                        // Nothing to do: Done earlier.
                        break;
                    case "options":
                        try {
                            initAttributeSet(sub_elt, file.getOptions().getAttributeSet(), null);
                        } catch (XmlReaderException e) {
                            addErrors(e, "options");
                        }
                        break;
                    case "mappings":
                        initMouseMappings(sub_elt);
                        break;
                    case "toolbar":
                        initToolbarData(sub_elt);
                        break;
                    case "main":
                        String main = sub_elt.getAttribute("name");
                        Circuit circ = file.getCircuit(main);
                        if (circ != null) {
                            file.setMainCircuit(circ);
                        }
                        break;
                    case "message":
                        file.addMessage(sub_elt.getAttribute("value"));
                        break;
                }
            }

            // fourth, execute a transaction that initializes all the circuits
            XmlCircuitReader builder;
            builder = new XmlCircuitReader(this, circuitsData);
            builder.execute();
        }

        private Library toLibrary(Element elt) {
            if (!elt.hasAttribute("name")) {
                loader.showError(getFromLocale("libNameMissingError"));
                return null;
            }
            if (!elt.hasAttribute("desc")) {
                loader.showError(getFromLocale("libDescMissingError"));
                return null;
            }
            String name = elt.getAttribute("name");
            String desc = elt.getAttribute("desc");
            Library ret = loader.loadLibrary(desc);
            if (ret == null) {
                return null;
            }

            libs.put(name, ret);
            for (Element sub_elt : XmlIterator.forChildElements(elt, "tool")) {
                if (!sub_elt.hasAttribute("name")) {
                    loader.showError(getFromLocale("toolNameMissingError"));
                } else {
                    String tool_str = sub_elt.getAttribute("name");
                    Tool tool = ret.getTool(tool_str);
                    if (tool != null) {
                        try {
                            initAttributeSet(sub_elt, tool.getAttributeSet(), tool);
                        } catch (XmlReaderException e) {
                            addErrors(e, "lib." + name + '.' + tool_str);
                        }
                    }
                }
            }
            return ret;
        }

        private Map<Element, Component> loadKnownComponents(Element elt) {
            Map<Element, Component> known = new HashMap<>();
            for (Element sub : XmlIterator.forChildElements(elt, "comp")) {
                try {
                    Component comp = XmlCircuitReader.getComponent(sub, this);
                    known.put(sub, comp);
                } catch (XmlReaderException e) { }
            }
            return known;
        }

        private void loadAppearance(Element appearElt, CircuitData circData,
                String context) {
            Map<Location, Instance> pins = new HashMap<>();
            circData.knownComponents.values().stream().filter(comp -> comp.getFactory() == Pin.FACTORY).forEach(comp -> {
                Instance instance = Instance.getInstanceFor(comp);
                pins.put(comp.getLocation(), instance);
            });

            List<AbstractCanvasObject> shapes = new ArrayList<>();
            for (Element sub : XmlIterator.forChildElements(appearElt)) {
                try {
                    AbstractCanvasObject m = AppearanceSvgReader.createShape(sub, pins);
                    if (m == null) {
                        addError(getFromLocale("fileAppearanceNotFound", sub.getTagName()),
                                context + '.' + sub.getTagName());
                    } else {
                        shapes.add(m);
                    }
                } catch (RuntimeException e) {
                    addError(getFromLocale("fileAppearanceError", sub.getTagName()),
                            context + '.' + sub.getTagName());
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
                Tool tool;
                try {
                    tool = toTool(sub_elt);
                } catch (XmlReaderException e) {
                    addErrors(e, "mapping");
                    continue;
                }

                String mods_str = sub_elt.getAttribute("map");
                if (mods_str == null || mods_str.isEmpty()) {
                    loader.showError(getFromLocale("mappingMissingError"));
                    continue;
                }
                int mods;
                try {
                    mods = InputEventUtil.fromString(mods_str);
                } catch (NumberFormatException e) {
                    loader.showError(getFromLocale("mappingBadError", mods_str));
                    continue;
                }

                tool = tool.cloneTool();
                try {
                    initAttributeSet(sub_elt, tool.getAttributeSet(), tool);
                } catch (XmlReaderException e) {
                    addErrors(e, "mapping." + tool.getName());
                }

                map.setToolFor(mods, tool);
            }
        }

        private void initToolbarData(Element elt) {
            ToolbarData toolbar = file.getOptions().getToolbarData();
            for (Element sub_elt : XmlIterator.forChildElements(elt)) {
                if (sub_elt.getTagName().equals("sep")) {
                    toolbar.addSeparator();
                } else if (sub_elt.getTagName().equals("tool")) {
                    Tool tool;
                    try {
                        tool = toTool(sub_elt);
                    } catch (XmlReaderException e) {
                        addErrors(e, "toolbar");
                        continue;
                    }
                    if (tool != null) {
                        tool = tool.cloneTool();
                        try {
                            initAttributeSet(sub_elt, tool.getAttributeSet(), tool);
                        } catch (XmlReaderException e) {
                            addErrors(e, "toolbar." + tool.getName());
                        }
                        toolbar.addTool(tool);
                    }
                }
            }
        }

        Tool toTool(Element elt) throws XmlReaderException {
            Library lib = findLibrary(elt.getAttribute("lib"));
            String name = elt.getAttribute("name");
            if (name == null || name.isEmpty()) {
                throw new XmlReaderException(getFromLocale("toolNameMissing"));
            }
            Tool tool = lib.getTool(name);
            if (tool == null) {
                throw new XmlReaderException(getFromLocale("toolNotFound"));
            }
            return tool;
        }

        void initAttributeSet(Element parentElt, AttributeSet attrs,
                AttributeDefaultProvider defaults) throws XmlReaderException {
            ArrayList<String> messages = null;

            Map<String, String> attrsDefined = new HashMap<>();
            for (Element attrElt : XmlIterator.forChildElements(parentElt, "a")) {
                if (!attrElt.hasAttribute("name")) {
                    if (messages == null) {
                        messages = new ArrayList<>();
                    }

                    messages.add(getFromLocale("attrNameMissingError"));
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

            if (attrs == null) {
                return;
            }


            LogisimVersion ver = sourceVersion;
            boolean setDefaults = defaults != null
                && !defaults.isAllDefaultValues(attrs, ver);
            // We need to process this in order, and we have to refetch the
            // attribute list each time because it may change as we iterate
            // (as it will for a splitter).
            for (int i = 0; true; i++) {
                List<Attribute<?>> attrList = attrs.getAttributes();
                if (i >= attrList.size()) {
                    break;
                }

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
                        if (messages == null) {
                            messages = new ArrayList<>();
                        }

                        messages.add(getFromLocale("attrValueInvalidError",
                            attrVal, attrName));
                    }
                }
            }
            if (messages != null) {
                throw new XmlReaderException(messages);
            }
        }

        Library findLibrary(String lib_name) throws XmlReaderException {
            if (lib_name == null || lib_name.isEmpty()) {
                return file;
            }

            Library ret = libs.get(lib_name);
            if (ret == null) {
                throw new XmlReaderException(getFromLocale("libMissingError", lib_name));
            } else {
                return ret;
            }
        }
    }

    private final LibraryLoader loader;

    XmlReader(LibraryLoader loader) {
        this.loader = loader;
    }

    LogisimFile readLibrary(InputStream is) throws IOException, SAXException {
        Document doc = loadXmlFrom(is);
        Element elt = doc.getDocumentElement();
        considerRepairs(doc, elt);
        LogisimFile file = new LogisimFile((Loader) loader);
        ReadContext context = new ReadContext(file);
        context.toLogisimFile(elt);
        if (file.getCircuitCount() == 0) {
            file.addCircuit(new Circuit("main"));
        }
        if (context.messages.size() > 0) {
            StringBuilder all = new StringBuilder();
            for (String msg : context.messages) {
                all.append(msg);
                all.append('\n');
            }
            loader.showError(all.substring(0, all.length() - 1));
        }
        return file;
    }

    private static Document loadXmlFrom(InputStream is) throws SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) { }
        return builder.parse(is);
    }

    private void considerRepairs(Document doc, Element root) {
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
                    if (eltName != null && !eltName.isEmpty()) {
                        if (eltName.equals("Select Tool")) {
                            select = elt;
                        }

                        if (eltName.equals("Wiring Tool")) {
                            wiring = elt;
                        }

                        if (eltName.equals("Edit Tool")) {
                            edit = elt;
                        }

                    }
                }
                if (select != null && wiring != null && edit == null) {
                    select.setAttribute("name", "Edit Tool");
                    toolbar.removeChild(wiring);
                }
            }
        }
        if (version.compareTo(LogisimVersion.get(2, 6, 3)) < 0) {
            for (Element circElt : XmlIterator.forChildElements(root, "circuit")) {
                for (Element attrElt : XmlIterator.forChildElements(circElt, "a")) {
                    String name = attrElt.getAttribute("name");
                    if (name != null && name.startsWith("label")) {
                        attrElt.setAttribute("name", 'c' + name);
                    }
                }
            }

            repairForWiringLibrary(doc, root);
            repairForLegacyLibrary(doc, root);
        }
    }

    private void repairForWiringLibrary(Document doc, Element root) {
        Element oldBaseElt = null;
        String oldBaseLabel = null;
        Element gatesElt = null;
        String gatesLabel = null;
        int maxLabel = -1;
        Element firstLibElt = null;
        Element lastLibElt = null;
        for (Element libElt : XmlIterator.forChildElements(root, "lib")) {
            String desc = libElt.getAttribute("desc");
            String label = libElt.getAttribute("name");
            if (desc == null) {
                // skip these tests
            } else if (desc.equals("#Base")) {
                oldBaseElt = libElt;
                oldBaseLabel = label;
            } else if (desc.equals("#Wiring")) {
                // Wiring library already in file. This shouldn't happen, but if
                // somehow it does, we don't want to add it again.
                return;
            } else if (desc.equals("#Gates")) {
                gatesElt = libElt;
                gatesLabel = label;
            }

            if (firstLibElt == null) {
                firstLibElt = libElt;
            }

            lastLibElt = libElt;
            try {
                if (label != null) {
                    int thisLabel = Integer.parseInt(label);
                    if (thisLabel > maxLabel) {
                        maxLabel = thisLabel;
                    }

                }
            } catch (NumberFormatException e) { }
        }

        Element wiringElt;
        String wiringLabel;
        Element newBaseElt;
        String newBaseLabel;
        if (oldBaseElt != null) {
            wiringLabel = oldBaseLabel;
            wiringElt = oldBaseElt;
            wiringElt.setAttribute("desc", "#Wiring");

            newBaseLabel = String.valueOf(maxLabel + 1);
            newBaseElt = doc.createElement("lib");
            newBaseElt.setAttribute("desc", "#Base");
            newBaseElt.setAttribute("name", newBaseLabel);
            root.insertBefore(newBaseElt, lastLibElt.getNextSibling());
        } else {
            wiringLabel = String.valueOf(maxLabel + 1);
            wiringElt = doc.createElement("lib");
            wiringElt.setAttribute("desc", "#Wiring");
            wiringElt.setAttribute("name", wiringLabel);
            root.insertBefore(wiringElt, lastLibElt.getNextSibling());

            newBaseLabel = null;
            newBaseElt = null;
        }

        HashMap<String,String> labelMap = new HashMap<>();
        addToLabelMap(labelMap, oldBaseLabel, newBaseLabel, "Poke Tool;"
                + "Edit Tool;Select Tool;Wiring Tool;Text Tool;Menu Tool;Text");
        addToLabelMap(labelMap, oldBaseLabel, wiringLabel, "Splitter;Pin;"
                + "Probe;Tunnel;Clock;Pull Resistor;Bit Extender");
        addToLabelMap(labelMap, gatesLabel, wiringLabel, "Constant");
        relocateTools(oldBaseElt, newBaseElt, labelMap);
        relocateTools(oldBaseElt, wiringElt, labelMap);
        relocateTools(gatesElt, wiringElt, labelMap);
        updateFromLabelMap(XmlIterator.forDescendantElements(root, "comp"), labelMap);
        updateFromLabelMap(XmlIterator.forDescendantElements(root, "tool"), labelMap);
    }

    private static void addToLabelMap(Map<String, String> labelMap, String srcLabel,
                                      String dstLabel, String toolNames) {
        if (srcLabel != null && dstLabel != null) {
            for (String tool : toolNames.split(";")) {
                labelMap.put(srcLabel + ':' + tool, dstLabel);
            }
        }
    }

    private static void relocateTools(Element src, Node dest,
                                      Map<String, String> labelMap) {
        if (src == null || src == dest) {
            return;
        }

        String srcLabel = src.getAttribute("name");
        if (srcLabel == null) {
            return;
        }


        List<Element> toRemove = new ArrayList<>();
        for (Element elt : XmlIterator.forChildElements(src, "tool")) {
            String name = elt.getAttribute("name");
            if (name != null && labelMap.containsKey(srcLabel + ':' + name)) {
                toRemove.add(elt);
            }
        }
        for (Element elt : toRemove) {
            src.removeChild(elt);
            if (dest != null) {
                dest.appendChild(elt);
            }
        }
    }

    private static void updateFromLabelMap(Iterable<Element> elts,
                                           Map<String, String> labelMap) {
        for (Element elt : elts) {
            String oldLib = elt.getAttribute("lib");
            String name = elt.getAttribute("name");
            if (oldLib != null && name != null) {
                String newLib = labelMap.get(oldLib + ':' + name);
                if (newLib != null) {
                    elt.setAttribute("lib", newLib);
                }
            }
        }
    }

    private static void repairForLegacyLibrary(Document doc, Element root) {
        Element legacyElt = null;
        String legacyLabel = null;
        for (Element libElt : XmlIterator.forChildElements(root, "lib")) {
            String desc = libElt.getAttribute("desc");
            String label = libElt.getAttribute("name");
            if (desc != null && desc.equals("#Legacy")) {
                legacyElt = libElt;
                legacyLabel = label;
            }
        }

        if (legacyElt != null) {
            root.removeChild(legacyElt);

            ArrayList<Element> toRemove = new ArrayList<>();
            findLibraryUses(toRemove, legacyLabel,
                    XmlIterator.forDescendantElements(root, "comp"));
            boolean componentsRemoved = !toRemove.isEmpty();
            findLibraryUses(toRemove, legacyLabel,
                    XmlIterator.forDescendantElements(root, "tool"));
            for (Element elt : toRemove) {
                elt.getParentNode().removeChild(elt);
            }
            if (componentsRemoved) {
                String error = "Some components have been deleted;"
                    + " the Legacy library is no longer supported.";
                Element elt = doc.createElement("message");
                elt.setAttribute("value", error);
                root.appendChild(elt);
            }
        }
    }

    private static void findLibraryUses(List<Element> dest, String label,
                                        Iterable<Element> candidates) {
        for (Element elt : candidates) {
            String lib = elt.getAttribute("lib");
            if (lib.equals(label)) {
                dest.add(elt);
            }
        }
    }
}
