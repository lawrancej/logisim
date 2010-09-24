/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public abstract class EditPopup extends JPopupMenu {
	private class Listener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			for (Map.Entry<LogisimMenuItem, JMenuItem> entry : items.entrySet()) {
				if (entry.getValue() == source) {
					fire(entry.getKey());
					return;
				}
			}
		}
	}
	
	private Listener listener;
	private Map<LogisimMenuItem, JMenuItem> items;
	
	public EditPopup() {
		this(false);
	}
	
	public EditPopup(boolean waitForInitialize) {
		listener = new Listener();
		items = new HashMap<LogisimMenuItem, JMenuItem>();
		if (!waitForInitialize) initialize();
	}
	
	protected void initialize() {
		boolean x = false;
		x |= add(LogisimMenuBar.CUT, Strings.get("editCutItem"));
		x |= add(LogisimMenuBar.COPY, Strings.get("editCopyItem"));
		if (x) { addSeparator(); x = false; }
		x |= add(LogisimMenuBar.DELETE, Strings.get("editClearItem"));
		x |= add(LogisimMenuBar.DUPLICATE, Strings.get("editDuplicateItem"));
		if (x) { addSeparator(); x = false; }
		x |= add(LogisimMenuBar.RAISE, Strings.get("editRaiseItem"));
		x |= add(LogisimMenuBar.LOWER, Strings.get("editLowerItem"));
		x |= add(LogisimMenuBar.RAISE_TOP, Strings.get("editRaiseTopItem"));
		x |= add(LogisimMenuBar.LOWER_BOTTOM, Strings.get("editLowerBottomItem"));
		if (x) { addSeparator(); x = false; }
		x |= add(LogisimMenuBar.ADD_CONTROL, Strings.get("editAddControlItem"));
		x |= add(LogisimMenuBar.REMOVE_CONTROL, Strings.get("editRemoveControlItem"));
		if (!x && getComponentCount() > 0) { remove(getComponentCount() - 1); }
	}
	
	private boolean add(LogisimMenuItem item, String display) {
		if (shouldShow(item)) {
			JMenuItem menu = new JMenuItem(display);
			items.put(item, menu);
			menu.setEnabled(isEnabled(item));
			menu.addActionListener(listener);
			add(menu);
			return true;
		} else {
			return false;
		}
	}
	
	protected abstract boolean shouldShow(LogisimMenuItem item);
	
	protected abstract boolean isEnabled(LogisimMenuItem item);
	
	protected abstract void fire(LogisimMenuItem item);
}
