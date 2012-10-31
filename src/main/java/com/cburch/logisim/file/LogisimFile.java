/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.SubcircuitFactory;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.proj.Projects;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.EventSourceWeakSupport;
import static com.cburch.logisim.util.LocaleString.*;

public class LogisimFile extends Library implements LibraryEventSource {
	private static class WritingThread extends Thread {
		OutputStream out;
		LogisimFile file;

		WritingThread(OutputStream out, LogisimFile file) {
			this.out = out;
			this.file = file;
		}

		@Override
		public void run() {
			try {
				file.write(out, file.loader);
			} catch (IOException e) {
				file.loader.showError(_("fileDuplicateError", e.toString()));
			}
			try {
				out.close();
			} catch (IOException e) {
				file.loader.showError(_("fileDuplicateError", e.toString()));
			}
		}
	}

	private EventSourceWeakSupport<LibraryListener> listeners
		= new EventSourceWeakSupport<LibraryListener>();
	private Loader loader;
	private LinkedList<String> messages = new LinkedList<String>();
	private Options options = new Options();
	private LinkedList<AddTool> tools = new LinkedList<AddTool>();
	private LinkedList<Library> libraries = new LinkedList<Library>();
	private Circuit main = null;
	private String name;
	private boolean dirty = false;

	LogisimFile(Loader loader) {
		this.loader = loader;
		
		name = _("defaultProjectName");
		if (Projects.windowNamed(name)) {
			for (int i = 2; true; i++) {
				if (!Projects.windowNamed(name + " " + i)) {
					name += " " + i;
					break;
				}
			}
		}

	}

	//
	// access methods
	//
	@Override
	public String getName() { return name; }
	
	@Override
	public boolean isDirty() { return dirty; }

	public String getMessage() {
		if (messages.size() == 0) return null;
		return messages.removeFirst();
	}

	public Loader getLoader() {
		return loader;
	}

	public Options getOptions() {
		return options;
	}

	@Override
	public List<AddTool> getTools() {
		return tools;
	}

	@Override
	public List<Library> getLibraries() {
		return libraries;
	}

	public Circuit getCircuit(String name) {
		if (name == null) return null;
		for (AddTool tool : tools) {
			SubcircuitFactory factory = (SubcircuitFactory) tool.getFactory();
			if (name.equals(factory.getName())) return factory.getSubcircuit();
		}
		return null;
	}
	
	public boolean contains(Circuit circ) {
		for (AddTool tool : tools) {
			SubcircuitFactory factory = (SubcircuitFactory) tool.getFactory();
			if (factory.getSubcircuit() == circ) return true;
		}
		return false;
	}
	
	public List<Circuit> getCircuits() {
		List<Circuit> ret = new ArrayList<Circuit>(tools.size());
		for (AddTool tool : tools) {
			SubcircuitFactory factory = (SubcircuitFactory) tool.getFactory();
			ret.add(factory.getSubcircuit());
		}
		return ret;
	}
	
	public AddTool getAddTool(Circuit circ) {
		for (AddTool tool : tools) {
			SubcircuitFactory factory = (SubcircuitFactory) tool.getFactory();
			if (factory.getSubcircuit() == circ) {
				return tool;
			}
		}
		return null;
	}

	public Circuit getMainCircuit() {
		return main;
	}

	public int getCircuitCount() {
		return tools.size();
	}

	//
	// listener methods
	//
	public void addLibraryListener(LibraryListener what) {
		listeners.add(what);
	}

	public void removeLibraryListener(LibraryListener what) {
		listeners.remove(what);
	}

	private void fireEvent(int action, Object data) {
		LibraryEvent e = new LibraryEvent(this, action, data);
		for (LibraryListener l : listeners) {
			l.libraryChanged(e);
		}
	}


	//
	// modification actions
	//
	public void addMessage(String msg) {
		messages.addLast(msg);
	}
	
	public void setDirty(boolean value) {
		if (dirty != value) {
			dirty = value;
			fireEvent(LibraryEvent.DIRTY_STATE, value ? Boolean.TRUE : Boolean.FALSE);
		}
	}

	public void setName(String name) {
		this.name = name;
		fireEvent(LibraryEvent.SET_NAME, name);
	}

	public void addCircuit(Circuit circuit) {
		addCircuit(circuit, tools.size());
	}
	
	public void addCircuit(Circuit circuit, int index) {
		AddTool tool = new AddTool(circuit.getSubcircuitFactory());
		tools.add(index, tool);
		if (tools.size() == 1) setMainCircuit(circuit);
		fireEvent(LibraryEvent.ADD_TOOL, tool);
	}

	public void removeCircuit(Circuit circuit) {
		if (tools.size() <= 1) {
			throw new RuntimeException("Cannot remove last circuit");
		}

		int index = getCircuits().indexOf(circuit);
		if (index >= 0) {
			Tool circuitTool = tools.remove(index);

			if (main == circuit) {
				AddTool dflt_tool = tools.get(0);
				SubcircuitFactory factory = (SubcircuitFactory) dflt_tool.getFactory();
				setMainCircuit(factory.getSubcircuit());
			}
			fireEvent(LibraryEvent.REMOVE_TOOL, circuitTool);
		}
	}
	
	public void moveCircuit(AddTool tool, int index) {
		int oldIndex = tools.indexOf(tool);
		if (oldIndex < 0) {
			tools.add(index, tool);
			fireEvent(LibraryEvent.ADD_TOOL, tool);
		} else {
			AddTool value = tools.remove(oldIndex);
			tools.add(index, value);
			fireEvent(LibraryEvent.MOVE_TOOL, tool);
		}
	}

