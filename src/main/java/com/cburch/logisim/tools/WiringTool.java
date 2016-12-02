/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;

import java.awt.Cursor;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.Icon;

import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.Icons;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import static com.cburch.logisim.util.LocaleString.*;

public class WiringTool extends Tool {
    private static Cursor cursor
        = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    private static final Icon toolIcon = Icons.getIcon("wiring.svg");

    private static final int HORIZONTAL = 1;
    private static final int VERTICAL = 2;

    private boolean exists = false;
    private boolean inCanvas = false;
    private Location start = Location.create(0, 0);
    private Location cur = Location.create(0, 0);
    private boolean hasDragged = false;
    private boolean startShortening = false;
    private Wire shortening = null;
    private Action lastAction = null;
    private int direction = 0;

    public WiringTool() {
        super.select(null);
    }

    @Override
    public void select(Canvas canvas) {
        super.select(canvas);
        lastAction = null;
        reset();
    }

    private void reset() {
        exists = false;
        inCanvas = false;
        start = Location.create(0, 0);
        cur = Location.create(0, 0);
        startShortening = false;
        shortening = null;
        direction = 0;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof WiringTool;
    }

    @Override
    public int hashCode() {
        return WiringTool.class.hashCode();
    }

    @Override
    public String getName() {
        return "Wiring Tool";
    }

    @Override
    public String getDisplayName() {
        return getFromLocale("wiringTool");
    }

    @Override
    public String getDescription() {
        return getFromLocale("wiringToolDesc");
    }

    private boolean computeMove(int newX, int newY) {
        if (cur.getX() == newX && cur.getY() == newY) {
            return false;
        }

        Location start = this.start;
        if (direction == 0) {
            if (newX != start.getX()) {
                direction = HORIZONTAL;
            }
            else if (newY != start.getY()) {
                direction = VERTICAL;
            }

        } else if (direction == HORIZONTAL && newX == start.getX()) {
            if (newY == start.getY()) {
                direction = 0;
            }
            else {
                direction = VERTICAL;
            }

        } else if (direction == VERTICAL && newY == start.getY()) {
            if (newX == start.getX()) {
                direction = 0;
            }
            else {
                direction = HORIZONTAL;
            }

        }
        return true;
    }

    @Override
    public Set<Component> getHiddenComponents(Canvas canvas) {
        Component shorten = willShorten(start, cur);
        if (shorten != null) {
            return Collections.singleton(shorten);
        }
        return null;
    }

    @Override
    public void draw(Canvas canvas, ComponentDrawContext context) {
        Graphics g = context.getGraphics();
        if (exists) {
            Location e0 = start;
            Location e1 = cur;
            Wire shortenBefore = willShorten(start, cur);
            if (shortenBefore != null) {
                Wire shorten = getShortenResult(shortenBefore, start, cur);
                if (shorten == null) {
                    return;
                }
                e0 = shorten.getEnd0();
                e1 = shorten.getEnd1();
            }
            int x0 = e0.getX();
            int y0 = e0.getY();
            int x1 = e1.getX();
            int y1 = e1.getY();

            g.setColor(Color.BLACK);
            GraphicsUtil.switchToWidth(g, 3);
            if (direction == HORIZONTAL) {
                if (x0 != x1) {
                    g.drawLine(x0, y0, x1, y0);
                }

                if (y0 != y1) {
                    g.drawLine(x1, y0, x1, y1);
                }

            } else if (direction == VERTICAL) {
                if (y0 != y1) {
                    g.drawLine(x0, y0, x0, y1);
                }

                if (x0 != x1) {
                    g.drawLine(x0, y1, x1, y1);
                }

            }
        } else if (AppPreferences.ADD_SHOW_GHOSTS.getBoolean() && inCanvas) {
            g.setColor(Color.GRAY);
            g.fillOval(cur.getX() - 2, cur.getY() - 2, 5, 5);
        }
    }

    @Override
    public void mouseEntered(Canvas canvas, Graphics g,
            MouseEvent e) {
        inCanvas = true;
        canvas.getProject().repaintCanvas();
    }

