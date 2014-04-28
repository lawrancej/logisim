/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.canvas;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import com.cburch.draw.model.CanvasModel;
import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.undo.Action;


@SuppressWarnings("serial")
public class Canvas extends JComponent {
    public static final String TOOL_PROPERTY = "tool";
    public static final String MODEL_PROPERTY = "model";

    private CanvasModel model;
    private ActionDispatcher dispatcher;
    private CanvasListener listener;
    private Selection selection;

    public Canvas() {
        model = null;
        listener = new CanvasListener(this);
        selection = new Selection();

        addMouseListener(listener);
        addMouseMotionListener(listener);
        addKeyListener(listener);
        setPreferredSize(new Dimension(200, 200));
    }

    public CanvasModel getModel() {
        return model;
    }

    public CanvasTool getTool() {
        return listener.getTool();
    }

    public void toolGestureComplete(CanvasTool tool, CanvasObject created) {
        // nothing to do - subclass may override
    }

    protected JPopupMenu showPopupMenu(MouseEvent e, CanvasObject clicked) {
        // subclass will override if it supports popup menus
        return null;
    }

    public Selection getSelection() {
        return selection;
    }

    protected void setSelection(Selection value) {
        selection = value;
        repaint();
    }

    public void doAction(Action action) {
        dispatcher.doAction(action);
    }

    public void setModel(CanvasModel value, ActionDispatcher dispatcher) {
        CanvasModel oldValue = model;
        if (!oldValue.equals(value)) {
            if (oldValue != null) {
                oldValue.removeCanvasModelListener(listener);
            }

            model = value;
            this.dispatcher = dispatcher;
            if (value != null) {
                value.addCanvasModelListener(listener);
            }

            selection.clearSelected();
            repaint();
            firePropertyChange(MODEL_PROPERTY, oldValue, value);
        }
    }

    public void setTool(CanvasTool value) {
        CanvasTool oldValue = listener.getTool();
        if (!value.equals(oldValue)) {
            listener.setTool(value);
            firePropertyChange(TOOL_PROPERTY, oldValue, value);
        }
    }

    public void repaintCanvasCoords(int x, int y, int width, int height) {
        repaint(x, y, width, height);
    }

    public double getZoomFactor() {
        // subclass will have to override this
        return 1.0;
    }

    public int snapX(int x) {
        // subclass will have to override this
        return x;
    }

    public int snapY(int y) {
        // subclass will have to override this
        return y;
    }

    @Override
    public void paintComponent(Graphics g) {
        paintBackground(g);
        paintForeground(g);
    }

    protected void paintBackground(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());
    }

    protected void paintForeground(Graphics g) {
        CanvasModel cModel = this.model;
        CanvasTool tool = listener.getTool();
        if (cModel != null) {
            Graphics dup = g.create();
            cModel.paint(g, selection);
            dup.dispose();
        }
        if (tool != null) {
            Graphics dup = g.create();
            tool.draw(this, dup);
            dup.dispose();
        }
    }
}
