/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.cburch.logisim.proj.Project;
import static com.cburch.logisim.util.LocaleString.*;

class MenuProject extends Menu {
	private class MyListener
			implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();
			Project proj = menubar.getProject();
			if (src == loadBuiltin) {
				ProjectLibraryActions.doLoadBuiltinLibrary(proj);
			} else if (src == loadLogisim) {
				ProjectLibraryActions.doLoadLogisimLibrary(proj);
			} else if (src == loadJar) {
				ProjectLibraryActions.doLoadJarLibrary(proj);
			} else if (src == unload) {
				ProjectLibraryActions.doUnloadLibraries(proj);
			} else if (src == options) {
				JFrame frame = proj.getOptionsFrame(true);
				frame.setVisible(true);
			}
		}
	}
	
	private LogisimMenuBar menubar;
	private MyListener myListener = new MyListener();
	
	private MenuItemImpl addCircuit = new MenuItemImpl(this, LogisimMenuBar.ADD_CIRCUIT);
	private JMenu loadLibrary = new JMenu();
	private JMenuItem loadBuiltin = new JMenuItem();
	private JMenuItem loadLogisim = new JMenuItem();
	private JMenuItem loadJar = new JMenuItem();
	private JMenuItem unload = new JMenuItem();
	private MenuItemImpl moveUp = new MenuItemImpl(this, LogisimMenuBar.MOVE_CIRCUIT_UP);
	private MenuItemImpl moveDown = new MenuItemImpl(this, LogisimMenuBar.MOVE_CIRCUIT_DOWN);
	private MenuItemImpl remove = new MenuItemImpl(this, LogisimMenuBar.REMOVE_CIRCUIT);
	private MenuItemImpl setAsMain = new MenuItemImpl(this, LogisimMenuBar.SET_MAIN_CIRCUIT);
	private MenuItemImpl revertAppearance = new MenuItemImpl(this, LogisimMenuBar.REVERT_APPEARANCE);
	private MenuItemImpl layout = new MenuItemImpl(this, LogisimMenuBar.EDIT_LAYOUT);
	private MenuItemImpl appearance = new MenuItemImpl(this, LogisimMenuBar.EDIT_APPEARANCE);
	private MenuItemImpl viewToolbox = new MenuItemImpl(this, LogisimMenuBar.VIEW_TOOLBOX);
	private MenuItemImpl viewSimulation = new MenuItemImpl(this, LogisimMenuBar.VIEW_SIMULATION);
	private MenuItemImpl analyze = new MenuItemImpl(this, LogisimMenuBar.ANALYZE_CIRCUIT);
	private MenuItemImpl stats = new MenuItemImpl(this, LogisimMenuBar.CIRCUIT_STATS);
	private JMenuItem options = new JMenuItem();

	MenuProject(LogisimMenuBar menubar) {
		this.menubar = menubar;

		menubar.registerItem(LogisimMenuBar.ADD_CIRCUIT, addCircuit);
		loadBuiltin.addActionListener(myListener);
		loadLogisim.addActionListener(myListener);
		loadJar.addActionListener(myListener);
		unload.addActionListener(myListener);
		menubar.registerItem(LogisimMenuBar.MOVE_CIRCUIT_UP, moveUp);
		menubar.registerItem(LogisimMenuBar.MOVE_CIRCUIT_DOWN, moveDown);
		menubar.registerItem(LogisimMenuBar.SET_MAIN_CIRCUIT, setAsMain);
		menubar.registerItem(LogisimMenuBar.REMOVE_CIRCUIT, remove);
		menubar.registerItem(LogisimMenuBar.REVERT_APPEARANCE, revertAppearance);
		menubar.registerItem(LogisimMenuBar.EDIT_LAYOUT, layout);
		menubar.registerItem(LogisimMenuBar.EDIT_APPEARANCE, appearance);
		menubar.registerItem(LogisimMenuBar.VIEW_TOOLBOX, viewToolbox);
		menubar.registerItem(LogisimMenuBar.VIEW_SIMULATION, viewSimulation);
		menubar.registerItem(LogisimMenuBar.ANALYZE_CIRCUIT, analyze);
		menubar.registerItem(LogisimMenuBar.CIRCUIT_STATS, stats);
		options.addActionListener(myListener);
		
		loadLibrary.add(loadBuiltin);
		loadLibrary.add(loadLogisim);
		loadLibrary.add(loadJar);
		
		add(addCircuit);
		add(loadLibrary);
		add(unload);
		addSeparator();
		add(moveUp);
		add(moveDown);
		add(setAsMain);
		add(remove);
		add(revertAppearance);
		addSeparator();
		add(viewToolbox);
		add(viewSimulation);
		add(layout);
		add(appearance);
		addSeparator();
		add(analyze);
		add(stats);
		addSeparator();
		add(options);

		boolean known = menubar.getProject() != null;
		loadLibrary.setEnabled(known);
		loadBuiltin.setEnabled(known);
		loadLogisim.setEnabled(known);
		loadJar.setEnabled(known);
		unload.setEnabled(known);
		options.setEnabled(known);
		computeEnabled();
	}
	
	public void localeChanged() {
		setText(_("projectMenu"));
		addCircuit.setText(_("projectAddCircuitItem"));
		loadLibrary.setText(_("projectLoadLibraryItem"));
		loadBuiltin.setText(_("projectLoadBuiltinItem"));
		loadLogisim.setText(_("projectLoadLogisimItem"));
		loadJar.setText(_("projectLoadJarItem"));
		unload.setText(_("projectUnloadLibrariesItem"));
		moveUp.setText(_("projectMoveCircuitUpItem"));
		moveDown.setText(_("projectMoveCircuitDownItem"));
		setAsMain.setText(_("projectSetAsMainItem"));
		remove.setText(_("projectRemoveCircuitItem"));
		revertAppearance.setText(_("projectRevertAppearanceItem"));
		layout.setText(_("projectEditCircuitLayoutItem"));
		appearance.setText(_("projectEditCircuitAppearanceItem"));
		viewToolbox.setText(_("projectViewToolboxItem"));
		viewSimulation.setText(_("projectViewSimulationItem"));
		analyze.setText(_("projectAnalyzeCircuitItem"));
		stats.setText(_("projectGetCircuitStatisticsItem"));
		options.setText(_("projectOptionsItem"));
	}
	
	@Override
	void computeEnabled() {
		setEnabled(menubar.getProject() != null
				|| addCircuit.hasListeners()
				|| moveUp.hasListeners()
				|| moveDown.hasListeners()
				|| setAsMain.hasListeners()
				|| remove.hasListeners()
				|| layout.hasListeners()
				|| revertAppearance.hasListeners()
				|| appearance.hasListeners()
				|| viewToolbox.hasListeners()
				|| viewSimulation.hasListeners()
				|| analyze.hasListeners()
				|| stats.hasListeners());
		menubar.fireEnableChanged();
	}
}