    @Override
    public void mouseExited(Canvas canvas, Graphics g,
            MouseEvent e) {
        inCanvas = false;
        canvas.getProject().repaintCanvas();
    }

    @Override
    public void mouseMoved(Canvas canvas, Graphics g, MouseEvent e) {
        if (exists) {
            mouseDragged(canvas, g, e);
        } else {
            Canvas.snapToGrid(e);
            inCanvas = true;
            int curX = e.getX();
            int curY = e.getY();
            if (cur.getX() != curX || cur.getY() != curY) {
                cur = Location.create(curX, curY);
            }
            canvas.getProject().repaintCanvas();
        }
    }

    @Override
    public void mousePressed(Canvas canvas, Graphics g, MouseEvent e) {
        if (!canvas.getProject().getLogisimFile().contains(canvas.getCircuit())) {
            exists = false;
            canvas.setErrorMessage(getFromLocale("cannotModifyError"), 0, 0);
            return;
        }

        if (exists) {
            mouseDragged(canvas, g, e);
        } else {
            Canvas.snapToGrid(e);
            start = Location.create(e.getX(), e.getY());
            cur = start;
            exists = true;
            hasDragged = false;

            startShortening = !canvas.getCircuit().getWires(start).isEmpty();
            shortening = null;

            super.mousePressed(canvas, g, e);
            canvas.getProject().repaintCanvas();
        }
    }

    @Override
    public void mouseDragged(Canvas canvas, Graphics g, MouseEvent e) {
        if (exists) {
            Canvas.snapToGrid(e);
            int curX = e.getX();
            int curY = e.getY();
            if (!computeMove(curX, curY)) {
                return;
            }

            hasDragged = true;

            Rectangle rect = new Rectangle();
            rect.add(start.getX(), start.getY());
            rect.add(cur.getX(), cur.getY());
            rect.add(curX, curY);
            rect.grow(3, 3);

            cur = Location.create(curX, curY);
            super.mouseDragged(canvas, g, e);

            Wire shorten = null;
            if (startShortening) {
                for (Wire w : canvas.getCircuit().getWires(start)) {
                	if (w.contains(cur)) {
                		shorten = w;
                		break;
                	}
                }
            }
            if (shorten == null) {
                for (Wire w : canvas.getCircuit().getWires(cur)) {
                    if (w.contains(start)) {
                    	shorten = w;
                    	break;
                    }
                }
            }
            shortening = shorten;

            canvas.repaint(rect);
        }
    }

    void resetClick() {
        exists = false;
    }

    @Override
    public void mouseReleased(Canvas canvas, Graphics g, MouseEvent e) {
        if (!exists) {
            return;
        }


        Canvas.snapToGrid(e);
        int curX = e.getX();
        int curY = e.getY();
        if (computeMove(curX, curY)) {
            cur = Location.create(curX, curY);
        }
        if (hasDragged) {
            exists = false;
            super.mouseReleased(canvas, g, e);

            ArrayList<Wire> ws = new ArrayList<Wire>(2);
            if (cur.getY() == start.getY() || cur.getX() == start.getX()) {
                Wire w = Wire.create(cur, start);
                w = checkForRepairs(canvas, w, w.getEnd0());
                w = checkForRepairs(canvas, w, w.getEnd1());
                if (performShortening(canvas, start, cur)) {
                    return;
                }
                if (w.getLength() > 0) {
                    ws.add(w);
                }

            } else {
                Location m;
                if (direction == HORIZONTAL) {
                    m = Location.create(cur.getX(), start.getY());
                } else {
                    m = Location.create(start.getX(), cur.getY());
                }
                Wire w0 = Wire.create(start, m);
                Wire w1 = Wire.create(m, cur);
                w0 = checkForRepairs(canvas, w0, start);
                w1 = checkForRepairs(canvas, w1, cur);
                if (w0.getLength() > 0) {
                    ws.add(w0);
                }

                if (w1.getLength() > 0) {
                    ws.add(w1);
                }

            }
            if (ws.size() > 0) {
                CircuitMutation mutation = new CircuitMutation(canvas.getCircuit());
                mutation.addAll(ws);
                String desc;
                if (ws.size() == 1) {
                    desc = getFromLocale("addWireAction");
                }

                else {
                    desc = getFromLocale("addWiresAction");
                }

                Action act = mutation.toAction(desc);
                canvas.getProject().doAction(act);
                lastAction = act;
            }
        }
    }

