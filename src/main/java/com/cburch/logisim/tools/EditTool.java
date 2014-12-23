/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitEvent;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.gui.main.Selection;
import com.cburch.logisim.gui.main.SelectionActions;
import com.cburch.logisim.gui.main.Selection.Event;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.util.GraphicsUtil;
import static com.cburch.logisim.util.LocaleString.*;


public class EditTool extends Tool {
    private static final int CACHE_MAX_SIZE = 32;
    private static final Location NULL_LOCATION
        = Location.create(Integer.MIN_VALUE, Integer.MIN_VALUE);

    private class Listener implements CircuitListener, Selection.Listener {
        @Override
        public void circuitChanged(CircuitEvent event) {
            if (event.getAction() != CircuitEvent.ACTION_INVALIDATE) {
                lastX = -1;
                cache.clear();
                updateLocation(lastCanvas, lastRawX, lastRawY, lastMods);
            }
        }

        @Override
        public void selectionChanged(Event event) {
            lastX = -1;
            cache.clear();
            updateLocation(lastCanvas, lastRawX, lastRawY, lastMods);
        }
    }

    private Listener listener;
    private SelectTool select;
    private WiringTool wiring;
    private Tool current;
    private LinkedHashMap<Location,Boolean> cache;
    private Canvas lastCanvas;
    private int lastRawX;
    private int lastRawY;
    // last coordinates where wiring was computed
    private int lastX;
    private int lastY;
    // last modifiers for mouse event
    private int lastMods;
    // coordinates where to draw wiring indicator, if
    private Location wireLoc;
    // last coordinate where mouse was pressed
    private int pressX;
    // (used to determine when a short wire has been clicked)
    private int pressY;

    public EditTool(SelectTool select, WiringTool wiring) {
        this.listener = new Listener();
        this.select = select;
        this.wiring = wiring;
        this.current = select;
        this.cache = new LinkedHashMap<Location,Boolean>();
        this.lastX = -1;
        this.wireLoc = NULL_LOCATION;
        this.pressX = -1;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EditTool;
    }

    @Override
    public int hashCode() {
        return EditTool.class.hashCode();
    }

    @Override
    public String getName() {
        return "Edit Tool";
    }

    @Override
    public String getDisplayName() {
        return getFromLocale("editTool");
    }

    @Override
    public String getDescription() {
        return getFromLocale("editToolDesc");
    }

    @Override
    public AttributeSet getAttributeSet() {
        return select.getAttributeSet();
    }

