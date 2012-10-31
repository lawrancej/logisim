/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.util.Arrays;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.list.UnmodifiableList;

import com.cburch.draw.toolbar.AbstractToolbarModel;
import com.cburch.draw.toolbar.ToolbarItem;
import com.cburch.logisim.circuit.Simulator;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.proj.Project;
import static com.cburch.logisim.util.LocaleString.*;

class SimulationToolbarModel extends AbstractToolbarModel
		implements ChangeListener {
	private Project project;
	private LogisimToolbarItem simEnable;
	private LogisimToolbarItem simStep;
	private LogisimToolbarItem tickEnable;
	private LogisimToolbarItem tickStep;
	private List<ToolbarItem> items;
	
	public SimulationToolbarModel(Project project, MenuListener menu) {
		this.project = project;
		
		simEnable = new LogisimToolbarItem(menu, "simplay.png", LogisimMenuBar.SIMULATE_ENABLE,
				__("simulateEnableStepsTip"));
		simStep = new LogisimToolbarItem(menu, "simstep.png", LogisimMenuBar.SIMULATE_STEP,
				__("simulateStepTip"));
		tickEnable = new LogisimToolbarItem(menu, "simtplay.png", LogisimMenuBar.TICK_ENABLE,
				__("simulateEnableTicksTip"));
		tickStep = new LogisimToolbarItem(menu, "simtstep.png", LogisimMenuBar.TICK_STEP,
				__("simulateTickTip"));
		
		items = UnmodifiableList.decorate(Arrays.asList(new ToolbarItem[] {
				simEnable,
				simStep,
				tickEnable,
				tickStep,
			}));
		
		menu.getMenuBar().addEnableListener(this);
		stateChanged(null);
	}

	@Override
	public List<ToolbarItem> getItems() {
		return items;
	}
	
	@Override
	public boolean isSelected(ToolbarItem item) {
		return false;
	}

	@Override
	public void itemSelected(ToolbarItem item) {
		if (item instanceof LogisimToolbarItem) {
			((LogisimToolbarItem) item).doAction();
		}
	}

	//
	// ChangeListener methods
	//
	public void stateChanged(ChangeEvent e) {
		Simulator sim = project.getSimulator(); 
		boolean running = sim != null && sim.isRunning();
		boolean ticking = sim != null && sim.isTicking();
		simEnable.setIcon(running ? "simstop.png" : "simplay.png");
		simEnable.setToolTip(running ? __("simulateDisableStepsTip")
				: __("simulateEnableStepsTip"));
		tickEnable.setIcon(ticking ? "simtstop.png" : "simtplay.png");
		tickEnable.setToolTip(ticking ? __("simulateDisableTicksTip")
				: __("simulateEnableTicksTip"));
		fireToolbarAppearanceChanged();
	}
}
