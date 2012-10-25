/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;

import com.cburch.logisim.prefs.PrefMonitor;
import com.cburch.logisim.util.StringGetter;

class PrefBoolean extends JCheckBox
		implements ActionListener, PropertyChangeListener {
	private PrefMonitor<Boolean> pref;
	private StringGetter title;
	
	PrefBoolean(PrefMonitor<Boolean> pref, StringGetter title) {
		super(title.toString());
		this.pref = pref;
		this.title = title;
		
		addActionListener(this);
		pref.addPropertyChangeListener(this);
		setSelected(pref.getBoolean());
	}
	
	void localeChanged() {
		setText(title.toString());
	}
	
	public void actionPerformed(ActionEvent e) {
		pref.setBoolean(this.isSelected());
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (pref.isSource(event)) {
			setSelected(pref.getBoolean());
		}
	}
}
