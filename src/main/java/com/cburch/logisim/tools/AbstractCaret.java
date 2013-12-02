/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import com.cburch.logisim.data.Bounds;

public class AbstractCaret implements Caret {
    private ArrayList<CaretListener> listeners = new ArrayList<CaretListener>();
    private List<CaretListener> listenersView;
    private Bounds bds = Bounds.EMPTY_BOUNDS;

    public AbstractCaret() {
        listenersView = Collections.unmodifiableList(listeners);
    }

    // listener methods
    @Override
    public void addCaretListener(CaretListener e) { listeners.add(e); }
    @Override
    public void removeCaretListener(CaretListener e) { listeners.remove(e); }
    protected List<CaretListener> getCaretListeners() { return listenersView; }

    // configuration methods
    public void setBounds(Bounds value) { bds = value; }

    // query/Graphics methods
    @Override
    public String getText() { return ""; }
    @Override
    public Bounds getBounds(Graphics g) { return bds; }
    @Override
    public void draw(Graphics g) { }

    // finishing
    @Override
    public void commitText(String text) { }
    @Override
    public void cancelEditing() { }
    @Override
    public void stopEditing() { }

    // events to handle
    @Override
    public void mousePressed(MouseEvent e) { }
    @Override
    public void mouseDragged(MouseEvent e) { }
    @Override
    public void mouseReleased(MouseEvent e) { }
    @Override
    public void keyPressed(KeyEvent e) { }
    @Override
    public void keyReleased(KeyEvent e) { }
    @Override
    public void keyTyped(KeyEvent e) { }
}