    @Override
    public void setAttributeSet(AttributeSet attrs) {
        select.setAttributeSet(attrs);
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
    public void paintIcon(ComponentDrawContext c, int x, int y) {
        select.paintIcon(c, x, y);
    }

    @Override
    public Set<Component> getHiddenComponents(Canvas canvas) {
        return current.getHiddenComponents(canvas);
    }

    @Override
    public void draw(Canvas canvas, ComponentDrawContext context) {
        Location loc = wireLoc;
        if (loc != NULL_LOCATION && current != wiring) {
            int x = loc.getX();
            int y = loc.getY();
            Graphics g = context.getGraphics();
            g.setColor(Value.TRUE_COLOR);
            GraphicsUtil.switchToWidth(g, 2);
            g.drawOval(x - 5, y - 5, 10, 10);
            g.setColor(Color.BLACK);
            GraphicsUtil.switchToWidth(g, 1);
        }
        current.draw(canvas, context);
    }

    @Override
    public void select(Canvas canvas) {
        current = select;
        lastCanvas = canvas;
        cache.clear();
        canvas.getCircuit().addCircuitListener(listener);
        canvas.getSelection().addListener(listener);
        select.select(canvas);
    }

    @Override
    public void deselect(Canvas canvas) {
        current = select;
        canvas.getSelection().setSuppressHandles(null);
        cache.clear();
        canvas.getCircuit().removeCircuitListener(listener);
        canvas.getSelection().removeListener(listener);
    }

    @Override
    public void mousePressed(Canvas canvas, Graphics g, MouseEvent e) {
        boolean wire = updateLocation(canvas, e);
        Location oldWireLoc = wireLoc;
        wireLoc = NULL_LOCATION;
        lastX = Integer.MIN_VALUE;
        if (wire) {
            current = wiring;
            Selection sel = canvas.getSelection();
            Circuit circ = canvas.getCircuit();
            Collection<Component> selected = sel.getAnchoredComponents();
            ArrayList<Component> suppress = null;
            for (Wire w : circ.getWires()) {
                if (selected.contains(w)) {
                    if (w.contains(oldWireLoc)) {
                        if (suppress == null) {
                            suppress = new ArrayList<Component>();
                        }

                        suppress.add(w);
                    }
                }
            }
            sel.setSuppressHandles(suppress);
        } else {
            current = select;
        }
        pressX = e.getX();
        pressY = e.getY();
        current.mousePressed(canvas, g, e);
    }

    @Override
    public void mouseDragged(Canvas canvas, Graphics g, MouseEvent e) {
        isClick(e);
        current.mouseDragged(canvas, g, e);
    }

    @Override
    public void mouseReleased(Canvas canvas, Graphics g, MouseEvent e) {
        boolean click = isClick(e) && current == wiring;
        canvas.getSelection().setSuppressHandles(null);
        current.mouseReleased(canvas, g, e);
        if (click) {
            wiring.resetClick();
            select.mousePressed(canvas, g, e);
            select.mouseReleased(canvas, g, e);
        }
        current = select;
        cache.clear();
        updateLocation(canvas, e);
    }

    @Override
    public void mouseEntered(Canvas canvas, Graphics g, MouseEvent e) {
        pressX = -1;
        current.mouseEntered(canvas, g, e);
        canvas.requestFocusInWindow();
    }

    @Override
    public void mouseExited(Canvas canvas, Graphics g, MouseEvent e) {
        pressX = -1;
        current.mouseExited(canvas, g, e);
    }

    @Override
    public void mouseMoved(Canvas canvas, Graphics g, MouseEvent e) {
        updateLocation(canvas, e);
        select.mouseMoved(canvas, g, e);
    }

    private boolean isClick(MouseEvent e) {
        int px = pressX;
        if (px < 0) {
            return false;
        } else {
            int dx = e.getX() - px;
            int dy = e.getY() - pressY;
            if (dx * dx + dy * dy <= 4) {
                return true;
            } else {
                pressX = -1;
                return false;
            }
        }
    }

    private boolean updateLocation(Canvas canvas, MouseEvent e) {
        return updateLocation(canvas, e.getX(), e.getY(), e.getModifiersEx());
    }

    private boolean updateLocation(Canvas canvas, KeyEvent e) {
        int x = lastRawX;
        if (x >= 0) {
            return updateLocation(canvas, x, lastRawY, e.getModifiersEx());
        }

        else {
            return false;
        }

    }

    private boolean updateLocation(Canvas canvas, int mx, int my, int mods) {
        int snapx = Canvas.snapXToGrid(mx);
        int snapy = Canvas.snapYToGrid(my);
        int dx = mx - snapx;
        int dy = my - snapy;
        boolean isEligible = dx * dx + dy * dy < 36;
        if ((mods & InputEvent.ALT_DOWN_MASK) != 0) {
            isEligible = true;
        }

        if (!isEligible) {
            snapx = -1;
            snapy = -1;
        }
        boolean modsSame = lastMods == mods;
        lastCanvas = canvas;
        lastRawX = mx;
        lastRawY = my;
        lastMods = mods;
        // already computed
        if (lastX == snapx && lastY == snapy && modsSame) {
            return wireLoc != NULL_LOCATION;
        } else {
            Location snap = Location.create(snapx, snapy);
            if (modsSame) {
                Object o = cache.get(snap);
                if (o != null) {
                    lastX = snapx;
                    lastY = snapy;
                    canvas.repaint();
                    boolean ret = ((Boolean) o).booleanValue();
                    wireLoc = ret ? snap : NULL_LOCATION;
                    return ret;
                }
            } else {
                cache.clear();
            }

            boolean ret = isEligible && isWiringPoint(canvas, snap, mods);
            wireLoc = ret ? snap : NULL_LOCATION;
            cache.put(snap, Boolean.valueOf(ret));
            int toRemove = cache.size() - CACHE_MAX_SIZE;
            Iterator<Location> it = cache.keySet().iterator();
            while (it.hasNext() && toRemove > 0) {
                it.next();
                it.remove();
                toRemove--;
            }

            lastX = snapx;
            lastY = snapy;
            canvas.repaint();
            return ret;
        }
    }

    private boolean isWiringPoint(Canvas canvas, Location loc, int modsEx) {
        boolean wiring = (modsEx & InputEvent.ALT_DOWN_MASK) == 0;
        boolean select = !wiring;

        if (canvas != null && canvas.getSelection() != null) {
            Collection<Component> sel = canvas.getSelection().getComponents();
            if (sel != null) {
                for (Component c : sel) {
                    if (c instanceof Wire) {
                        Wire w = (Wire) c;
                        if (w.contains(loc) && !w.endsAt(loc)) {
                            return select;
                        }

                    }
                }
            }
        }

        Circuit circ = canvas.getCircuit();
        Collection<? extends Component> at = circ.getComponents(loc);
        if (at != null && at.size() > 0) {
            return wiring;
        }


        for (Wire w : circ.getWires()) {
            if (w.contains(loc)) {
                { return wiring;
            }
 }
        }
        return select;
    }

    @Override
    public void keyTyped(Canvas canvas, KeyEvent e) {
        select.keyTyped(canvas, e);
    }

    @Override
    public void keyPressed(Canvas canvas, KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_BACK_SPACE:
        case KeyEvent.VK_DELETE:
            if (!canvas.getSelection().isEmpty()) {
                Action act = SelectionActions.clear(canvas.getSelection());
                canvas.getProject().doAction(act);
                e.consume();
            } else {
                wiring.keyPressed(canvas, e);
            }
            break;
        case KeyEvent.VK_INSERT:
            Action act = SelectionActions.duplicate(canvas.getSelection());
            canvas.getProject().doAction(act);
            e.consume();
            break;
        case KeyEvent.VK_UP:
            if (e.getModifiersEx() == 0) {
                attemptReface(canvas, Direction.NORTH, e);
            }

            else {
                                        select.keyPressed(canvas, e);
            }

            break;
        case KeyEvent.VK_DOWN:
            if (e.getModifiersEx() == 0) {
                attemptReface(canvas, Direction.SOUTH, e);
            }

            else {
                                        select.keyPressed(canvas, e);
            }

            break;
        case KeyEvent.VK_LEFT:
            if (e.getModifiersEx() == 0) {
                attemptReface(canvas, Direction.WEST, e);
            }

            else {
                                        select.keyPressed(canvas, e);
            }

            break;
        case KeyEvent.VK_RIGHT:
            if (e.getModifiersEx() == 0) {
                attemptReface(canvas, Direction.EAST, e);
            }

            else {
                                        select.keyPressed(canvas, e);
            }

            break;
        case KeyEvent.VK_ALT:   updateLocation(canvas, e); e.consume(); break;
        default:
            select.keyPressed(canvas, e);
        }
    }

