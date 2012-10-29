/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.gui.opts.OptionsFrame;
import com.cburch.logisim.gui.prefs.PreferencesFrame;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectActions;
import com.cburch.logisim.util.MacCompatibility;
import static com.cburch.logisim.util.LocaleString.*;

class MenuFile extends Menu implements ActionListener {
	private LogisimMenuBar menubar;
	private JMenuItem newi = new JMenuItem();
	private JMenuItem open = new JMenuItem();
	private OpenRecent openRecent;
	private JMenuItem close = new JMenuItem();
	private JMenuItem save = new JMenuItem();
	private JMenuItem saveAs = new JMenuItem();
	private MenuItemImpl print = new MenuItemImpl(this, LogisimMenuBar.PRINT);
	private MenuItemImpl exportImage = new MenuItemImpl(this, LogisimMenuBar.EXPORT_IMAGE);
	private JMenuItem prefs = new JMenuItem();
	private JMenuItem quit = new JMenuItem();

	public MenuFile(LogisimMenuBar menubar) {
		this.menubar = menubar;
		openRecent = new OpenRecent(menubar);
		
		int menuMask = getToolkit().getMenuShortcutKeyMask();

		newi.setAccelerator(KeyStroke.getKeyStroke(
			KeyEvent.VK_N, menuMask));
		open.setAccelerator(KeyStroke.getKeyStroke(
			KeyEvent.VK_O, menuMask));
		close.setAccelerator(KeyStroke.getKeyStroke(
			KeyEvent.VK_W, menuMask | InputEvent.SHIFT_MASK));
		save.setAccelerator(KeyStroke.getKeyStroke(
			KeyEvent.VK_S, menuMask));
		saveAs.setAccelerator(KeyStroke.getKeyStroke(
			KeyEvent.VK_S, menuMask | InputEvent.SHIFT_MASK));
		print.setAccelerator(KeyStroke.getKeyStroke(
			KeyEvent.VK_P, menuMask));
		quit.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Q, menuMask));

		add(newi);
		add(open);
		add(openRecent);
		addSeparator();
		add(close);
		add(save);
		add(saveAs);
		addSeparator();
		add(exportImage);
		add(print);
		if (!MacCompatibility.isPreferencesAutomaticallyPresent()) {
			addSeparator();
			add(prefs);
		}
		if (!MacCompatibility.isQuitAutomaticallyPresent()) {
			addSeparator();
			add(quit);
		}

		Project proj = menubar.getProject();
		newi.addActionListener(this);
		open.addActionListener(this);
		if (proj == null) {
			close.setEnabled(false);
			save.setEnabled(false);
			saveAs.setEnabled(false);
		} else {
			close.addActionListener(this);
			save.addActionListener(this);
			saveAs.addActionListener(this);
		}
		menubar.registerItem(LogisimMenuBar.EXPORT_IMAGE, exportImage);
		menubar.registerItem(LogisimMenuBar.PRINT, print);
		prefs.addActionListener(this);
		quit.addActionListener(this);
	}

	public void localeChanged() {
		this.setText(_("fileMenu"));
		newi.setText(_("fileNewItem"));
		open.setText(_("fileOpenItem"));
		openRecent.localeChanged();
		close.setText(_("fileCloseItem"));
		save.setText(_("fileSaveItem"));
		saveAs.setText(_("fileSaveAsItem"));
		exportImage.setText(_("fileExportImageItem"));
		print.setText(_("filePrintItem"));
		prefs.setText(_("filePreferencesItem"));
		quit.setText(_("fileQuitItem"));
	}

	@Override
	void computeEnabled() {
		setEnabled(true);
		menubar.fireEnableChanged();
	}
	
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		Project proj = menubar.getProject();
		if (src == newi) {
			ProjectActions.doNew(proj);
		} else if (src == open) {
			ProjectActions.doOpen(proj == null ? null : proj.getFrame().getCanvas(), proj);
		} else if (src == close) {
			Frame frame = proj.getFrame();
			if (frame.confirmClose()) {
				frame.dispose();
				OptionsFrame f = proj.getOptionsFrame(false);
				if (f != null) f.dispose();
			}
		} else if (src == save) {
			ProjectActions.doSave(proj);
		} else if (src == saveAs) {
			ProjectActions.doSaveAs(proj);
		} else if (src == prefs) {
			PreferencesFrame.showPreferences();
		} else if (src == quit) {
			ProjectActions.doQuit();
		}
	}
}
