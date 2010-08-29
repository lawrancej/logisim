/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.opts;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.proj.Project;

abstract class OptionsPanel extends JPanel {
	private OptionsFrame optionsFrame;
	
	public OptionsPanel(OptionsFrame frame) {
		super();
		this.optionsFrame = frame;
	}
	
	public OptionsPanel(OptionsFrame frame, LayoutManager manager) {
		super(manager);
		this.optionsFrame = frame;
	}
	
	public abstract String getTitle();
	public abstract String getHelpText();
	public abstract void localeChanged();
	
	OptionsFrame getOptionsFrame() {
		return optionsFrame;
	}
	
	Project getProject() {
		return optionsFrame.getProject();
	}
	
	LogisimFile getLogisimFile() {
		return optionsFrame.getLogisimFile();
	}
	
	Options getOptions() {
		return optionsFrame.getOptions();
	}
}
