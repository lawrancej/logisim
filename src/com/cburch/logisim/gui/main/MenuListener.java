/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.Simulator;
import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.gui.menu.ProjectCircuitActions;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.gui.menu.SimulateListener;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import com.cburch.logisim.std.base.Base;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;

class MenuListener {
	private class FileListener implements ActionListener {
		private void register() {
			menubar.addActionListener(LogisimMenuBar.EXPORT_IMAGE, this);
			menubar.addActionListener(LogisimMenuBar.PRINT, this);
		}
		
		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();
			Project proj = frame.getProject();
			if (src == LogisimMenuBar.EXPORT_IMAGE) {
				ExportImage.doExport(proj);
			} else if (src == LogisimMenuBar.PRINT) {
				Print.doPrint(proj);
			}
		}
	}

	private class EditListener
			implements ProjectListener, LibraryListener, PropertyChangeListener,
				ActionListener {
		private void register() {
			Project proj = frame.getProject();
			Clipboard.addPropertyChangeListener(Clipboard.contentsProperty, this);
			proj.addProjectListener(this);
			proj.addLibraryListener(this);
			
			menubar.addActionListener(LogisimMenuBar.CUT, this);
			menubar.addActionListener(LogisimMenuBar.COPY, this);
			menubar.addActionListener(LogisimMenuBar.PASTE, this);
			menubar.addActionListener(LogisimMenuBar.DELETE, this);
			menubar.addActionListener(LogisimMenuBar.DUPLICATE, this);
			menubar.addActionListener(LogisimMenuBar.SELECT_ALL, this);
			enableItems();
		}

		public void projectChanged(ProjectEvent e) {
			int action = e.getAction();
			if (action == ProjectEvent.ACTION_SET_FILE) {
				enableItems();
			} else if (action == ProjectEvent.ACTION_SET_CURRENT) {
				enableItems();
			} else if (action == ProjectEvent.ACTION_SELECTION) {
				enableItems();
			}
		}

		public void libraryChanged(LibraryEvent e) {
			int action = e.getAction();
			if (action == LibraryEvent.ADD_LIBRARY) {
				enableItems();
			} else if (action == LibraryEvent.REMOVE_LIBRARY) {
				enableItems();
			}
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getPropertyName().equals(Clipboard.contentsProperty)) {
				enableItems();
			}
		}
		
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			Project proj = frame.getProject();
			Selection sel = frame.getCanvas().getSelection();
			if (src == LogisimMenuBar.CUT) {
				proj.doAction(SelectionActions.cut(sel));
			} else if (src == LogisimMenuBar.COPY) {
				proj.doAction(SelectionActions.copy(sel));
			} else if (src == LogisimMenuBar.PASTE) {
				selectSelectTool(proj);
				proj.doAction(SelectionActions.paste(sel));
			} else if (src == LogisimMenuBar.DELETE) {
				proj.doAction(SelectionActions.clear(sel));
			} else if (src == LogisimMenuBar.DUPLICATE) {
				proj.doAction(SelectionActions.duplicate(sel));
			} else if (src == LogisimMenuBar.SELECT_ALL) {
				selectSelectTool(proj);
				Circuit circ = proj.getCurrentCircuit();
				sel.addAll(circ.getWires());
				sel.addAll(circ.getNonWires());
				proj.repaintCanvas();
			}
		}
		
		private void selectSelectTool(Project proj) {
			for (Library sub : proj.getLogisimFile().getLibraries()) {
				if (sub instanceof Base) {
					Base base = (Base) sub;
					Tool tool = base.getTool("Edit Tool");
					if (tool != null) proj.setTool(tool);
				}
			}
		}

		public void enableItems() {
			Project proj = frame.getProject();
			Selection sel = proj == null ? null : proj.getSelection();
			boolean selEmpty = (sel == null ? true : sel.isEmpty());
			boolean canChange = proj != null && proj.getLogisimFile().contains(proj.getCurrentCircuit());
			
			boolean selectAvailable = false;
			for (Library lib : proj.getLogisimFile().getLibraries()) {
				if (lib instanceof Base) selectAvailable = true;
			}

			menubar.setEnabled(LogisimMenuBar.CUT, !selEmpty && selectAvailable && canChange);
			menubar.setEnabled(LogisimMenuBar.COPY, !selEmpty && selectAvailable);
			menubar.setEnabled(LogisimMenuBar.PASTE, selectAvailable && canChange
				&& !Clipboard.isEmpty());
			menubar.setEnabled(LogisimMenuBar.DELETE, !selEmpty && selectAvailable && canChange);
			menubar.setEnabled(LogisimMenuBar.DUPLICATE, !selEmpty && selectAvailable && canChange);
			menubar.setEnabled(LogisimMenuBar.SELECT_ALL, selectAvailable);
		}
	}

	class ProjectMenuListener implements ProjectListener, LibraryListener,
				ActionListener {
		void register() {
			Project proj = frame.getProject();
			if (proj == null) {
				return;
			}

			proj.addProjectListener(this);
			proj.addLibraryListener(this);
			
			projbar.setActionListener(this);
			menubar.addActionListener(LogisimMenuBar.ADD_CIRCUIT, this);
			menubar.addActionListener(LogisimMenuBar.MOVE_CIRCUIT_UP, this);
			menubar.addActionListener(LogisimMenuBar.MOVE_CIRCUIT_DOWN, this);
			menubar.addActionListener(LogisimMenuBar.SET_MAIN_CIRCUIT, this);
			menubar.addActionListener(LogisimMenuBar.REMOVE_CIRCUIT, this);
			menubar.addActionListener(LogisimMenuBar.ANALYZE_CIRCUIT, this);
			menubar.addActionListener(LogisimMenuBar.CIRCUIT_STATS, this);
			
			computeEnabled();
		}

		public void projectChanged(ProjectEvent event) {
			int action = event.getAction();
			if (action == ProjectEvent.ACTION_SET_CURRENT) {
				computeEnabled();
			} else if (action == ProjectEvent.ACTION_SET_FILE) {
				computeEnabled();
			}
		}
		
		public void libraryChanged(LibraryEvent event) {
			computeEnabled();
		}
		
		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();
			Project proj = frame.getProject();
			Circuit cur = proj == null ? null : proj.getCurrentCircuit();
			if (src == LogisimMenuBar.ADD_CIRCUIT) {
				ProjectCircuitActions.doAddCircuit(proj);
			} else if (src == LogisimMenuBar.ANALYZE_CIRCUIT) {
				ProjectCircuitActions.doAnalyze(proj, cur);
			} else if (src == LogisimMenuBar.CIRCUIT_STATS) {
				StatisticsDialog.show(frame, proj.getLogisimFile(), cur);
			} else if (src == LogisimMenuBar.MOVE_CIRCUIT_UP) {
				ProjectCircuitActions.doMoveCircuit(proj, cur, -1);
			} else if (src == LogisimMenuBar.MOVE_CIRCUIT_DOWN) {
				ProjectCircuitActions.doMoveCircuit(proj, cur, 1);
			} else if (src == LogisimMenuBar.SET_MAIN_CIRCUIT) {
				ProjectCircuitActions.doSetAsMainCircuit(proj, cur);
			} else if (src == LogisimMenuBar.REMOVE_CIRCUIT) {
				ProjectCircuitActions.doRemoveCircuit(proj, cur);
			}
		}
		
		private void computeEnabled() {
			Project proj = frame.getProject();
			Circuit cur = proj.getCurrentCircuit();
			boolean isProjectCircuit = proj.getLogisimFile().contains(cur);
			boolean canSetMain = false;
			boolean canMoveUp = false;
			boolean canMoveDown = false;
			boolean canRemove = false;
			if (isProjectCircuit) {
				List<AddTool> tools = proj.getLogisimFile().getTools();
				Object firstTool = tools.get(0);
				Object lastTool = tools.get(tools.size() - 1);

				canSetMain = proj.getLogisimFile().getMainCircuit() != cur;
				canMoveUp = firstTool instanceof AddTool
					&& ((AddTool) firstTool).getFactory() != cur;
				canMoveDown = lastTool instanceof AddTool
					&& ((AddTool) lastTool).getFactory() != cur;
				canRemove = proj.getLogisimFile().getTools().size() > 1;
			}
			
			menubar.setEnabled(LogisimMenuBar.ADD_CIRCUIT, true);
			menubar.setEnabled(LogisimMenuBar.ANALYZE_CIRCUIT, true);
			menubar.setEnabled(LogisimMenuBar.CIRCUIT_STATS, true);
			menubar.setEnabled(LogisimMenuBar.MOVE_CIRCUIT_UP, canMoveUp);
			menubar.setEnabled(LogisimMenuBar.MOVE_CIRCUIT_DOWN, canMoveDown);
			menubar.setEnabled(LogisimMenuBar.SET_MAIN_CIRCUIT, canSetMain);
			menubar.setEnabled(LogisimMenuBar.REMOVE_CIRCUIT, canRemove);
			projbar.setEnabled(LogisimMenuBar.ADD_CIRCUIT, true);
			projbar.setEnabled(LogisimMenuBar.MOVE_CIRCUIT_UP, canMoveUp);
			projbar.setEnabled(LogisimMenuBar.MOVE_CIRCUIT_DOWN, canMoveDown);
			projbar.setEnabled(LogisimMenuBar.REMOVE_CIRCUIT, canRemove);
		}
	}

	class SimulateMenuListener implements ProjectListener, SimulateListener {
		void register() {
			Project proj = frame.getProject();
			proj.addProjectListener(this);
			menubar.setSimulateListener(this);
			menubar.setCircuitState(proj.getSimulator(), proj.getCircuitState());
		}
		
		public void projectChanged(ProjectEvent event) {
			if (event.getAction() == ProjectEvent.ACTION_SET_STATE) {
				menubar.setCircuitState(frame.getProject().getSimulator(),
						frame.getProject().getCircuitState());
			}
		}

		public void stateChangeRequested(Simulator sim, CircuitState state) {
			if (state != null) frame.getProject().setCircuitState(state);
		}
	}
	
	private Frame frame;
	private LogisimMenuBar menubar;
	private ProjectToolbar projbar;
	private FileListener fileListener = new FileListener();
	private EditListener editListener = new EditListener();
	private ProjectMenuListener projectListener = new ProjectMenuListener();
	private SimulateMenuListener simulateListener = new SimulateMenuListener();

	public MenuListener(Frame frame, LogisimMenuBar menubar,
			ProjectToolbar projectToolbar) {
		this.frame = frame;
		this.menubar = menubar;
		this.projbar = projectToolbar;
	}
	
	public void register() {
		fileListener.register();
		editListener.register();
		projectListener.register();
		simulateListener.register();
	}

}

