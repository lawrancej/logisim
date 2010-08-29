/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractSpinnerModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;

class ZoomControl extends JPanel {
    private class Model extends AbstractSpinnerModel
            implements ActionListener, ProjectListener, AttributeListener {
        private double[] choices = new double[] { 20, 50, 75, 100, 133, 150, 200 };

        public Object getNextValue() {
            Options opts = canvas.getProject().getOptions();
            Double zoom = opts.getAttributeSet().getValue(Options.zoom_attr);
            double factor = zoom.doubleValue() * 100.0 * 1.001;
            for (int i = 0; i < choices.length; i++) {
                if (choices[i] > factor) return toString(choices[i]);
            }
            return null;
        }

        public Object getPreviousValue() {
            Options opts = canvas.getProject().getOptions();
            Double zoom = opts.getAttributeSet().getValue(Options.zoom_attr);
            double factor = zoom.doubleValue() * 100.0 * 0.999;
            for (int i = choices.length - 1; i >= 0; i--) {
                if (choices[i] < factor) return toString(choices[i]);
            }
            return null;
        }

        public Object getValue() {
            Options opts = canvas.getProject().getOptions();
            Double zoom = opts.getAttributeSet().getValue(Options.zoom_attr);
            return toString(zoom.doubleValue() * 100.0);
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

        public void setValue(Object value) {
            if (value instanceof String) {
                String s = (String) value;
                if (s.endsWith("%")) s = s.substring(0, s.length() - 1);
                s = s.trim();
                try {
                    double rawVal = Double.parseDouble(s) / 100.0;
                    Double val = new Double(rawVal);
                    Options opts = canvas.getProject().getOptions();
                    AttributeSet attrs = opts.getAttributeSet();
                    Double old = attrs.getValue(Options.zoom_attr);
                    if (!val.equals(old)) {
                        attrs.setValue(Options.zoom_attr, val);
                    }
                } catch (NumberFormatException e) { }
            }
        }
        
        public void actionPerformed(ActionEvent event) {
        }

        public void projectChanged(ProjectEvent event) {
            if (event.getAction() == ProjectEvent.ACTION_SET_FILE) {
                Object oldFile = event.getOldData();
                if (oldFile instanceof LogisimFile) {
                    Options opts = ((LogisimFile) oldFile).getOptions();
                    opts.getAttributeSet().removeAttributeListener(this);
                }
                Object newFile = event.getData();
                if (newFile instanceof LogisimFile) {
                    Options opts = ((LogisimFile) newFile).getOptions();
                    opts.getAttributeSet().addAttributeListener(this);
                }
                fireStateChanged();
            }
        }

        public void attributeListChanged(AttributeEvent e) {}

        public void attributeValueChanged(AttributeEvent e) {
            Object attr = e.getAttribute();
            if (attr == Options.zoom_attr) {
                fireStateChanged();
            } else if (attr == Options.showgrid_attr) {
                grid.update();
            }
        }
    }
    
    private class GridIcon extends JComponent implements MouseListener {
        boolean state = true;
        
        public GridIcon() {
            addMouseListener(this);
            setPreferredSize(new Dimension(15, 15));
            setToolTipText("");
        }
        
        @Override
        public String getToolTipText(MouseEvent e) {
            return Strings.get("zoomShowGrid");
        }

        private void update() {
            Options opts = canvas.getProject().getOptions();
            Object o = opts.getAttributeSet().getValue(Options.showgrid_attr);
            boolean b = !(o instanceof Boolean) || ((Boolean) o).booleanValue();
            if (b != state) {
                state = b;
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

        public void mouseClicked(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }

        public void mousePressed(MouseEvent e) {
            Options opts = canvas.getProject().getOptions();
            Object val = opts.getAttributeSet().getValue(Options.showgrid_attr);
            if (val instanceof Boolean) {
                Boolean o = Boolean.valueOf(!((Boolean) val).booleanValue());
                opts.getAttributeSet().setValue(Options.showgrid_attr, o);
            }
        }
    }
    
    private Canvas canvas;
    private GridIcon grid;
    
    public ZoomControl(Canvas canvas) {
        super(new BorderLayout());
        this.canvas = canvas;
        
        Model model = new Model();
        JSpinner spinner = new JSpinner();
        spinner.setModel(model);
        this.add(spinner, BorderLayout.CENTER);
        
        grid = new GridIcon();
        this.add(grid, BorderLayout.EAST);
        grid.update();

        canvas.getProject().addProjectListener(model);
        LogisimFile file = canvas.getProject().getLogisimFile();
        if (file != null) {
            file.getOptions().getAttributeSet().addAttributeListener(model);
        }
        
    }
}
