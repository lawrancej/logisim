/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.cburch.logisim.proj.LogisimPreferences;
import com.cburch.logisim.proj.Project;

class MenuProject extends Menu {
	private class MyListener
			implements ActionListener, PropertyChangeListener, ItemListener {
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
		
		public void propertyChange(PropertyChangeEvent event) {
			String prop = event.getPropertyName();
			if (prop.equals(LogisimPreferences.SHOW_PROJECT_TOOLBAR)) {
				showProjectToolbar.setSelected(LogisimPreferences.getShowProjectToolbar());
			}
		}
		 
		public void itemStateChanged(ItemEvent event) {
			Object src = event.getSource();
			if (src == showProjectToolbar) {
				LogisimPreferences.setShowProjectToolbar(showProjectToolbar.isSelected());
			}
		}
	}
	
	private LogisimMenuBar menubar;
	private MyListener myListener = new MyListener();
	
	private MenuItem addCircuit = new MenuItem(this, LogisimMenuBar.ADD_CIRCUIT);
	private JMenu loadLibrary = new JMenu();
	private JMenuItem loadBuiltin = new JMenuItem();
	private JMenuItem loadLogisim = new JMenuItem();
	private JMenuItem loadJar = new JMenuItem();
	private JMenuItem unload = new JMenuItem();
	private MenuItem moveUp = new MenuItem(this, LogisimMenuBar.MOVE_CIRCUIT_UP);
	private MenuItem moveDown = new MenuItem(this, LogisimMenuBar.MOVE_CIRCUIT_DOWN);
	private MenuItem remove = new MenuItem(this, LogisimMenuBar.REMOVE_CIRCUIT);
	private MenuItem setAsMain = new MenuItem(this, LogisimMenuBar.SET_MAIN_CIRCUIT);
	private MenuItem layout = new MenuItem(this, LogisimMenuBar.EDIT_LAYOUT);
	private MenuItem appearance = new MenuItem(this, LogisimMenuBar.EDIT_APPEARANCE);
	private MenuItem revertAppearance = new MenuItem(this, LogisimMenuBar.REVERT_APPEARANCE);
	private MenuItem analyze = new MenuItem(this, LogisimMenuBar.ANALYZE_CIRCUIT);
	private MenuItem stats = new MenuItem(this, LogisimMenuBar.CIRCUIT_STATS);
	private JMenuItem options = new JMenuItem();
	private JCheckBoxMenuItem showProjectToolbar = new JCheckBoxMenuItem();

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
		menubar.registerItem(LogisimMenuBar.EDIT_LAYOUT, layout);
		menubar.registerItem(LogisimMenuBar.EDIT_APPEARANCE, appearance);
		menubar.registerItem(LogisimMenuBar.REVERT_APPEARANCE, revertAppearance);
		menubar.registerItem(LogisimMenuBar.ANALYZE_CIRCUIT, analyze);
		menubar.registerItem(LogisimMenuBar.CIRCUIT_STATS, stats);
		options.addActionListener(myListener);
		LogisimPreferences.addPropertyChangeListener(LogisimPreferences.SHOW_PROJECT_TOOLBAR, myListener);
		showProjectToolbar.setSelected(LogisimPreferences.getShowProjectToolbar());
		showProjectToolbar.addItemListener(myListener);
		
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
		addSeparator();
		add(layout);
		add(appearance);
		add(revertAppearance);
		add(analyze);
		add(stats);
		addSeparator();
		add(showProjectToolbar);
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
		setText(Strings.get("projectMenu"));
		addCircuit.setText(Strings.get("projectAddCircuitItem"));
		loadLibrary.setText(Strings.get("projectLoadLibraryItem"));
		loadBuiltin.setText(Strings.get("projectLoadBuiltinItem"));
		loadLogisim.setText(Strings.get("projectLoadLogisimItem"));
		loadJar.setText(Strings.get("projectLoadJarItem"));
		unload.setText(Strings.get("projectUnloadLibrariesItem"));
		moveUp.setText(Strings.get("projectMoveCircuitUpItem"));
		moveDown.setText(Strings.get("projectMoveCircuitDownItem"));
		setAsMain.setText(Strings.get("projectSetAsMainItem"));
		remove.setText(Strings.get("projectRemoveCircuitItem"));
		layout.setText(Strings.get("projectEditCircuitLayoutItem"));
		appearance.setText(Strings.get("projectEditCircuitAppearanceItem"));
		revertAppearance.setText(Strings.get("projectRevertAppearanceItem"));
		analyze.setText(Strings.get("projectAnalyzeCircuitItem"));
		stats.setText(Strings.get("projectGetCircuitStatisticsItem"));
		options.setText(Strings.get("projectOptionsItem"));
		showProjectToolbar.setText(Strings.get("projectShowToolbarItem"));
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
				|| appearance.hasListeners()
				|| revertAppearance.hasListeners()
				|| analyze.hasListeners()
				|| stats.hasListeners());
	}
}
