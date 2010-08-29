/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

import java.awt.LayoutManager;

import javax.swing.JPanel;

abstract class OptionsPanel extends JPanel{
	private PreferencesFrame optionsFrame;
	
	public OptionsPanel(PreferencesFrame frame) {
		super();
		this.optionsFrame = frame;
	}
	
	public OptionsPanel(PreferencesFrame frame, LayoutManager manager) {
		super(manager);
		this.optionsFrame = frame;
	}
	
	public abstract String getTitle();
	public abstract String getHelpText();
	public abstract void localeChanged();
	
	PreferencesFrame getPreferencesFrame() {
		return optionsFrame;
	}
}
