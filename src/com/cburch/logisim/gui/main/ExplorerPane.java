/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.CardLayout;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;

class ExplorerPane extends JPanel {
	static interface View {
		
	}
	
	private ArrayList<JComponent> views;
	private JComponent currentView; // will also implement View
	
	public ExplorerPane() {
		super(new CardLayout());
		views = new ArrayList<JComponent>();
		currentView = null;
	}
	
	public void setView(JComponent value) {
		if (!(value instanceof View)) {
			throw new IllegalArgumentException("must implement View interface");
		}
		
		JComponent oldView = currentView;
		if (oldView != value) {
			CardLayout layout = (CardLayout) getLayout();
			int index = views.indexOf(value);
			if (index < 0) {
				index = views.size();
				views.add(value);
				this.add(value, "" + index);
			}
			layout.show(this, "" + index);
			currentView = value;
		}	
	}
}
