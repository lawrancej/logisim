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
    private MenuItem cut    = new MenuItem(this, LogisimMenuBar.CUT);
    private MenuItem copy   = new MenuItem(this, LogisimMenuBar.COPY);
    private MenuItem paste  = new MenuItem(this, LogisimMenuBar.PASTE);
    private MenuItem clear  = new MenuItem(this, LogisimMenuBar.DELETE);
    private MenuItem dup    = new MenuItem(this, LogisimMenuBar.DUPLICATE);
    private MenuItem selall = new MenuItem(this, LogisimMenuBar.SELECT_ALL);
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
        clear.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_DELETE, 0));
        dup.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_D, menuMask));
        selall.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_A, menuMask));

        add(undo);
        addSeparator();
        add(cut);
        add(copy);
        add(paste);
        add(clear);
        add(dup);
        add(selall);
        
        Project proj = menubar.getProject();
        if (proj != null) {
            proj.addProjectListener(myListener);
            undo.addActionListener(myListener);
        }

        undo.setEnabled(false);
        menubar.registerItem(LogisimMenuBar.CUT, cut);
        menubar.registerItem(LogisimMenuBar.COPY, copy);
        menubar.registerItem(LogisimMenuBar.PASTE, paste);
        menubar.registerItem(LogisimMenuBar.DELETE, clear);
        menubar.registerItem(LogisimMenuBar.DUPLICATE, dup);
        menubar.registerItem(LogisimMenuBar.SELECT_ALL, selall);
        computeEnabled();
    }

    public void localeChanged() {
        this.setText(Strings.get("editMenu"));
        myListener.projectChanged(null);
        cut.setText(Strings.get("editCutItem"));
        copy.setText(Strings.get("editCopyItem"));
        paste.setText(Strings.get("editPasteItem"));
        clear.setText(Strings.get("editClearItem"));
        dup.setText(Strings.get("editDuplicateItem"));
        selall.setText(Strings.get("editSelectAllItem"));
    }
    
    @Override
    void computeEnabled() {
        setEnabled(menubar.getProject() != null
                || cut.hasListeners()
                || copy.hasListeners()
                || paste.hasListeners()
                || clear.hasListeners()
                || dup.hasListeners()
                || selall.hasListeners());
    }
}

