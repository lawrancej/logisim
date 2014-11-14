/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import static com.cburch.logisim.util.LocaleString.*;

@SuppressWarnings("serial")
class MenuEdit extends Menu {
    private class MyListener implements ProjectListener, ActionListener {
        @Override
        public void projectChanged(ProjectEvent e) {
            Project proj = menubar.getProject();
            Action last = proj == null ? null : proj.getLastAction();
			if( last == null )
			{
				undo.setText( getFromLocale( "editCantUndoItem" ) );
				undo.setEnabled( false );
			}
			else
			{
				undo.setText( getFromLocale( "editUndoItem", last.getName() ) );
				undo.setEnabled( true );
            }

			// If there is a project open...
			if( proj != null )
				// And you CAN redo an undo...
				if( proj.getCanRedo() )
				{
					// Get that action
					Action lastRedo = proj.getLastRedoAction();

					// Set the detailed, localized text

					redo.setText( getFromLocale( "editRedoItem", lastRedo.getName() ) );
					
					// Set it to enabled
					redo.setEnabled( true );
				}
				else
				{	// If there is no project...
					// Let them know they can't redo anything
					redo.setText( getFromLocale( "editCantRedoItem" ) );

					// And disable the button
					redo.setEnabled( false );
				}
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            Project proj = menubar.getProject();
			if( src == undo )
			{
				if( proj != null )
					proj.undoAction();
			}
			else if( src == redo )
			{
				if (proj != null )
					proj.redoAction();
            }
        }
    }

    private LogisimMenuBar menubar;
    private JMenuItem undo  = new JMenuItem();
	private JMenuItem redo  = new JMenuItem();
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
		redo.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Y, menuMask));
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
                KeyEvent.VK_UP, menuMask | InputEvent.SHIFT_DOWN_MASK));
        lowerBottom.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_DOWN, menuMask | InputEvent.SHIFT_DOWN_MASK));

        add(undo);
		add(redo);
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
			redo.addActionListener( myListener );
        }

        undo.setEnabled(false);
		redo.setEnabled(false);
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
        this.setText(getFromLocale("editMenu"));
        myListener.projectChanged(null);
        cut.setText(getFromLocale("editCutItem"));
        copy.setText(getFromLocale("editCopyItem"));
        paste.setText(getFromLocale("editPasteItem"));
        delete.setText(getFromLocale("editClearItem"));
        dup.setText(getFromLocale("editDuplicateItem"));
        selall.setText(getFromLocale("editSelectAllItem"));
        raise.setText(getFromLocale("editRaiseItem"));
        lower.setText(getFromLocale("editLowerItem"));
        raiseTop.setText(getFromLocale("editRaiseTopItem"));
        lowerBottom.setText(getFromLocale("editLowerBottomItem"));
        addCtrl.setText(getFromLocale("editAddControlItem"));
        remCtrl.setText(getFromLocale("editRemoveControlItem"));
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

