/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.WidthIncompatibilityData;
import com.cburch.logisim.circuit.WireSet;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.GraphicsUtil;

class CanvasPainter implements AttributeListener {
    private static final int GRID_DOT_COLOR = 0xFF777777;
    private static final int GRID_DOT_ZOOMED_COLOR = 0xFFCCCCCC;

    private static final Color GRID_ZOOMED_OUT_COLOR = new Color(210, 210, 210);
    private static final Set<Component> NO_COMPONENTS = Collections.emptySet();

    private Canvas canvas;
    private boolean showGrid = true;
    private boolean printerView = false;
    private boolean showHalo = true;
    private Component haloedComponent = null;
    private Circuit haloedCircuit = null;
    private WireSet highlightedWires = WireSet.EMPTY;
    private Image gridImage;
    private int gridImageWidth;
    
    CanvasPainter(Canvas canvas) {
        this.canvas = canvas;
    }
    
    //
    // accessor methods
    //
    boolean getShowHalo() {
        return showHalo;
    }
    
    Component getHaloedComponent() {
        return haloedComponent;
    }

    //
    // mutator methods
    //
    void loadOptions(AttributeSet options) {
        options.addAttributeListener(this);
        printerView = options.getValue(Options.preview_attr).booleanValue();
        showGrid = options.getValue(Options.showgrid_attr).booleanValue();
        showHalo = options.getValue(Options.showhalo_attr).booleanValue();
    }

    void setHighlightedWires(WireSet value) {
        highlightedWires = value == null ? WireSet.EMPTY : value;
    }

    void setHaloedComponent(Circuit circ, Component comp) {
        if (comp == haloedComponent) return;
        Graphics g = canvas.getGraphics();
        exposeHaloedComponent(g);
        haloedCircuit = circ;
        haloedComponent = comp;
        exposeHaloedComponent(g);
    }

    private void exposeHaloedComponent(Graphics g) {
        Component c = haloedComponent;
        if (c == null) return;
        Bounds bds = c.getBounds(g).expand(7);
        int w = bds.getWidth();
        int h = bds.getHeight();
        double a = Canvas.SQRT_2 * w;
        double b = Canvas.SQRT_2 * h;
        canvas.repaint((int) Math.round(bds.getX() + w/2.0 - a/2.0),
            (int) Math.round(bds.getY() + h/2.0 - b/2.0),
            (int) Math.round(a), (int) Math.round(b));
    }
    
    //
    // AtttributeListener methods
    //
    public void attributeListChanged(AttributeEvent e) { }
    public void attributeValueChanged(AttributeEvent e) {
        Attribute<?> attr = e.getAttribute();
        Object val = e.getValue();
        if (attr == Options.showgrid_attr) {
            showGrid = ((Boolean) val).booleanValue();
            canvas.repaint();
        } else if (attr == Options.preview_attr) {
            printerView = ((Boolean) val).booleanValue();
            canvas.repaint();
        } else if (attr == Options.showhalo_attr) {
            showHalo = ((Boolean) val).booleanValue();
            canvas.repaint();
        }
    }


    //
    // painting methods
    //
    void paintContents(Graphics g, Project proj) {
        Rectangle clip = g.getClipBounds();
        Dimension size = canvas.getSize();
        double zoomFactor = canvas.getZoomFactor();
        if (canvas.ifPaintDirtyReset() || clip == null) {
            clip = new Rectangle(0, 0, size.width, size.height);
        }
        g.setColor(Color.white);
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        if (showGrid) {
            paintGrid(g, zoomFactor, clip);
        }
        g.setColor(Color.black);

        Graphics gScaled = g.create();
        if (zoomFactor != 1.0 && gScaled instanceof Graphics2D) {
            ((Graphics2D) gScaled).scale(zoomFactor, zoomFactor);
        }
        drawWithUserState(g, gScaled, proj);
        drawWidthIncompatibilityData(g, gScaled, proj);
        Circuit circ = proj.getCurrentCircuit();
        
        CircuitState circState = proj.getCircuitState();
        ComponentDrawContext ptContext = new ComponentDrawContext(canvas,
                circ, circState, g, gScaled);
        ptContext.setHighlightedWires(highlightedWires);
        gScaled.setColor(Color.RED);
        circState.drawOscillatingPoints(ptContext);
        gScaled.setColor(Color.BLUE);
        proj.getSimulator().drawStepPoints(ptContext);
        gScaled.dispose();
    }
    
