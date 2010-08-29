/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.proj.LogisimPreferences;
import com.cburch.logisim.util.StringGetter;

class ProjectToolbar extends JComponent
        implements MouseListener, MouseMotionListener, PropertyChangeListener {
    private static final String path = "resources/logisim/projbar.png";
    private static Icon image;
    private static final int NUM_ICONS = 5;
    private static final int ICON_WIDTH = 20;
    private static final int ICON_HEIGHT = 20;
    private static final Object[] items = {
        LogisimMenuBar.ADD_CIRCUIT,
        LogisimMenuBar.MOVE_CIRCUIT_UP,
        LogisimMenuBar.MOVE_CIRCUIT_DOWN,
        LogisimMenuBar.REMOVE_CIRCUIT,
    };
    private static final StringGetter[] tips = {
        Strings.getter("projectAddCircuitTip"),
        Strings.getter("projectMoveCircuitUpTip"),
        Strings.getter("projectMoveCircuitDownTip"),
        Strings.getter("projectRemoveCircuitTip"),
    };
    
    static {
        java.net.URL url = ProjectToolbar.class.getClassLoader().getResource(path);
        if (url != null) image = new ImageIcon(url);
    }
    
    private boolean[] enabled = new boolean[NUM_ICONS];
    private int mousePressed = -1;
    private int mouseOn = -1;
    private ActionListener listener;

    ProjectToolbar() {
        if (image != null) {
            setPreferredSize(new Dimension(image.getIconWidth(), image.getIconHeight()));
        }
        addMouseListener(this);
        addMouseMotionListener(this);
        setToolTipText("");
        
        LogisimPreferences.addPropertyChangeListener(LogisimPreferences.SHOW_PROJECT_TOOLBAR, this);
        this.setVisible(LogisimPreferences.getShowProjectToolbar());
    }
    
    public void setActionListener(ActionListener value) {
        listener = value;
        Arrays.fill(enabled, value != null);
    }
    
    public void setEnabled(Object item, boolean value) {
        for (int i = 0; i < items.length; i++) {
            if (item == items[i]) {
                if (enabled[i] != value) {
                    enabled[i] = value;
                    repaint();
                }
                return;
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        if (image != null) image.paintIcon(this, g, 0, 0);
        Color bg = getBackground();
        g.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 128));
        for (int i = 0; i < enabled.length; i++) {
            if (!enabled[i] || i == mouseOn) {
                g.fillRect(i * ICON_WIDTH, 0, ICON_WIDTH, ICON_HEIGHT);
            }
        }
    }
    
    @Override
    public String getToolTipText(MouseEvent e) {
        int index = computeIndex(e);
        if (index < 0) {
            return null;
        } else {
            return tips[index].get();
        }
    }

    public void mouseClicked(MouseEvent e) { }

    public void mouseEntered(MouseEvent e) { }

    public void mouseExited(MouseEvent e) { }

    public void mousePressed(MouseEvent e) {
        int i = computeIndex(e);
        if (i >= 0 && i < enabled.length && enabled[i]) {
            mousePressed = i;
            mouseOn = i;
            repaint();
        }
    }

    public void mouseReleased(MouseEvent e) {
        mouseDragged(e);
        int i = mouseOn;
        if (i >= 0 && listener != null) {
            listener.actionPerformed(new ActionEvent(items[i],
                    ActionEvent.ACTION_PERFORMED, items[i].toString()));
        }
        mouseOn = -1;
        mousePressed = -1;
        repaint();
    }
    
    public void mouseMoved(MouseEvent e) { }
    
    public void mouseDragged(MouseEvent e) {
        int i = computeIndex(e);
        if (i != mousePressed) i = -1;
        if (mouseOn != i) {
            mouseOn = i;
            repaint();
        }
    }
    
    private int computeIndex(MouseEvent e) {
        int y = e.getY();
        if (y < 0 || y >= ICON_HEIGHT) return -1;
        int x = e.getX();
        if (x < 0 || x >= items.length * ICON_WIDTH) return -1;
        return x / ICON_WIDTH;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if (prop.equals(LogisimPreferences.SHOW_PROJECT_TOOLBAR)) {
            this.setVisible(LogisimPreferences.getShowProjectToolbar());
        }
    }
}
