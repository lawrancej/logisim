/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.Icon;

import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.ReplacementMap;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.gui.main.Selection;
import com.cburch.logisim.gui.main.SelectionActions;
import com.cburch.logisim.gui.main.Selection.Event;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.key.KeyConfigurationEvent;
import com.cburch.logisim.tools.key.KeyConfigurator;
import com.cburch.logisim.tools.key.KeyConfigurationResult;
import com.cburch.logisim.tools.move.MoveResult;
import com.cburch.logisim.tools.move.MoveGesture;
import com.cburch.logisim.tools.move.MoveRequestListener;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.Icons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.cburch.logisim.util.LocaleString.*;

public class SelectTool extends Tool {
    private static final Cursor selectCursor
        = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private static final Cursor rectSelectCursor
        = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    private static final Cursor moveCursor
        = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);

    private static final int IDLE = 0;
    private static final int MOVING = 1;
    private static final int RECT_SELECT = 2;
    private static final Icon toolIcon = Icons.getIcon("select.svg");

    private static final Color COLOR_UNMATCHED = new Color(192, 0, 0);
    private static final Color COLOR_COMPUTING = new Color(96, 192, 96);
    private static final Color COLOR_RECT_SELECT = new Color(0, 64, 128, 255);
    private static final Color BACKGROUND_RECT_SELECT = new Color(192, 192, 255, 192);

    private static class MoveRequestHandler implements MoveRequestListener {
        private Canvas canvas;

        MoveRequestHandler(Canvas canvas) {
            this.canvas = canvas;
        }

        @Override
        public void requestSatisfied(MoveGesture gesture, int dx, int dy) {
            clearCanvasMessage(canvas, dx, dy);
        }
    }

    private class Listener implements Selection.Listener {
        @Override
        public void selectionChanged(Event event) {
            keyHandlers = null;
        }
    }

    private Location start;
    private int state;
    private int curDx;
    private int curDy;
    private boolean drawConnections;
    private MoveGesture moveGesture;
    private HashMap<Component,KeyConfigurator> keyHandlers;
    private HashSet<Selection> selectionsAdded;
    private Listener selListener;

    public SelectTool() {
        start = null;
        state = IDLE;
        selectionsAdded = new HashSet<Selection>();
        selListener = new Listener();
        keyHandlers = null;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SelectTool;
    }

    @Override
    public int hashCode() {
        return SelectTool.class.hashCode();
    }

    @Override
    public String getName() {
        return "Select Tool";
    }

    @Override
    public String getDisplayName() {
        return getFromLocale("selectTool");
    }

    @Override
    public String getDescription() {
        return getFromLocale("selectToolDesc");
    }

    @Override
    public AttributeSet getAttributeSet(Canvas canvas) {
        return canvas.getSelection().getAttributeSet();
    }

    @Override
    public boolean isAllDefaultValues(AttributeSet attrs, LogisimVersion ver) {
        return true;
    }

    @Override
    public void draw(Canvas canvas, ComponentDrawContext context) {
        Project proj = canvas.getProject();
        int dx = curDx;
        int dy = curDy;
        if (state == MOVING) {
            proj.getSelection().drawGhostsShifted(context, dx, dy);

            MoveGesture gesture = moveGesture;
            if (gesture != null && drawConnections && (dx != 0 || dy != 0)) {
                MoveResult result = gesture.findResult(dx, dy);
                if (result != null) {
                    Collection<Wire> wiresToAdd = result.getWiresToAdd();
                    Graphics g = context.getGraphics();
                    GraphicsUtil.switchToWidth(g, 3);
                    g.setColor(Color.GRAY);
                    for (Wire w : wiresToAdd) {
                        Location loc0 = w.getEnd0();
                        Location loc1 = w.getEnd1();
                        g.drawLine(loc0.getX(), loc0.getY(),
                                loc1.getX(), loc1.getY());
                    }
                    GraphicsUtil.switchToWidth(g, 1);
                    g.setColor(COLOR_UNMATCHED);
                    for (Location conn : result.getUnconnectedLocations()) {
                        int connX = conn.getX();
                        int connY = conn.getY();
                        g.fillOval(connX - 3, connY - 3, 6, 6);
                        g.fillOval(connX + dx - 3, connY + dy - 3, 6, 6);
                    }
                }
            }
        } else if (state == RECT_SELECT) {
            int left = start.getX();
            int right = left + dx;
            if (left > right) { int i = left; left = right; right = i; }
            int top = start.getY();
            int bot = top + dy;
            if (top > bot) { int i = top; top = bot; bot = i; }

            Graphics gBase = context.getGraphics();
            int w = right - left - 1;
            int h = bot - top - 1;
            if (w > 2 && h > 2) {
                gBase.setColor(BACKGROUND_RECT_SELECT);
                gBase.fillRect(left + 1, top + 1, w - 1, h - 1);
            }

            Circuit circ = canvas.getCircuit();
            Bounds bds = Bounds.create(left, top, right - left, bot - top);
            for (Component c : circ.getAllWithin(bds)) {
                Location cloc = c.getLocation();
                Graphics gDup = gBase.create();
                context.setGraphics(gDup);
                c.getFactory().drawGhost(context, COLOR_RECT_SELECT,
                        cloc.getX(), cloc.getY(), c.getAttributeSet());
                gDup.dispose();
            }

            gBase.setColor(COLOR_RECT_SELECT);
            GraphicsUtil.switchToWidth(gBase, 2);
            if (w < 0) w = 0;
            if (h < 0) h = 0;
            gBase.drawRect(left, top, w, h);
        }
    }

    @Override
    public void select(Canvas canvas) {
        Selection sel = canvas.getSelection();
        if (!selectionsAdded.contains(sel)) {
            sel.addListener(selListener);
        }
    }

    @Override
    public void deselect(Canvas canvas) {
        moveGesture = null;
    }

    @Override
    public void mouseEntered(Canvas canvas, Graphics g, MouseEvent e) {
        canvas.requestFocusInWindow();
    }

    @Override
    public void mousePressed(Canvas canvas, Graphics g, MouseEvent e) {
        Project proj = canvas.getProject();
        Selection sel = proj.getSelection();
        Circuit circuit = canvas.getCircuit();
        start = Location.create(e.getX(), e.getY());
        curDx = 0;
        curDy = 0;
        moveGesture = null;

        // if the user clicks into the selection,
        // selection is being modified
        Collection<Component> in_sel = sel.getComponentsContaining(start, g);
        if (!in_sel.isEmpty()) {
            if ((e.getModifiers() & InputEvent.SHIFT_MASK) == 0) {
                if (e.getClickCount() == 2) { //double click event
                	PopUpLabelAction.triggerLabel(proj, sel, 
                			e.getXOnScreen(), e.getYOnScreen());
				}
                setState(proj, MOVING);
                proj.repaintCanvas();
                return;
            }
            
            Action act = SelectionActions.drop(sel, in_sel);
			if (act != null) {
				proj.doAction(act);
			} 
        }

        // if the user clicks into a component outside selection, user
        // wants to add/reset selection
        Collection<Component> clicked = circuit.getAllContaining(start, g);
        if (!clicked.isEmpty()) {
            if ((e.getModifiers() & InputEvent.SHIFT_MASK) == 0) {
                if (sel.getComponentsContaining(start).isEmpty()) {
                    Action act = SelectionActions.dropAll(sel);
                    if (act != null) {
                        proj.doAction(act);
                    }
                }
            }
            for (Component comp : clicked) {
                if (!in_sel.contains(comp)) {
                    sel.add(comp);
                }
            }
            setState(proj, MOVING);
            proj.repaintCanvas();
            return;
        }

        // The user clicked on the background. This is a rectangular
        // selection (maybe with the shift key down).
        if ((e.getModifiers() & InputEvent.SHIFT_MASK) == 0) {
            Action act = SelectionActions.dropAll(sel);
            if (act != null) {
                proj.doAction(act);
            }
        }
        setState(proj, RECT_SELECT);
        proj.repaintCanvas();
    }

    @Override
    public void mouseDragged(Canvas canvas, Graphics g, MouseEvent e) {
        if (state == MOVING) {
            Project proj = canvas.getProject();
            computeDxDy(proj, e, g);
            handleMoveDrag(canvas, curDx, curDy, e.getModifiersEx());
        } else if (state == RECT_SELECT) {
            Project proj = canvas.getProject();
            curDx = e.getX() - start.getX();
            curDy = e.getY() - start.getY();
            proj.repaintCanvas();
        }
    }

    private void handleMoveDrag(Canvas canvas, int dx, int dy, int modsEx) {
        boolean connect = shouldConnect(canvas, modsEx);
        drawConnections = connect;
        if (connect) {
            MoveGesture gesture = moveGesture;
            if (gesture == null) {
                gesture = new MoveGesture(new MoveRequestHandler(canvas),
                    canvas.getCircuit(), canvas.getSelection().getAnchoredComponents());
                moveGesture = gesture;
            }
            if (dx != 0 || dy != 0) {
                boolean queued = gesture.enqueueRequest(dx, dy);
                if (queued) {
                    canvas.setErrorMessage(getFromLocale("moveWorkingMsg"), dx, dy, COLOR_COMPUTING);
                    // maybe CPU scheduled led the request to be satisfied
                    // just before the "if(queued)" statement. In any case, it
                    // doesn't hurt to check to ensure the message belongs.
                    if (gesture.findResult(dx, dy) != null) {
                        clearCanvasMessage(canvas, dx, dy);
                    }
                }
            }
        }
        canvas.repaint();
    }

    private boolean shouldConnect(Canvas canvas, int modsEx) {
        boolean shiftReleased = (modsEx & InputEvent.SHIFT_DOWN_MASK) == 0;
        boolean dflt = AppPreferences.MOVE_KEEP_CONNECT.getBoolean();
        return shiftReleased ? dflt : !dflt;
    }

    @Override
    public void mouseReleased(Canvas canvas, Graphics g, MouseEvent e) {
        Project proj = canvas.getProject();
        if (state == MOVING) {
            setState(proj, IDLE);
            computeDxDy(proj, e, g);
            int dx = curDx;
            int dy = curDy;
            if (dx != 0 || dy != 0) {
                if (!proj.getLogisimFile().contains(canvas.getCircuit())) {
                    canvas.setErrorMessage(getFromLocale("cannotModifyError"), dx, dy);
                } else if (proj.getSelection().hasConflictWhenMoved(dx, dy)) {
                    canvas.setErrorMessage(getFromLocale("exclusiveError"), dx, dy);
                } else {
                    boolean connect = shouldConnect(canvas, e.getModifiersEx());
                    drawConnections = false;
                    ReplacementMap repl;
                    if (connect) {
                        MoveGesture gesture = moveGesture;
                        if (gesture == null) {
                            gesture = new MoveGesture(new MoveRequestHandler(canvas),
                                    canvas.getCircuit(), canvas.getSelection().getAnchoredComponents());
                        }
                        canvas.setErrorMessage(getFromLocale("moveWorkingMsg"), dx, dy, COLOR_COMPUTING);
                        MoveResult result = gesture.forceRequest(dx, dy);
                        clearCanvasMessage(canvas, dx, dy);
                        repl = result.getReplacementMap();
                    } else {
                        repl = null;
                    }
                    Selection sel = proj.getSelection();
                    proj.doAction(SelectionActions.translate(sel, dx, dy, repl));
                }
            }
            moveGesture = null;
            proj.repaintCanvas();
        } else if (state == RECT_SELECT) {
            Bounds bds = Bounds.create(start).add(start.getX() + curDx,
                start.getY() + curDy);
            Circuit circuit = canvas.getCircuit();
            Selection sel = proj.getSelection();
            Collection<Component> in_sel = sel.getComponentsWithin(bds, g);
            for (Component comp : circuit.getAllWithin(bds, g)) {
                if (!in_sel.contains(comp)) sel.add(comp);
            }
            Action act = SelectionActions.drop(sel, in_sel);
            if (act != null) {
                proj.doAction(act);
            }
            setState(proj, IDLE);
            proj.repaintCanvas();
        }
    }

    @Override
    public void keyPressed(Canvas canvas, KeyEvent e) {
        if (state == MOVING && e.getKeyCode() == KeyEvent.VK_SHIFT) {
            handleMoveDrag(canvas, curDx, curDy, e.getModifiersEx());
        } else {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_BACK_SPACE:
            case KeyEvent.VK_DELETE:
                if (!canvas.getSelection().isEmpty()) {
                    Action act = SelectionActions.clear(canvas.getSelection());
                    canvas.getProject().doAction(act);
                    e.consume();
                }
                break;
            default:
                processKeyEvent(canvas, e, KeyConfigurationEvent.KEY_PRESSED);
            }
        }
    }

    @Override
    public void keyReleased(Canvas canvas, KeyEvent e) {
        if (state == MOVING && e.getKeyCode() == KeyEvent.VK_SHIFT) {
            handleMoveDrag(canvas, curDx, curDy, e.getModifiersEx());
        } else {
            processKeyEvent(canvas, e, KeyConfigurationEvent.KEY_RELEASED);
        }
    }

    @Override
    public void keyTyped(Canvas canvas, KeyEvent e) {
        processKeyEvent(canvas, e, KeyConfigurationEvent.KEY_TYPED);
    }

    private void processKeyEvent(Canvas canvas, KeyEvent e, int type) {
        HashMap<Component, KeyConfigurator> handlers = keyHandlers;
        if (handlers == null) {
            handlers = new HashMap<Component, KeyConfigurator>();
            Selection sel = canvas.getSelection();
            for (Component comp : sel.getComponents()) {
                ComponentFactory factory = comp.getFactory();
                AttributeSet attrs = comp.getAttributeSet();
                Object handler = factory.getFeature(KeyConfigurator.class, attrs);
                if (handler != null) {
                    KeyConfigurator base = (KeyConfigurator) handler;
                    handlers.put(comp, base.clone());
                }
            }
            keyHandlers = handlers;
        }

        if (!handlers.isEmpty()) {
            boolean consume = false;
            ArrayList<KeyConfigurationResult> results;
            results = new ArrayList<KeyConfigurationResult>();
            for (Map.Entry<Component, KeyConfigurator> entry : handlers.entrySet()) {
                Component comp = entry.getKey();
                KeyConfigurator handler = entry.getValue();
                KeyConfigurationEvent event = new KeyConfigurationEvent(type,
                        comp.getAttributeSet(), e, comp);
                KeyConfigurationResult result = handler.keyEventReceived(event);
                consume |= event.isConsumed();
                if (result != null) {
                    results.add(result);
                }
            }
            if (consume) {
                e.consume();
            }
            if (!results.isEmpty()) {
                SetAttributeAction act = new SetAttributeAction(canvas.getCircuit(),
                        getFromLocale("changeComponentAttributesAction"));
                for (KeyConfigurationResult result : results) {
                    Component comp = (Component) result.getEvent().getData();
                    Map<Attribute<?>,Object> newValues = result.getAttributeValues();
                    for (Map.Entry<Attribute<?>,Object> entry : newValues.entrySet()) {
                        act.set(comp, entry.getKey(), entry.getValue());
                    }
                }
                if (!act.isEmpty()) {
                    canvas.getProject().doAction(act);
                }
            }
        }
    }

    private void computeDxDy(Project proj, MouseEvent e, Graphics g) {
        Bounds bds = proj.getSelection().getBounds(g);
        int dx;
        int dy;
        if (bds == Bounds.EMPTY_BOUNDS) {
            dx = e.getX() - start.getX();
            dy = e.getY() - start.getY();
        } else {
            dx = Math.max(e.getX() - start.getX(), -bds.getX());
            dy = Math.max(e.getY() - start.getY(), -bds.getY());
        }

        Selection sel = proj.getSelection();
        if (sel.shouldSnap()) {
            dx = Canvas.snapXToGrid(dx);
            dy = Canvas.snapYToGrid(dy);
        }
        curDx = dx;
        curDy = dy;
    }

    @Override
    public void paintIcon(ComponentDrawContext c, int x, int y) {
        Graphics g = c.getGraphics();
        if (toolIcon != null) {
            toolIcon.paintIcon(c.getDestination(), g, x + 2, y + 2);
        } else {
            int[] xp = { x+ 5, x+ 5, x+ 9, x+12, x+14, x+11, x+16 };
            int[] yp = { y   , y+17, y+12, y+18, y+18, y+12, y+12 };
            g.setColor(java.awt.Color.black);
            g.fillPolygon(xp, yp, xp.length);
        }
    }

    @Override
    public Cursor getCursor() {
        return state == IDLE ? selectCursor :
            (state == RECT_SELECT ? rectSelectCursor : moveCursor);
    }

    @Override
    public Set<Component> getHiddenComponents(Canvas canvas) {
        if (state == MOVING) {
            int dx = curDx;
            int dy = curDy;
            if (dx == 0 && dy == 0) {
                return null;
            }

            Set<Component> sel = canvas.getSelection().getComponents();
            MoveGesture gesture = moveGesture;
            if (gesture != null && drawConnections) {
                MoveResult result = gesture.findResult(dx, dy);
                if (result != null) {
                    HashSet<Component> ret = new HashSet<Component>(sel);
                    ret.addAll(result.getReplacementMap().getRemovals());
                    return ret;
                }
            }
            return sel;
        } else {
            return null;
        }
    }

    private void setState(Project proj, int new_state) {
        // do nothing if state not new
        if (state == new_state) return;

        state = new_state;
        proj.getFrame().getCanvas().setCursor(getCursor());
    }

    private static void clearCanvasMessage(Canvas canvas, int dx, int dy) {
        Object getter = canvas.getErrorMessage();
        if (getter instanceof ComputingMessage) {
            ComputingMessage msg = (ComputingMessage) getter;
            if (msg.dx == dx && msg.dy == dy) {
                canvas.setErrorMessage(null, 0, 0);
                canvas.repaint();
            }
        }
    }

    public static class ComputingMessage {
        private int dx;
        private int dy;

        public ComputingMessage(int dx, int dy) { this.dx = dx; this.dy = dy; }

        @Override
        public String toString() {
            return getFromLocale("moveWorkingMsg");
        }
    }
}
