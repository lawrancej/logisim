/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.gui;

import javax.swing.JFrame;

import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.WindowMenuItemManager;
import static com.cburch.logisim.util.LocaleString._;

public class AnalyzerManager extends WindowMenuItemManager
		implements LocaleListener {
	public static void initialize() {
		analysisManager = new AnalyzerManager();
	}
	
	public static Analyzer getAnalyzer() {
		if (analysisWindow == null) {
			analysisWindow = new Analyzer();
			analysisWindow.pack();
			if (analysisManager != null) analysisManager.frameOpened(analysisWindow);
		}
		return analysisWindow;
	}
	
	private static Analyzer analysisWindow = null;
	private static AnalyzerManager analysisManager = null;

	private AnalyzerManager() {
		super(_("analyzerWindowTitle"), true);
		LocaleManager.addLocaleListener(this);
	}
	
	@Override
	public JFrame getJFrame(boolean create) {
		if (create) {
			return getAnalyzer();
		} else {
			return analysisWindow;
		}
	}

	public void localeChanged() {
		setText(_("analyzerWindowTitle"));
	}
}
