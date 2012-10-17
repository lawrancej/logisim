/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;

class MenuItemHelper implements ActionListener {
	private JMenuItem source;
	private LogisimMenuItem menuItem;
	private Menu menu;
	private boolean enabled;
	private boolean inActionListener;
	private ArrayList<ActionListener> listeners;
	
	public MenuItemHelper(JMenuItem source, Menu menu, LogisimMenuItem menuItem) {
		this.source = source;
		this.menu = menu;
		this.menuItem = menuItem;
		this.enabled = true;
		this.inActionListener = false;
		this.listeners = new ArrayList<ActionListener>();
	}
	
	public boolean hasListeners() {
		return !listeners.isEmpty();
	}
	
	public void addActionListener(ActionListener l) {
		listeners.add(l);
		computeEnabled();
	}
	
	public void removeActionListener(ActionListener l) {
		listeners.remove(l);
		computeEnabled();
	}
	
	public void setEnabled(boolean value) {
		if (!inActionListener) {
			enabled = value;
		}
	}
	
	private void computeEnabled() {
		inActionListener = true;
		try {
			source.setEnabled(enabled);
			menu.computeEnabled();
		} finally {
			inActionListener = false;
		}
	}

	public void actionPerformed(ActionEvent event) {
		if (!listeners.isEmpty()) {
			ActionEvent e = new ActionEvent(menuItem, event.getID(),
					event.getActionCommand(), event.getWhen(),
					event.getModifiers());
			for (ActionListener l : listeners) {
				l.actionPerformed(e);
			}
		}
	}
}