	public void addLibrary(Library lib) {
		libraries.add(lib);
		fireEvent(LibraryEvent.ADD_LIBRARY, lib);
	}

	public void removeLibrary(Library lib) {
		libraries.remove(lib);
		fireEvent(LibraryEvent.REMOVE_LIBRARY, lib);
	}
	
	public String getUnloadLibraryMessage(Library lib) {
		HashSet<ComponentFactory> factories = new HashSet<ComponentFactory>();
		for (Tool tool : lib.getTools()) {
			if (tool instanceof AddTool) {
				factories.add(((AddTool) tool).getFactory());
			}
		}
		
		for (Circuit circuit : getCircuits()) {
			for (Component comp : circuit.getNonWires()) {
				if (factories.contains(comp.getFactory())) {
					return _("unloadUsedError",
							circuit.getName());
				}
			}
		}
		
		ToolbarData tb = options.getToolbarData();
		MouseMappings mm = options.getMouseMappings();
		for (Tool t : lib.getTools()) {
			if (tb.usesToolFromSource(t)) {
				return _("unloadToolbarError");
			}
			if (mm.usesToolFromSource(t)) {
				return _("unloadMappingError");
			}
		}
		
		return null;
	}

	public void setMainCircuit(Circuit circuit) {
		if (circuit == null) return;
		this.main = circuit;
		fireEvent(LibraryEvent.SET_MAIN, circuit);
	}

	//
	// other methods
	//
	void write(OutputStream out, LibraryLoader loader) throws IOException {
		try {
			XmlWriter.write(this, out, loader);
		} catch (TransformerConfigurationException e) {
			loader.showError("internal error configuring transformer");
		} catch (ParserConfigurationException e) {
			loader.showError("internal error configuring parser");
		} catch (TransformerException e) {
			String msg = e.getMessage();
			String err = _("xmlConversionError");
			if (msg == null) err += ": " + msg;
			loader.showError(err);
		}
	}

	public LogisimFile cloneLogisimFile(Loader newloader) {
		PipedInputStream reader = new PipedInputStream();
		PipedOutputStream writer = new PipedOutputStream();
		try {
			reader.connect(writer);
		} catch (IOException e) {
			newloader.showError(_("fileDuplicateError", e.toString()));
			try {
				reader.close();
			} catch (IOException e1) {
			}
			return null;
		}
		new WritingThread(writer, this).start();
		try {
			return LogisimFile.load(reader, newloader);
		} catch (IOException e) {
			newloader.showError(_("fileDuplicateError", e.toString()));
			return null;
		}
	}
	
	Tool findTool(Tool query) {
		for (Library lib : getLibraries()) {
			Tool ret = findTool(lib, query);
			if (ret != null) return ret;
		}
		return null;
	}
	
	private Tool findTool(Library lib, Tool query) {
		for (Tool tool : lib.getTools()) {
			if (tool.equals(query)) return tool;
		}
		return null;
	}

	//
	// creation methods
	//
	public static LogisimFile createNew(Loader loader) {
		LogisimFile ret = new LogisimFile(loader);
		ret.main = new Circuit("main");
		// The name will be changed in LogisimPreferences
		ret.tools.add(new AddTool(ret.main.getSubcircuitFactory()));
		return ret;
	}

	public static LogisimFile load(File file, Loader loader)
			throws IOException {
		InputStream in = new FileInputStream(file);
		Throwable firstExcept = null;
		try {
			return loadSub(in, loader);
		} catch (Throwable t) {
			firstExcept = t;
		} finally {
			in.close();
		}
		
		if (firstExcept != null) {
			// We'll now try to do it using a reader. This is to work around
			// Logisim versions prior to 2.5.1, when files were not saved using
			// UTF-8 as the encoding (though the XML file reported otherwise).
			try {
				in = new ReaderInputStream(new FileReader(file), "UTF8");
				return loadSub(in, loader);
			} catch (Throwable t) {
				loader.showError(_("xmlFormatError", firstExcept.toString()));
			} finally {
				try {
					in.close();
				} catch (Throwable t) { }
			}
		}
		
		return null;
	}
	
	public static LogisimFile load(InputStream in, Loader loader)
			throws IOException {
		try {
			return loadSub(in, loader);
		} catch (SAXException e) {
			loader.showError(_("xmlFormatError", e.toString()));
			return null;
		}
	}

	public static LogisimFile loadSub(InputStream in, Loader loader)
			throws IOException, SAXException {
		// fetch first line and then reset
		BufferedInputStream inBuffered = new BufferedInputStream(in);
		String firstLine = getFirstLine(inBuffered);

		if (firstLine == null) {
			throw new IOException("File is empty");
		} else if (firstLine.equals("Logisim v1.0")) {
			// if this is a 1.0 file, then set up a pipe to translate to
			// 2.0 and then interpret as a 2.0 file
			throw new IOException("Version 1.0 files no longer supported");
		}

		XmlReader xmlReader = new XmlReader(loader);
		LogisimFile ret = xmlReader.readLibrary(inBuffered);
		ret.loader = loader;
		return ret;
	}

	private static String getFirstLine(BufferedInputStream in)
			throws IOException {
		byte[] first = new byte[512];
		in.mark(first.length - 1);
		in.read(first);
		in.reset();
		
		int lineBreak = first.length;
		for (int i = 0; i < lineBreak; i++) {
			if (first[i] == '\n') {
				lineBreak = i;
			}
		}
		return new String(first, 0, lineBreak, "UTF-8");
	}
}
