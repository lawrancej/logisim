/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import com.cburch.logisim.util.StringUtil;

class MenuEdit extends Menu {
	private class MyListener implements ProjectListener, ActionListener {
		public void projectChanged(ProjectEvent e) {
			Project proj = menubar.getProject();
			Action last = proj == null ? null : proj.getLastAction();
			if (last == null) {
				undo.setText(Strings.get("editCantUndoItem"));
				undo.setEnabled(false);
			} else {
				undo.setText(StringUtil.format(Strings.get("editUndoItem"),
					last.getName()));
				undo.setEnabled(true);
			}
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			Project proj = menubar.getProject();
			if (src == undo) {
				if (proj != null) proj.undoAction();
			}
		}
	}

	private LogisimMenuBar menubar;
	private JMenuItem undo  = new JMenuItem();
	private MenuItemImpl cut    = new MenuItemImpl(this, LogisimMenuBar.CUT);
	private MenuItemImpl copy   = new MenuItemImpl(this, LogisimMenuBar.COPY);
	private MenuItemImpl paste  = new MenuItemImpl(this, LogisimMenuBar.PASTE);
	private MenuItemImpl delete = new MenuItemImpl(this, LogisimMenuBar.DELETE);
	private MenuItemImpl dup    = new MenuItemImpl(this, LogisimMenuBar.DUPLICATE);
	private MenuItemImpl selall = new MenuItemImpl(this, LogisimMenuBar.SELECT_ALL);
	private MenuItemImpl raise = new MenuItemImpl(this, LogisimMenuBar.RAISE);
	private MenuItemImpl lower = new MenuItemImpl(this, LogisimMenuBar.LOWER);
	private MenuItemImpl raiseTop = new MenuItemImpl(this, LogisimMenuBar.RAISE_TOP);
	private MenuItemImpl lowerBottom = new MenuItemImpl(this, LogisimMenuBar.LOWER_BOTTOM);
	private MenuItemImpl addCtrl = new MenuItemImpl(this, LogisimMenuBar.ADD_CONTROL);
	private MenuItemImpl remCtrl = new MenuItemImpl(this, LogisimMenuBar.REMOVE_CONTROL);
	private MyListener myListener = new MyListener();

	public MenuEdit(LogisimMenuBar menubar) {
		this.menubar = menubar;

		int menuMask = getToolkit().getMenuShortcutKeyMask();
		undo.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Z, menuMask));
		cut.setAccelerator(KeyStroke.getKeyStroke(
			KeyEvent.VK_X, menuMask));
		copy.setAccelerator(KeyStroke.getKeyStroke(
			KeyEvent.VK_C, menuMask));
		paste.setAccelerator(KeyStroke.getKeyStroke(
			KeyEvent.VK_V, menuMask));
		delete.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_DELETE, 0));
		dup.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_D, menuMask));
		selall.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_A, menuMask));
		raise.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_UP, menuMask));
		lower.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_DOWN, menuMask));
		raiseTop.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_UP, menuMask | KeyEvent.SHIFT_DOWN_MASK));
		lowerBottom.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_DOWN, menuMask | KeyEvent.SHIFT_DOWN_MASK));

		add(undo);
		addSeparator();
		add(cut);
		add(copy);
		add(paste);
		addSeparator();
		add(delete);
		add(dup);
		add(selall);
		addSeparator();
		add(raise);
		add(lower);
		add(raiseTop);
		add(lowerBottom);
		addSeparator();
		add(addCtrl);
		add(remCtrl);
		
		Project proj = menubar.getProject();
		if (proj != null) {
			proj.addProjectListener(myListener);
			undo.addActionListener(myListener);
		}

		undo.setEnabled(false);
		menubar.registerItem(LogisimMenuBar.CUT, cut);
		menubar.registerItem(LogisimMenuBar.COPY, copy);
		menubar.registerItem(LogisimMenuBar.PASTE, paste);
		menubar.registerItem(LogisimMenuBar.DELETE, delete);
		menubar.registerItem(LogisimMenuBar.DUPLICATE, dup);
		menubar.registerItem(LogisimMenuBar.SELECT_ALL, selall);
		menubar.registerItem(LogisimMenuBar.RAISE, raise);
		menubar.registerItem(LogisimMenuBar.LOWER, lower);
		menubar.registerItem(LogisimMenuBar.RAISE_TOP, raiseTop);
		menubar.registerItem(LogisimMenuBar.LOWER_BOTTOM, lowerBottom);
		menubar.registerItem(LogisimMenuBar.ADD_CONTROL, addCtrl);
		menubar.registerItem(LogisimMenuBar.REMOVE_CONTROL, remCtrl);
		computeEnabled();
	}

	public void localeChanged() {
		this.setText(Strings.get("editMenu"));
		myListener.projectChanged(null);
		cut.setText(Strings.get("editCutItem"));
		copy.setText(Strings.get("editCopyItem"));
		paste.setText(Strings.get("editPasteItem"));
		delete.setText(Strings.get("editClearItem"));
		dup.setText(Strings.get("editDuplicateItem"));
		selall.setText(Strings.get("editSelectAllItem"));
		raise.setText(Strings.get("editRaiseItem"));
		lower.setText(Strings.get("editLowerItem"));
		raiseTop.setText(Strings.get("editRaiseTopItem"));
		lowerBottom.setText(Strings.get("editLowerBottomItem"));
		addCtrl.setText(Strings.get("editAddControlItem"));
		remCtrl.setText(Strings.get("editRemoveControlItem"));
	}
	
	@Override
	void computeEnabled() {
		setEnabled(menubar.getProject() != null
				|| cut.hasListeners()
				|| copy.hasListeners()
				|| paste.hasListeners()
				|| delete.hasListeners()
				|| dup.hasListeners()
				|| selall.hasListeners()
				|| raise.hasListeners()
				|| lower.hasListeners()
				|| raiseTop.hasListeners()
				|| lowerBottom.hasListeners()
				|| addCtrl.hasListeners()
				|| remCtrl.hasListeners());
	}
}