    private void paintGrid(Graphics g, double zoomFactor, Rectangle clip) {
        Image img = gridImage;
        int w = gridImageWidth;
        if (img == null) {
            paintGridOld(g, zoomFactor, clip);
            return;
        }
        double f = zoomFactor;
        int x0 = 10 * (int) Math.ceil(clip.x / f / 10);
        int y0 = 10 * (int) Math.ceil(clip.y / f / 10);
        for (int x = 0; x < clip.width; x += w) {
            for (int y = 0; y < clip.height; y += w) {
                g.drawImage(gridImage, x0 + x, y0 + y, canvas);
            }
        }
    }
    
    private void paintGridOld(Graphics g, double zoomFactor, Rectangle clip) {
        g.setColor(Color.gray);
        double f = zoomFactor;
        if (f == 1.0) {
            int start_x = ((clip.x + 9) / 10) * 10;
            int start_y = ((clip.y + 9) / 10) * 10;
            for (int x = 0; x < clip.width; x += 10) {
                for (int y = 0; y < clip.height; y += 10) {
                    g.fillRect(start_x + x, start_y + y, 1, 1);
                }
            }
        } else {
            /* Kevin Walsh of Cornell suggested the code below instead. */
            int x0 = 10 * (int) Math.ceil(clip.x / f / 10);
            int x1 = x0 + (int) (clip.width / f);
            int y0 = 10 * (int) Math.ceil(clip.y / f / 10);
            int y1 = y0 + (int) (clip.height / f);
            if (f <= 0.5) g.setColor(GRID_ZOOMED_OUT_COLOR);
            for (double x = x0; x < x1; x += 10) {
                for (double y = y0; y < y1; y += 10) {
                    int sx = (int) Math.round(f * x);
                    int sy = (int) Math.round(f * y);
                    g.fillRect(sx, sy, 1, 1);
                }
            }
            if (f <= 0.5) { // make every 5th pixel darker
                g.setColor(Color.gray);
                x0 = 50 * (int) Math.ceil(clip.x / f / 50);
                y0 = 50 * (int) Math.ceil(clip.y / f / 50);
                for (double x = x0; x < x1; x += 50) {
                    for (double y = y0; y < y1; y += 50) {
                        int sx = (int) Math.round(f * x);
                        int sy = (int) Math.round(f * y);
                        g.fillRect(sx, sy, 1, 1);
                    }
                }
            }               
            
            /* Original code by Carl Burch
            int x0 = 10 * (int) Math.ceil(clip.x / f / 10);
            int x1 = x0 + (int)(clip.width / f);
            int y0 = 10 * (int) Math.ceil(clip.y / f / 10);
            int y1 = y0 + (int) (clip.height / f);
            int s = f > 0.5 ? 1 : f > 0.25 ? 2 : 3;
            int i0 = s - ((x0 + 10*s - 1) % (s * 10)) / 10 - 1;
            int j0 = s - ((y1 + 10*s - 1) % (s * 10)) / 10 - 1;
            for (int i = 0; i < s; i++) {
                for (int x = x0+i*10; x < x1; x += s*10) {
                    for (int j = 0; j < s; j++) {
                        g.setColor(i == i0 && j == j0 ? Color.gray : GRID_ZOOMED_OUT_COLOR);
                        for (int y = y0+j*10; y < y1; y += s*10) {
                            int sx = (int) Math.round(f * x);
                            int sy = (int) Math.round(f * y);
                            g.fillRect(sx, sy, 1, 1);
                        }
                    }
                }
            }
            */
        }
    }