    @Override
    public void keyReleased(Canvas canvas, KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_ALT:   updateLocation(canvas, e); e.consume(); break;
        default:
            select.keyReleased(canvas, e);
        }
    }

    private void attemptReface(Canvas canvas, final Direction facing, KeyEvent e) {
        if (e.getModifiersEx() == 0) {
            final Circuit circuit = canvas.getCircuit();
            final Selection sel = canvas.getSelection();
            SetAttributeAction act = new SetAttributeAction(circuit,
                    getFromLocale("selectionRefaceAction"));
            for (Component comp : sel.getComponents()) {
                if (!(comp instanceof Wire)) {
                    Attribute<Direction> attr = getFacingAttribute(comp);
                    if (attr != null) {
                        act.set(comp, attr, facing);
                    }
                }
            }
            if (!act.isEmpty()) {
                canvas.getProject().doAction(act);
                e.consume();
            }
        }
    }

    private Attribute<Direction> getFacingAttribute(Component comp) {
        AttributeSet attrs = comp.getAttributeSet();
        Object key = ComponentFactory.FACING_ATTRIBUTE_KEY;
        Attribute<?> a = (Attribute<?>) comp.getFactory().getFeature(key, attrs);
        @SuppressWarnings("unchecked")
        Attribute<Direction> ret = (Attribute<Direction>) a;
        return ret;
    }

    @Override
    public Cursor getCursor() {
        return select.getCursor();
    }
}
