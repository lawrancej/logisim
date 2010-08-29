/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;

class MenuItem extends JMenuItem implements ActionListener {
    private LogisimMenuItem menuItem;
    private Menu menu;
    private boolean enabled;
    private ArrayList<ActionListener> listeners;
    
    public MenuItem(Menu menu, LogisimMenuItem menuItem) {
        this.menu = menu;
        this.menuItem = menuItem;
        this.enabled = true;
        this.listeners = new ArrayList<ActionListener>();
        super.addActionListener(this);
        computeEnabled();
    }
    
    boolean hasListeners() {
        return !listeners.isEmpty();
    }
    
    @Override
    public void addActionListener(ActionListener l) {
        listeners.add(l);
        computeEnabled();
        menu.computeEnabled();
    }
    
    @Override
    public void removeActionListener(ActionListener l) {
        listeners.remove(l);
        computeEnabled();
        menu.computeEnabled();
    }
    
    @Override
    public void setEnabled(boolean value) {
        enabled = value;
        computeEnabled();
    }
    
    private void computeEnabled() {
        super.setEnabled(enabled && hasListeners());
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