    private void drawWithUserState(Graphics base, Graphics g, Project proj) {
        Circuit circ = proj.getCurrentCircuit();
        Selection sel = proj.getSelection();
        Set<Component> hidden;
        Tool dragTool = canvas.getDragTool();
        if (dragTool == null) {
            hidden = NO_COMPONENTS;
        } else {
            hidden = dragTool.getHiddenComponents(canvas);
            if (hidden == null) hidden = NO_COMPONENTS;
        }

        // draw halo around component whose attributes we are viewing
        if (showHalo && haloedComponent != null && haloedCircuit == circ
                && !hidden.contains(haloedComponent)) {
            GraphicsUtil.switchToWidth(g, 3);
            g.setColor(AttributeTable.HALO_COLOR);
            Bounds bds = haloedComponent.getBounds(g).expand(5);
            int w = bds.getWidth();
            int h = bds.getHeight();
            double a = Canvas.SQRT_2 * w;
            double b = Canvas.SQRT_2 * h;
            g.drawOval((int) Math.round(bds.getX() + w/2.0 - a/2.0),
                (int) Math.round(bds.getY() + h/2.0 - b/2.0),
                (int) Math.round(a), (int) Math.round(b));
            GraphicsUtil.switchToWidth(g, 1);
            g.setColor(Color.BLACK);
        }

        // draw circuit and selection
        CircuitState circState = proj.getCircuitState();
        ComponentDrawContext context = new ComponentDrawContext(canvas,
                circ, circState, base, g, printerView);
        context.setHighlightedWires(highlightedWires);
        circ.draw(context, hidden);
        sel.draw(context, hidden);

        // draw tool
        Tool tool = dragTool != null ? dragTool : proj.getTool();
        if (tool != null && !canvas.isPopupMenuUp()) {
            Graphics gCopy = g.create();
            context.setGraphics(gCopy);
            tool.draw(canvas, context);
            gCopy.dispose();
        }
    }
    
    private void drawWidthIncompatibilityData(Graphics base, Graphics g, Project proj) {
        Set<WidthIncompatibilityData> exceptions;
        exceptions = proj.getCurrentCircuit().getWidthIncompatibilityData();
        if (exceptions == null || exceptions.size() == 0) return;

        g.setColor(Value.WIDTH_ERROR_COLOR);
        GraphicsUtil.switchToWidth(g, 2);
        FontMetrics fm = base.getFontMetrics(g.getFont());
        for (WidthIncompatibilityData ex : exceptions) {
            for (int i = 0; i < ex.size(); i++) {
                Location p = ex.getPoint(i);
                BitWidth w = ex.getBitWidth(i);

                // ensure it hasn't already been drawn
                boolean drawn = false;
                for (int j = 0; j < i; j++) {
                    if (ex.getPoint(j).equals(p)) { drawn = true; break; }
                }
                if (drawn) continue;

                // compute the caption combining all similar points
                String caption = "" + w.getWidth();
                for (int j = i + 1; j < ex.size(); j++) {
                    if (ex.getPoint(j).equals(p)) { caption += "/" + ex.getBitWidth(j); break; }
                }
                g.drawOval(p.getX() - 4, p.getY() - 4, 8, 8);
                g.drawString(caption, p.getX() + 5, p.getY() + 2 + fm.getAscent());
            }
        }
        g.setColor(Color.BLACK);
        GraphicsUtil.switchToWidth(g, 1);
    }

    //
    // creating the grid image
    //
    void updateGridImage(double f) {
        double ww = f * 50;
        while (2 * ww < 150) ww *= 2;
        int w = (int) Math.round(ww);
        int[] pix = new int[w * w];
        Arrays.fill(pix, 0xFFFFFF);

        if (f == 1.0) {
            for (int j = 0; j < pix.length; j += 10 * w) {
                for (int i = 0; i < w; i += 10) {
                    pix[i + j] = GRID_DOT_COLOR;
                }
            }
        } else {
            int dotColor = f <= 0.5 ? GRID_DOT_ZOOMED_COLOR : GRID_DOT_COLOR;
            for (int j = 0; true; j += 10) {
                int y = (int) Math.round(f * j);
                if (y >= w) break;
                y *= w;
                
                for (int i = 0; true; i += 10) {
                    int x = (int) Math.round(f * i);
                    if (x >= w) break;
                    pix[y + x] = dotColor;
                }
            }
            if (f <= 0.5) { // make every 5th pixel darker
                for (int j = 0; true; j += 50) {
                    int y = (int) Math.round(f * j);
                    if (y >= w) break;
                    y *= w;
                    
                    for (int i = 0; true; i += 50) {
                        int x = (int) Math.round(f * i);
                        if (x >= w) break;
                        pix[y + x] = GRID_DOT_COLOR;
                    }
                }
            }
        }
        gridImage = canvas.createImage(new MemoryImageSource(w, w, pix, 0, w));
        gridImageWidth = w;
    }
}
