/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.proj;

import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JFileChooser;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.Simulator;
import com.cburch.logisim.circuit.SubcircuitFactory;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.gui.log.LogFrame;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.gui.main.Selection;
import com.cburch.logisim.gui.main.SelectionActions;
import com.cburch.logisim.gui.opts.OptionsFrame;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.EventSourceWeakSupport;
import com.cburch.logisim.util.JFileChoosers;

public class Project {
	private static final int MAX_UNDO_SIZE = 64;

	private static class ActionData {
		CircuitState circuitState;
		Action action;

		public ActionData(CircuitState circuitState, Action action) {
			this.circuitState = circuitState;
			this.action = action;
		}
	}

	private class MyListener implements Selection.Listener, LibraryListener {
		public void selectionChanged(Selection.Event e) {
			fireEvent(ProjectEvent.ACTION_SELECTION, e.getSource());
		}
		
		public void libraryChanged(LibraryEvent event) {
			int action = event.getAction();
			if (action == LibraryEvent.REMOVE_LIBRARY) {
				Library unloaded = (Library) event.getData();
				if (tool != null && unloaded.containsFromSource(tool)) {
					setTool(null);
				}
			} else if (action == LibraryEvent.REMOVE_TOOL) {
				Object data = event.getData();
				if (data instanceof AddTool) {
					Object factory = ((AddTool) data).getFactory();
					if (factory instanceof SubcircuitFactory) {
						SubcircuitFactory fact = (SubcircuitFactory) factory;
						if (fact.getSubcircuit() == getCurrentCircuit()) {
							setCurrentCircuit(file.getMainCircuit());
						}
					}
				}
			}
		}
	}

	private Simulator simulator = new Simulator();
	private LogisimFile file;
	private CircuitState circuitState;
	private HashMap<Circuit,CircuitState> stateMap
		= new HashMap<Circuit,CircuitState>();
	private Frame frame = null;
	private OptionsFrame optionsFrame = null;
	private LogFrame logFrame = null;
	private Tool tool = null;
	private LinkedList<ActionData> undoLog = new LinkedList<ActionData>();
	private int undoMods = 0;
	private EventSourceWeakSupport<ProjectListener> projectListeners
		= new EventSourceWeakSupport<ProjectListener>();
	private EventSourceWeakSupport<LibraryListener> fileListeners
		= new EventSourceWeakSupport<LibraryListener>();
	private EventSourceWeakSupport<CircuitListener> circuitListeners
		= new EventSourceWeakSupport<CircuitListener>();
	private Dependencies depends;
	private MyListener myListener = new MyListener();
	private boolean startupScreen = false;

	public Project(LogisimFile file) {
		addLibraryListener(myListener);
		setLogisimFile(file);
	}

	public void setFrame(Frame value) {
		if (frame == value) return;
		Frame oldValue = frame;
		frame = value;
		Projects.windowCreated(this, oldValue, value);
		value.getCanvas().getSelection().addListener(myListener);
	}

	//
	// access methods
	//
	public LogisimFile getLogisimFile() {
		return file;
	}

	public Simulator getSimulator() {
		return simulator;
	}

	public Options getOptions() {
		return file.getOptions();
	}

	public Dependencies getDependencies() {
		return depends;
	}

	public Frame getFrame() {
		return frame;
	}
	
	public OptionsFrame getOptionsFrame(boolean create) {
		if (optionsFrame == null || optionsFrame.getLogisimFile() != file) {
			if (create) optionsFrame = new OptionsFrame(this);
			else optionsFrame = null;
		}
		return optionsFrame;
	}
	
	public LogFrame getLogFrame(boolean create) {
		if (logFrame == null) {
			if (create) logFrame = new LogFrame(this);
		}
		return logFrame;
	}

	public Circuit getCurrentCircuit() {
		return circuitState == null ? null : circuitState.getCircuit();
	}

	public CircuitState getCircuitState() {
		return circuitState;
	}
	
