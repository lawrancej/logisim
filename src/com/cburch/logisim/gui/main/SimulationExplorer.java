/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.cburch.draw.toolbar.Toolbar;
import com.cburch.logisim.proj.Project;

class SimulationExplorer extends JPanel implements ExplorerPane.View {
	private ProjectExplorer toolbox;
	
	SimulationExplorer(Project proj, MenuListener menu) {
		super(new BorderLayout());
		
		ToolboxToolbarModel toolbarModel = new ToolboxToolbarModel(menu);
		Toolbar toolbar = new Toolbar(toolbarModel);
		add(toolbar, BorderLayout.NORTH);
	}
}
