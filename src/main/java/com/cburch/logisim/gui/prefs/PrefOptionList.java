/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.cburch.logisim.prefs.PrefMonitor;
import com.cburch.logisim.util.StringGetter;

class PrefOptionList implements ActionListener, PropertyChangeListener {
	private PrefMonitor<String> pref;
	private StringGetter labelStr;
	
	private JLabel label;
	private JComboBox combo;
	
	public PrefOptionList(PrefMonitor<String> pref, StringGetter labelStr,
			PrefOption[] options) {
		this.pref = pref;
		this.labelStr = labelStr;
		
		label = new JLabel(labelStr.toString() + " ");
		combo = new JComboBox();
		for (PrefOption opt : options) {
			combo.addItem(opt);
		}
		
		combo.addActionListener(this);
		pref.addPropertyChangeListener(this);
		selectOption(pref.get());
	}
	
	JPanel createJPanel() {
		JPanel ret = new JPanel();
		ret.add(label);
		ret.add(combo);
		return ret;
	}
	
	JLabel getJLabel() {
		return label;
	}
	
	JComboBox getJComboBox() {
		return combo;
	}
	
	void localeChanged() {
		label.setText(labelStr.toString() + " ");
	}

	public void actionPerformed(ActionEvent e) {
		PrefOption x = (PrefOption) combo.getSelectedItem();
		pref.set((String) x.getValue());
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (pref.isSource(event)) {
			selectOption(pref.get());
		}
	}
	
	private void selectOption(Object value) {
		for (int i = combo.getItemCount() - 1; i >= 0; i--) {
			PrefOption opt = (PrefOption) combo.getItemAt(i);
			if (opt.getValue().equals(value)) {
				combo.setSelectedItem(opt);
				return;
			}
		}
		combo.setSelectedItem(combo.getItemAt(0));
	}
}