	public CircuitState getCircuitState(Circuit circuit) {
		if (circuitState != null && circuitState.getCircuit() == circuit) {
			return circuitState;
		} else {
			CircuitState ret = stateMap.get(circuit);
			if (ret == null) {
				ret = new CircuitState(this, circuit);
				stateMap.put(circuit, ret);
			}
			return ret;
		}
	}

	public Action getLastAction() {
		if (undoLog.size() == 0) {
			return null;
		} else {
			return undoLog.getLast().action;
		}
	}

	public Tool getTool() {
		return tool;
	}

	public Selection getSelection() {
		if (frame == null) return null;
		Canvas canvas = frame.getCanvas();
		if (canvas == null) return null;
		return canvas.getSelection();
	}

	public boolean isFileDirty() {
		return undoMods != 0;
	}

	public JFileChooser createChooser() {
		if (file == null) return JFileChoosers.create();
		Loader loader = file.getLoader();
		return loader == null ? JFileChoosers.create() : loader.createChooser();
	}

	//
	// Listener methods
	//
	public void addProjectListener(ProjectListener what) {
		projectListeners.add(what);
	}

	public void removeProjectListener(ProjectListener what) {
		projectListeners.remove(what);
	}
	
	public void addLibraryListener(LibraryListener value) {
		fileListeners.add(value);
		if (file != null) file.addLibraryListener(value);
	}
	
	public void removeLibraryListener(LibraryListener value) {
		fileListeners.remove(value);
		if (file != null) file.removeLibraryListener(value);
	}
	
	public void addCircuitListener(CircuitListener value) {
		circuitListeners.add(value);
		Circuit current = getCurrentCircuit();
		if (current != null) current.addCircuitListener(value);
	}
	
	public void removeCircuitListener(CircuitListener value) {
		circuitListeners.remove(value);
		Circuit current = getCurrentCircuit();
		if (current != null) current.removeCircuitListener(value);
	}

	private void fireEvent(int action, Object old, Object data) {
		fireEvent(new ProjectEvent(action, this, old, data));
	}

	private void fireEvent(int action, Object data) {
		fireEvent(new ProjectEvent(action, this, data));
	}

	private void fireEvent(ProjectEvent event) {
		for (ProjectListener l : projectListeners) {
			l.projectChanged(event);
		}
	}
	
	// We track whether this project is the empty project opened
	// at startup by default, because we want to close it
	// immediately as another project is opened, if there
	// haven't been any changes to it.
	public boolean isStartupScreen() {
		return startupScreen;
	}
	
	public boolean confirmClose(String title) {
		return frame.confirmClose(title);
	}

	//
	// actions
	//
	public void setStartupScreen(boolean value) {
		startupScreen = value;
	}

	public void setLogisimFile(LogisimFile value) {
		LogisimFile old = this.file;
		if (old != null) {
			for (LibraryListener l : fileListeners) {
				old.removeLibraryListener(l);
			}
		}
		file = value;
		stateMap.clear();
		depends = new Dependencies(file);
		undoLog.clear();
		undoMods = 0;
		fireEvent(ProjectEvent.ACTION_SET_FILE, old, file);
		setCurrentCircuit(file.getMainCircuit());
		if (file != null) {
			for (LibraryListener l : fileListeners) {
				file.addLibraryListener(l);
			}
		}
		file.setDirty(true); // toggle it so that everybody hears the file is fresh
		file.setDirty(false);
	}

