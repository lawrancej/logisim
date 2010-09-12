/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.cburch.draw.canvas.CanvasModelEvent;
import com.cburch.draw.canvas.CanvasModelListener;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.Simulator;
import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.gui.appear.RevertAppearanceAction;
import com.cburch.logisim.gui.generic.CardPanel;
import com.cburch.logisim.gui.menu.LogisimMenuItem;
import com.cburch.logisim.gui.menu.ProjectCircuitActions;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.gui.menu.SimulateListener;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;

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

	private class EditListener implements ActionListener, EditHandler.Listener {
		private EditHandler handler = null;
		
		private void setHandler(EditHandler value) {
			handler = value;
			handler.computeEnabled();
		}
		
		private void register() {
			menubar.addActionListener(LogisimMenuBar.CUT, this);
			menubar.addActionListener(LogisimMenuBar.COPY, this);
			menubar.addActionListener(LogisimMenuBar.PASTE, this);
			menubar.addActionListener(LogisimMenuBar.DELETE, this);
			menubar.addActionListener(LogisimMenuBar.DUPLICATE, this);
			menubar.addActionListener(LogisimMenuBar.SELECT_ALL, this);
			if (handler != null) handler.computeEnabled();
		}
		
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			EditHandler h = handler;
			if (src == LogisimMenuBar.CUT) {
				if (h != null) h.cut();
			} else if (src == LogisimMenuBar.COPY) {
				if (h != null) h.copy();
			} else if (src == LogisimMenuBar.PASTE) {
				if (h != null) h.paste();
			} else if (src == LogisimMenuBar.DELETE) {
				if (h != null) h.delete();
			} else if (src == LogisimMenuBar.DUPLICATE) {
				if (h != null) h.duplicate();
			} else if (src == LogisimMenuBar.SELECT_ALL) {
				if (h != null) h.selectAll();
			}
		}

		public void enableChanged(EditHandler handler, LogisimMenuItem action,
				boolean value) {
			if (handler == this.handler) {
				menubar.setEnabled(action, value);
			}
		}
	}

	class ProjectMenuListener implements ProjectListener, LibraryListener,
				ActionListener, ChangeListener, CanvasModelListener {
		void register(CardPanel mainPanel) {
			Project proj = frame.getProject();
			if (proj == null) {
				return;
			}

			proj.addProjectListener(this);
			proj.addLibraryListener(this);
			mainPanel.addChangeListener(this);
			Circuit circ = proj.getCurrentCircuit();
			if (circ != null) { 
				circ.getAppearance().addCanvasModelListener(this);
			}
			
			projbar.setActionListener(this);
			menubar.addActionListener(LogisimMenuBar.ADD_CIRCUIT, this);
			menubar.addActionListener(LogisimMenuBar.MOVE_CIRCUIT_UP, this);
			menubar.addActionListener(LogisimMenuBar.MOVE_CIRCUIT_DOWN, this);
			menubar.addActionListener(LogisimMenuBar.SET_MAIN_CIRCUIT, this);
			menubar.addActionListener(LogisimMenuBar.REMOVE_CIRCUIT, this);
			menubar.addActionListener(LogisimMenuBar.EDIT_LAYOUT, this);
			menubar.addActionListener(LogisimMenuBar.EDIT_APPEARANCE, this);
			menubar.addActionListener(LogisimMenuBar.REVERT_APPEARANCE, this);
			menubar.addActionListener(LogisimMenuBar.ANALYZE_CIRCUIT, this);
			menubar.addActionListener(LogisimMenuBar.CIRCUIT_STATS, this);
			
			computeEnabled();
		}

		public void modelChanged(CanvasModelEvent event) {
			computeRevertEnabled();
		}

		public void projectChanged(ProjectEvent event) {
			int action = event.getAction();
			if (action == ProjectEvent.ACTION_SET_CURRENT) {
				Circuit old = (Circuit) event.getOldData();
				if (old != null) {
					old.getAppearance().removeCanvasModelListener(this);
				}
				Circuit circ = (Circuit) event.getData();
				if (circ != null) { 
					circ.getAppearance().addCanvasModelListener(this);
				}
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
			} else if (src == LogisimMenuBar.MOVE_CIRCUIT_UP) {
				ProjectCircuitActions.doMoveCircuit(proj, cur, -1);
			} else if (src == LogisimMenuBar.MOVE_CIRCUIT_DOWN) {
				ProjectCircuitActions.doMoveCircuit(proj, cur, 1);
			} else if (src == LogisimMenuBar.SET_MAIN_CIRCUIT) {
				ProjectCircuitActions.doSetAsMainCircuit(proj, cur);
			} else if (src == LogisimMenuBar.REMOVE_CIRCUIT) {
				ProjectCircuitActions.doRemoveCircuit(proj, cur);
			} else if (src == LogisimMenuBar.EDIT_LAYOUT) {
				frame.setView(Frame.LAYOUT);
			} else if (src == LogisimMenuBar.EDIT_APPEARANCE) {
				frame.setView(Frame.APPEARANCE);
			} else if (src == LogisimMenuBar.REVERT_APPEARANCE) {
				proj.doAction(new RevertAppearanceAction(cur));
			} else if (src == LogisimMenuBar.ANALYZE_CIRCUIT) {
				ProjectCircuitActions.doAnalyze(proj, cur);
			} else if (src == LogisimMenuBar.CIRCUIT_STATS) {
				StatisticsDialog.show(frame, proj.getLogisimFile(), cur);
			}
		}
		
		private void computeEnabled() {
			Project proj = frame.getProject();
			LogisimFile file = proj.getLogisimFile();
			Circuit cur = proj.getCurrentCircuit();
			int curIndex = file.getCircuits().indexOf(cur);
			boolean isProjectCircuit = curIndex >= 0;
			String view = frame.getView();
			boolean canSetMain = false;
			boolean canMoveUp = false;
			boolean canMoveDown = false;
			boolean canRemove = false;
			boolean canRevert = false;
			boolean viewAppearance = view.equals(Frame.APPEARANCE);
			boolean viewLayout = view.equals(Frame.LAYOUT);
			if (isProjectCircuit) {
				List<?> tools = proj.getLogisimFile().getTools();

				canSetMain = proj.getLogisimFile().getMainCircuit() != cur;
				canMoveUp = curIndex > 0;
				canMoveDown = curIndex < tools.size() - 1;
				canRemove = tools.size() > 1;
				canRevert = viewAppearance
					&& !cur.getAppearance().isDefaultAppearance();
			}
			
			menubar.setEnabled(LogisimMenuBar.ADD_CIRCUIT, true);
			menubar.setEnabled(LogisimMenuBar.MOVE_CIRCUIT_UP, canMoveUp);
			menubar.setEnabled(LogisimMenuBar.MOVE_CIRCUIT_DOWN, canMoveDown);
			menubar.setEnabled(LogisimMenuBar.SET_MAIN_CIRCUIT, canSetMain);
			menubar.setEnabled(LogisimMenuBar.REMOVE_CIRCUIT, canRemove);
			projbar.setEnabled(LogisimMenuBar.ADD_CIRCUIT, true);
			projbar.setEnabled(LogisimMenuBar.MOVE_CIRCUIT_UP, canMoveUp);
			projbar.setEnabled(LogisimMenuBar.MOVE_CIRCUIT_DOWN, canMoveDown);
			projbar.setEnabled(LogisimMenuBar.REMOVE_CIRCUIT, canRemove);
			menubar.setEnabled(LogisimMenuBar.EDIT_LAYOUT, !viewLayout);
			menubar.setEnabled(LogisimMenuBar.EDIT_APPEARANCE, !viewAppearance);
			menubar.setEnabled(LogisimMenuBar.REVERT_APPEARANCE, canRevert);
			projbar.setEnabled(LogisimMenuBar.EDIT_LAYOUT, !view.equals(Frame.LAYOUT));
			projbar.setEnabled(LogisimMenuBar.EDIT_APPEARANCE, !view.equals(Frame.APPEARANCE));
			menubar.setEnabled(LogisimMenuBar.ANALYZE_CIRCUIT, true);
			menubar.setEnabled(LogisimMenuBar.CIRCUIT_STATS, true);
		}
		
		private void computeRevertEnabled() {
			// do this separately since it can happen rather often
			Project proj = frame.getProject();
			LogisimFile file = proj.getLogisimFile();
			Circuit cur = proj.getCurrentCircuit();
			boolean isProjectCircuit = file.contains(cur);
			boolean viewAppearance = frame.getView().equals(Frame.APPEARANCE);
			boolean canRevert = isProjectCircuit && viewAppearance
				&& !cur.getAppearance().isDefaultAppearance();
			menubar.setEnabled(LogisimMenuBar.REVERT_APPEARANCE, canRevert);
		}

		public void stateChanged(ChangeEvent e) {
			computeEnabled();
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
	private ProjectToolbarModel projbar;
	private FileListener fileListener = new FileListener();
	private EditListener editListener = new EditListener();
	private ProjectMenuListener projectListener = new ProjectMenuListener();
	private SimulateMenuListener simulateListener = new SimulateMenuListener();

	public MenuListener(Frame frame, LogisimMenuBar menubar,
			ProjectToolbarModel projectToolbar) {
		this.frame = frame;
		this.menubar = menubar;
		this.projbar = projectToolbar;
	}
	
	public void register(CardPanel mainPanel) {
		fileListener.register();
		editListener.register();
		projectListener.register(mainPanel);
		simulateListener.register();
	}

	public void setEditHandler(EditHandler handler) {
		editListener.setHandler(handler);
	}
}

