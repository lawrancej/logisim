/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractSpinnerModel;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;

import com.cburch.logisim.util.CustomAction;

import static com.cburch.logisim.util.LocaleString.*;

@SuppressWarnings("serial")
public class ZoomControl extends JPanel {
    private class SpinnerModel extends AbstractSpinnerModel
            implements PropertyChangeListener {
        @Override
        public Object getNextValue() {
            double zoom = model.getZoomFactor();
            double[] choices = model.getZoomOptions();
            double factor = zoom * 100.0 * 1.001;
            for (int i = 0; i < choices.length; i++) {
                if (choices[i] > factor) return toString(choices[i]);
            }
            return null;
        }

        @Override
        public Object getPreviousValue() {
            double zoom = model.getZoomFactor();
            double[] choices = model.getZoomOptions();
            double factor = zoom * 100.0 * 0.999;
            for (int i = choices.length - 1; i >= 0; i--) {
                if (choices[i] < factor) return toString(choices[i]);
            }
            return null;
        }
        
        @Override
        public Object getValue() {
            double zoom = model.getZoomFactor();
            return toString(zoom * 100.0);
        }

        private String toString(double factor) {
            if (factor > 10) {
                return (int) (factor + 0.5) + "%";
            } else if (factor > 0.1) {
                return (int) (factor * 100 + 0.5) / 100.0 + "%";
            } else {
                return factor + "%";
            }
        }

        @Override
        public void setValue(Object value) {
            if (value instanceof String) {
                String s = (String) value;
                if (s.endsWith("%")) s = s.substring(0, s.length() - 1);
                s = s.trim();
                try {
                    double zoom = Double.parseDouble(s) / 100.0;
                    model.setZoomFactor(zoom);
                } catch (NumberFormatException e) { }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            fireStateChanged();
        }
    }

    private class GridIcon extends JComponent
            implements MouseListener, PropertyChangeListener {
        boolean state = true;

        public GridIcon() {
            addMouseListener(this);
            setPreferredSize(new Dimension(15, 15));
            setToolTipText("");
            setFocusable(true);
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            return getFromLocale("zoomShowGrid");
        }

        private void update() {
            boolean grid = model.getShowGrid();
            if (grid != state) {
                state = grid;
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            int width = getWidth();
            int height = getHeight();
            g.setColor(state ? Color.black : getBackground().darker());
            int dim = (Math.min(width, height) - 4) / 3 * 3 + 1;
            int xoff = (width - dim) / 2;
            int yoff = (height - dim) / 2;
            for (int x = 0; x < dim; x += 3) {
                for (int y = 0; y < dim; y += 3) {
                    g.drawLine(x + xoff, y + yoff, x + xoff, y + yoff);
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) { }
        @Override
        public void mouseEntered(MouseEvent e) { }
        @Override
        public void mouseExited(MouseEvent e) { }
        @Override
        public void mouseReleased(MouseEvent e) { }

        @Override
        public void mousePressed(MouseEvent e) {
            model.setShowGrid(!state);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            update();
        }
    }

    private ZoomModel model;
    private JSpinner spinner;
    private SpinnerModel spinnerModel;
    private GridIcon grid;

    public void zoomIn() {
    	spinnerModel.setValue(spinnerModel.getNextValue());
    }
    
    public void zoomOut() {
    	spinnerModel.setValue(spinnerModel.getPreviousValue());
    }
    
    public ZoomControl(ZoomModel model) {
        super(new BorderLayout());
        this.model = model;

        spinnerModel = new SpinnerModel();
        spinner = new JSpinner();
        spinner.setModel(spinnerModel);
        
        //Zooming with CTRL+/-
        InputMap im = this.getInputMap(ZoomControl.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();
        int mask = getToolkit().getMenuShortcutKeyMask();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, mask), "CTRL+");
        am.put("CTRL+", new CustomAction("CTRL+", this));
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, mask), "CTRL+");
        am.put("CTRL+", new CustomAction("CTRL+", this));
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, mask), "CTRL-");
        am.put("CTRL-", new CustomAction("CTRL-", this));
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, mask), "CTRL-");
        am.put("CTRL-", new CustomAction("CTRL-", this));
        
        this.add(spinner, BorderLayout.CENTER);

        grid = new GridIcon();
        this.add(grid, BorderLayout.EAST);
        grid.update();

        model.addPropertyChangeListener(ZoomModel.SHOW_GRID, grid);
        model.addPropertyChangeListener(ZoomModel.ZOOM, spinnerModel);
    }

    public void setZoomModel(ZoomModel value) {
        ZoomModel oldModel = model;
        if (oldModel != value) {
            if (oldModel != null) {
                oldModel.removePropertyChangeListener(ZoomModel.SHOW_GRID, grid);
                oldModel.removePropertyChangeListener(ZoomModel.ZOOM, spinnerModel);
            }
            model = value;
            spinnerModel = new SpinnerModel();
            spinner.setModel(spinnerModel);
            grid.update();
            if (value != null) {
                value.addPropertyChangeListener(ZoomModel.SHOW_GRID, grid);
                value.addPropertyChangeListener(ZoomModel.ZOOM, spinnerModel);
            }
        }
    }
}