	public void setCircuitState(CircuitState value) {
		if (value == null || circuitState == value) return;

		CircuitState old = circuitState;
		Circuit oldCircuit = old == null ? null : old.getCircuit();
		Circuit newCircuit = value.getCircuit();
		boolean circuitChanged = old == null || oldCircuit != newCircuit;
		if (circuitChanged) {
			Canvas canvas = frame == null ? null : frame.getCanvas();
			if (canvas != null) {
				if (tool != null) tool.deselect(canvas);
				Selection selection = canvas.getSelection();
				if (selection != null) {
					Action act = SelectionActions.dropAll(selection);
					if (act != null) {
						doAction(act);
					}
				}
				if (tool != null) tool.select(canvas);
			}
			if (oldCircuit != null) {
				for (CircuitListener l : circuitListeners) {
					oldCircuit.removeCircuitListener(l);
				}
			}
		}
		circuitState = value;
		stateMap.put(circuitState.getCircuit(), circuitState);
		simulator.setCircuitState(circuitState);
		if (circuitChanged) {
			fireEvent(ProjectEvent.ACTION_SET_CURRENT, oldCircuit, newCircuit);
			if (newCircuit != null) {
				for (CircuitListener l : circuitListeners) {
					newCircuit.addCircuitListener(l);
				}
			}
		}
		fireEvent(ProjectEvent.ACTION_SET_STATE, old, circuitState);
	}

	public void setCurrentCircuit(Circuit circuit) {
		CircuitState circState = stateMap.get(circuit);
		if (circState == null) circState = new CircuitState(this, circuit);
		setCircuitState(circState);
	}

	public void setTool(Tool value) {
		if (tool == value) return;
		Tool old = tool;
		Canvas canvas = frame.getCanvas();
		if (old != null) old.deselect(canvas);
		Selection selection = canvas.getSelection();
		if (selection != null && !selection.isEmpty()) {
			if (value == null || !getOptions().getMouseMappings().containsSelectTool()) {
				Action act = SelectionActions.anchorAll(selection);
				if (act != null) {
					doAction(act);
				}
			}
		}
		startupScreen = false;
		tool = value;
		if (tool != null) tool.select(frame.getCanvas());
		fireEvent(ProjectEvent.ACTION_SET_TOOL, old, tool);
	}

	public void doAction(Action act) {
		if (act == null) return;
		Action toAdd = act;
		startupScreen = false;
		if (!undoLog.isEmpty() && act.shouldAppendTo(getLastAction())) {
			ActionData firstData = undoLog.removeLast();
			Action first = firstData.action;
			if (first.isModification()) --undoMods;
			toAdd = first.append(act);
			if (toAdd != null) {
				undoLog.add(new ActionData(circuitState, toAdd));
				if (toAdd.isModification()) ++undoMods;
			}
			fireEvent(new ProjectEvent(ProjectEvent.ACTION_START, this, act));
			act.doIt(this);
			file.setDirty(isFileDirty());
			fireEvent(new ProjectEvent(ProjectEvent.ACTION_COMPLETE, this, act));
			fireEvent(new ProjectEvent(ProjectEvent.ACTION_MERGE, this, first, toAdd));
			return;
		}
		undoLog.add(new ActionData(circuitState, toAdd));
		fireEvent(new ProjectEvent(ProjectEvent.ACTION_START, this, act));
		act.doIt(this);
		while (undoLog.size() > MAX_UNDO_SIZE) {
			undoLog.removeFirst();
		}
		if (toAdd.isModification()) ++undoMods;
		file.setDirty(isFileDirty());
		fireEvent(new ProjectEvent(ProjectEvent.ACTION_COMPLETE, this, act));
	}

	public void undoAction() {
		if (undoLog != null && undoLog.size() > 0) {
			ActionData data = undoLog.removeLast();
			setCircuitState(data.circuitState);
			Action action = data.action;
			if (action.isModification()) --undoMods;
			fireEvent(new ProjectEvent(ProjectEvent.UNDO_START, this, action));
			action.undo(this);
			file.setDirty(isFileDirty());
			fireEvent(new ProjectEvent(ProjectEvent.UNDO_COMPLETE, this, action));
		}
	}

	public void setFileAsClean() {
		undoMods = 0;
		file.setDirty(isFileDirty());
	}

	public void repaintCanvas() {
		// for actions that ought not be logged (i.e., those that
		// change nothing, except perhaps the current values within
		// the circuit)
		fireEvent(new ProjectEvent(ProjectEvent.REPAINT_REQUEST, this, null));
	}
}