    private Wire checkForRepairs(Canvas canvas, Wire w, Location end) {
        // don't repair a short wire to nothing
        if (w.getLength() <= 10) {
            return w;
        }

        if (!canvas.getCircuit().getNonWires(end).isEmpty()) {
            return w;
        }


        int delta = (end.equals(w.getEnd0()) ? 10 : -10);
        Location cand;
        if (w.isVertical()) {
            cand = Location.create(end.getX(), end.getY() + delta);
        } else {
            cand = Location.create(end.getX() + delta, end.getY());
        }

        for (Component comp : canvas.getCircuit().getNonWires(cand)) {
            if (comp.getBounds().contains(end, 2)) {
                WireRepair repair = (WireRepair) comp.getFeature(WireRepair.class);
                if (repair != null && repair.shouldRepairWire(new WireRepairData(w, cand))) {
                    w = Wire.create(w.getOtherEnd(end), cand);
                    canvas.repaint(end.getX() - 13, end.getY() - 13, 26, 26);
                    return w;
                }
            }
        }
        return w;
    }

    private Wire willShorten(Location drag0, Location drag1) {
        Wire shorten = shortening;
        if (shorten == null) {
            return null;
        }
        if (shorten.endsAt(drag0) || shorten.endsAt(drag1)) {
            return shorten;
        }
        return null;
    }

    private Wire getShortenResult(Wire shorten, Location drag0, Location drag1) {
        if (shorten == null) {
            return null;
        }

        Location e0;
        Location e1;
        if (shorten.endsAt(drag0)) {
        	e0 = drag1;
        	e1 = shorten.getOtherEnd(drag0);
        } else if (shorten.endsAt(drag1)) {
        	e0 = drag0;
        	e1 = shorten.getOtherEnd(drag1);
        } else {
        	return null;
        }
        return e0.equals(e1) ? null : Wire.create(e0, e1);
    }

    private boolean performShortening(Canvas canvas, Location drag0, Location drag1) {
    	Wire shorten = willShorten(drag0, drag1);
    	if (shorten == null) {
    		return false;
    	}
    	
    	CircuitMutation xn = new CircuitMutation(canvas.getCircuit());
    	String actName;
    	Wire result = getShortenResult(shorten, drag0, drag1);
    	if (result == null) {
    		xn.remove(shorten);
    		actName = getFromLocale("removeComponentAction", shorten.getFactory().getDisplayGetter());
    	} else {
    		xn.replace(shorten, result);
    		actName = getFromLocale("shortenWireAction");
    	}
    	canvas.getProject().doAction(xn.toAction(actName));
    	return true;
    }

    @Override
    public void keyPressed(Canvas canvas, KeyEvent event) {
        switch (event.getKeyCode()) {
        case KeyEvent.VK_BACK_SPACE:
            if (lastAction != null && canvas.getProject().getLastAction() == lastAction) {
                canvas.getProject().undoAction();
                lastAction = null;
            }
        }
    }

    @Override
    public void paintIcon(ComponentDrawContext c, int x, int y) {
        Graphics g = c.getGraphics();
        if (toolIcon != null) {
            toolIcon.paintIcon(c.getDestination(), g, x + 2, y + 2);
        } else {
            g.setColor(java.awt.Color.black);
            /*
             * Wiring Tool under basics
             * Icon:
             * 2nd Line: Box Left
             * 3rd Line: Box Right
             * 1st Line: Connection between Boxes
             */
            g.drawLine(x + 3, y + 13, x + 17, y + 7);
            g.fillOval(x + 1, y + 11, 5, 5);
            g.fillOval(x + 15, y + 5, 5, 5);
        }
    }

    @Override
    public Cursor getCursor() {
    	return cursor;
    }
}
