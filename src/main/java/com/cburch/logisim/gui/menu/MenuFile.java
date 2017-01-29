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

@SuppressWarnings("serial")
class MenuFile extends Menu implements ActionListener {
    private LogisimMenuBar menubar;
    private JMenuItem newi = new JMenuItem();
    private JMenuItem open = new JMenuItem();
    private JMenuItem protoboard = new JMenuItem();
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
        protoboard.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, menuMask));
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
        add(protoboard);
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
        protoboard.addActionListener(this);
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
        this.setText(getFromLocale("fileMenu"));
        newi.setText(getFromLocale("fileNewItem"));
        protoboard.setText(getFromLocale("fileNewProtoboard"));
        open.setText(getFromLocale("fileOpenItem"));
        openRecent.localeChanged();
        close.setText(getFromLocale("fileCloseItem"));
        save.setText(getFromLocale("fileSaveItem"));
        saveAs.setText(getFromLocale("fileSaveAsItem"));
        exportImage.setText(getFromLocale("fileExportImageItem"));
        print.setText(getFromLocale("filePrintItem"));
        prefs.setText(getFromLocale("filePreferencesItem"));
        quit.setText(getFromLocale("fileQuitItem"));
    }

    @Override
    void computeEnabled() {
        setEnabled(true);
        menubar.fireEnableChanged();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        Project proj = menubar.getProject();
        if (src == newi) {
            ProjectActions.doNew(proj);
        } else if (src == open) {
            ProjectActions.doOpen(proj == null ? null : proj.getFrame().getCanvas(), proj);
        } else if (src == protoboard) {
        	ProjectActions.setTemplate();
        	ProjectActions.doNew(proj);
        	ProjectActions.resetTemplate();
        } 
        else if (src == close) {
            Frame frame = proj.getFrame();
            if (frame.confirmClose()) {
                frame.dispose();
                OptionsFrame f = proj.getOptionsFrame(false);
                if (f != null) {
                    f.dispose();
                }

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
