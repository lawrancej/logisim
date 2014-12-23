/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitException;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.circuit.SubcircuitFactory;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.gui.main.SelectionActions;
import com.cburch.logisim.gui.main.ToolAttributeAction;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Dependencies;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.key.KeyConfigurationEvent;
import com.cburch.logisim.tools.key.KeyConfigurator;
import com.cburch.logisim.tools.key.KeyConfigurationResult;
import static com.cburch.logisim.util.LocaleString.*;

public class AddTool extends Tool {
    private static int INVALID_COORD = Integer.MIN_VALUE;

    private static int SHOW_NONE    = 0;
    private static int SHOW_GHOST   = 1;
    private static int SHOW_ADD     = 2;
    private static int SHOW_ADD_NO  = 3;

    private static Cursor cursor
        = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

    private class MyAttributeListener implements AttributeListener {
        @Override
        public void attributeListChanged(AttributeEvent e) {
            bounds = null;
        }
        @Override
        public void attributeValueChanged(AttributeEvent e) {
            bounds = null;
        }
    }

    private Class<? extends Library> descriptionBase;
    private FactoryDescription description;
    private boolean sourceLoadAttempted;
    private ComponentFactory factory;
    private AttributeSet attrs;
    private Bounds bounds;
    private boolean shouldSnap;
    private int lastX = INVALID_COORD;
    private int lastY = INVALID_COORD;
    private int state = SHOW_GHOST;
    private Action lastAddition;
    private boolean keyHandlerTried;
    private KeyConfigurator keyHandler;

    public AddTool(Class<? extends Library> base, FactoryDescription description) {
        this.descriptionBase = base;
        this.description = description;
        this.sourceLoadAttempted = false;
        this.shouldSnap = true;
        this.attrs = new FactoryAttributes(base, description);
        attrs.addAttributeListener(new MyAttributeListener());
        this.keyHandlerTried = false;
    }

    public AddTool(ComponentFactory source) {
        this.description = null;
        this.sourceLoadAttempted = true;
        this.factory = source;
        this.bounds = null;
        this.attrs = new FactoryAttributes(source);
        attrs.addAttributeListener(new MyAttributeListener());
        Boolean value = (Boolean) source.getFeature(ComponentFactory.SHOULD_SNAP, attrs);
        this.shouldSnap = value == null ? true : value.booleanValue();
    }

    private AddTool(AddTool base) {
        this.descriptionBase = base.descriptionBase;
        this.description = base.description;
        this.sourceLoadAttempted = base.sourceLoadAttempted;
        this.factory = base.factory;
        this.bounds = base.bounds;
        this.shouldSnap = base.shouldSnap;
        this.attrs = (AttributeSet) base.attrs.clone();
        attrs.addAttributeListener(new MyAttributeListener());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AddTool)) {
            return false;
        }

        AddTool o = (AddTool) other;
        if (this.description != null) {
            return this.descriptionBase == o.descriptionBase
                && this.description.equals(o.description);
        } else {
            return this.factory.equals(o.factory);
        }
    }

    @Override
    public int hashCode() {
        FactoryDescription desc = description;
        return desc != null ? desc.hashCode() : factory.hashCode();
    }

    @Override
    public boolean sharesSource(Tool other) {
        if (!(other instanceof AddTool)) {
            return false;
        }

        AddTool o = (AddTool) other;
        if (this.sourceLoadAttempted && o.sourceLoadAttempted) {
            return this.factory.equals(o.factory);
        } else if (this.description == null) {
            return o.description == null;
        } else {
            return this.description.equals(o.description);
        }
    }

    public ComponentFactory getFactory(boolean forceLoad) {
        return forceLoad ? getFactory() : factory;
    }

    public ComponentFactory getFactory() {
        ComponentFactory ret = factory;
        if (ret != null || sourceLoadAttempted) {
            return ret;
        } else {
            ret = description.getFactory(descriptionBase);
            if (ret != null) {
                AttributeSet base = getBaseAttributes();
                Boolean value = (Boolean) ret.getFeature(ComponentFactory.SHOULD_SNAP, base);
                shouldSnap = value == null ? true : value.booleanValue();
            }
            factory = ret;
            sourceLoadAttempted = true;
            return ret;
        }
    }

    @Override
    public String getName() {
        FactoryDescription desc = description;
        return desc == null ? factory.getName() : desc.getName();
    }

    @Override
    public String getDisplayName() {
        FactoryDescription desc = description;
        return desc == null ? factory.getDisplayName() : desc.getDisplayName();
    }

    @Override
    public String getDescription() {
        String ret;
        FactoryDescription desc = description;
        if (desc != null) {
            ret = desc.getToolTip();
        } else {
            ComponentFactory source = getFactory();
            if (source != null) {
                ret = (String) source.getFeature(ComponentFactory.TOOL_TIP,
                        getAttributeSet());
            } else {
                ret = null;
            }
        }
        if (ret == null) {
            ret = getFromLocale("addToolText", getDisplayName());
        }
        return ret;
    }

    @Override
    public Tool cloneTool() {
        return new AddTool(this);
    }

    @Override
    public AttributeSet getAttributeSet() {
        return attrs;
    }

    @Override
    public boolean isAllDefaultValues(AttributeSet attrs, LogisimVersion ver) {
        return this.attrs == attrs && attrs instanceof FactoryAttributes
            && !((FactoryAttributes) attrs).isFactoryInstantiated();
    }

    @Override
    public Object getDefaultAttributeValue(Attribute<?> attr, LogisimVersion ver) {
        return getFactory().getDefaultAttributeValue(attr, ver);
    }

    @Override
    public void draw(Canvas canvas, ComponentDrawContext context) {
        // next "if" suggested roughly by Kevin Walsh of Cornell to take care of
        // repaint problems on OpenJDK under Ubuntu
        int x = lastX;
        int y = lastY;
        if (x == INVALID_COORD || y == INVALID_COORD) {
            return;
        }

        ComponentFactory source = getFactory();
        if (source == null) {
            return;
        }

        if (state == SHOW_GHOST) {
            source.drawGhost(context, Color.GRAY, x, y, getBaseAttributes());
        } else if (state == SHOW_ADD) {
            source.drawGhost(context, Color.BLACK, x, y, getBaseAttributes());
        }
    }

    private AttributeSet getBaseAttributes() {
        AttributeSet ret = attrs;
        if (ret instanceof FactoryAttributes) {
            ret = ((FactoryAttributes) ret).getBase();
        }
        return ret;
    }

    public void cancelOp() { }

    @Override
    public void select(Canvas canvas) {
        setState(canvas, SHOW_GHOST);
        bounds = null;
    }

    @Override
    public void deselect(Canvas canvas) {
        setState(canvas, SHOW_GHOST);
        moveTo(canvas, canvas.getGraphics(), INVALID_COORD, INVALID_COORD);
        bounds = null;
        lastAddition = null;
    }

    private synchronized void moveTo(Canvas canvas, Graphics g,
            int x, int y) {
        if (state != SHOW_NONE) {
            expose(canvas, lastX, lastY);
        }

        lastX = x;
        lastY = y;
        if (state != SHOW_NONE) {
            expose(canvas, lastX, lastY);
        }

    }

    @Override
    public void mouseEntered(Canvas canvas, Graphics g,
            MouseEvent e) {
        if (state == SHOW_GHOST || state == SHOW_NONE) {
            setState(canvas, SHOW_GHOST);
            canvas.requestFocusInWindow();
        } else if (state == SHOW_ADD_NO) {
            setState(canvas, SHOW_ADD);
            canvas.requestFocusInWindow();
        }
    }

    @Override
    public void mouseExited(Canvas canvas, Graphics g,
            MouseEvent e) {
        if (state == SHOW_GHOST) {
            moveTo(canvas, canvas.getGraphics(), INVALID_COORD, INVALID_COORD);
            setState(canvas, SHOW_NONE);
        } else if (state == SHOW_ADD) {
            moveTo(canvas, canvas.getGraphics(), INVALID_COORD, INVALID_COORD);
            setState(canvas, SHOW_ADD_NO);
        }
    }

    @Override
    public void mouseMoved(Canvas canvas, Graphics g, MouseEvent e) {
        if (state != SHOW_NONE) {
            if (shouldSnap) {
                Canvas.snapToGrid(e);
            }

            moveTo(canvas, g, e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(Canvas canvas, Graphics g, MouseEvent e) {
        // verify the addition would be valid
        Circuit circ = canvas.getCircuit();
        if (!canvas.getProject().getLogisimFile().contains(circ)) {
            canvas.setErrorMessage(getFromLocale("cannotModifyError"), 0, 0);
            return;
        }
        if (factory instanceof SubcircuitFactory) {
            SubcircuitFactory circFact = (SubcircuitFactory) factory;
            Dependencies depends = canvas.getProject().getDependencies();
            if (!depends.canAdd(circ, circFact.getSubcircuit())) {
                canvas.setErrorMessage(getFromLocale("circularError"), 0, 0);
                return;
            }
        }

        if (shouldSnap) {
            Canvas.snapToGrid(e);
        }

        moveTo(canvas, g, e.getX(), e.getY());
        setState(canvas, SHOW_ADD);
    }

    @Override
    public void mouseDragged(Canvas canvas, Graphics g, MouseEvent e) {
        if (state != SHOW_NONE) {
            if (shouldSnap) {
                Canvas.snapToGrid(e);
            }

            moveTo(canvas, g, e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(Canvas canvas, Graphics g,
            MouseEvent e) {
        Component added = null;
        if (state == SHOW_ADD) {
            Circuit circ = canvas.getCircuit();
            if (!canvas.getProject().getLogisimFile().contains(circ)) {
                return;
            }

            if (shouldSnap) {
                Canvas.snapToGrid(e);
            }

            moveTo(canvas, g, e.getX(), e.getY());

            Location loc = Location.create(e.getX(), e.getY());
            AttributeSet attrsCopy = (AttributeSet) attrs.clone();
            ComponentFactory source = getFactory();
            if (source == null) {
                return;
            }

            Component c = source.createComponent(loc, attrsCopy);

            if (circ.hasConflict(c)) {
                canvas.setErrorMessage(getFromLocale("exclusiveError"), 0, 0);
                return;
            }

            Bounds bds = c.getBounds(g);
            if (bds.getX() < 0 || bds.getY() < 0) {
                canvas.setErrorMessage(getFromLocale("negativeCoordError"), 0, 0);
                return;
            }

            try {
                CircuitMutation mutation = new CircuitMutation(circ);
                mutation.add(c);
                Action action = mutation.toAction(getFromLocale("addComponentAction", factory.getDisplayGetter()));
                canvas.getProject().doAction(action);
                lastAddition = action;
                added = c;
            } catch (CircuitException ex) {
                JOptionPane.showMessageDialog(canvas.getProject().getFrame(),
                    ex.getMessage());
            }
            setState(canvas, SHOW_GHOST);
        } else if (state == SHOW_ADD_NO) {
            setState(canvas, SHOW_NONE);
        }

        Project proj = canvas.getProject();
        Tool next = determineNext(proj);
        if (next != null) {
            proj.setTool(next);
            Action act = SelectionActions.dropAll(canvas.getSelection());
            if (act != null) {
                proj.doAction(act);
            }
            if (added != null) {
                canvas.getSelection().add(added);
            }

        }
    }

    private Tool determineNext(Project proj) {
        String afterAdd = AppPreferences.ADD_AFTER.get();
        if (afterAdd.equals(AppPreferences.ADD_AFTER_UNCHANGED)) {
            return null;
        // switch to Edit Tool
        } else {
            Library base = proj.getLogisimFile().getLibrary("Base");
            if (base == null) {
                return null;
            } else {
                return base.getTool("Edit Tool");
            }
        }
    }

    @Override
    public void keyPressed(Canvas canvas, KeyEvent event) {
        processKeyEvent(canvas, event, KeyConfigurationEvent.KEY_PRESSED);

        if (!event.isConsumed() && event.getModifiersEx() == 0) {
            switch (event.getKeyCode()) {
            case KeyEvent.VK_UP:    setFacing(canvas, Direction.NORTH); break;
            case KeyEvent.VK_DOWN:  setFacing(canvas, Direction.SOUTH); break;
            case KeyEvent.VK_LEFT:  setFacing(canvas, Direction.WEST); break;
            case KeyEvent.VK_RIGHT: setFacing(canvas, Direction.EAST); break;
            case KeyEvent.VK_BACK_SPACE:
                if (lastAddition != null && canvas.getProject().getLastAction() == lastAddition) {
                    canvas.getProject().undoAction();
                    lastAddition = null;
                }
            }
        }
    }

    @Override
    public void keyReleased(Canvas canvas, KeyEvent event) {
        processKeyEvent(canvas, event, KeyConfigurationEvent.KEY_RELEASED);
    }

    @Override
    public void keyTyped(Canvas canvas, KeyEvent event) {
        processKeyEvent(canvas, event, KeyConfigurationEvent.KEY_TYPED);
    }

    private void processKeyEvent(Canvas canvas, KeyEvent event, int type) {
        KeyConfigurator handler = keyHandler;
        if (!keyHandlerTried) {
            ComponentFactory source = getFactory();
            AttributeSet baseAttrs = getBaseAttributes();
            handler = (KeyConfigurator) source.getFeature(KeyConfigurator.class, baseAttrs);
            keyHandler = handler;
            keyHandlerTried = true;
        }

        if (handler != null) {
            AttributeSet baseAttrs = getBaseAttributes();
            KeyConfigurationEvent e = new KeyConfigurationEvent(type, baseAttrs, event, this);
            KeyConfigurationResult r = handler.keyEventReceived(e);
            if (r != null) {
                Action act = ToolAttributeAction.create(r);
                canvas.getProject().doAction(act);
            }
        }
    }

    private void setFacing(Canvas canvas, Direction facing) {
        ComponentFactory source = getFactory();
        if (source == null) {
            return;
        }

        AttributeSet base = getBaseAttributes();
        Object feature = source.getFeature(ComponentFactory.FACING_ATTRIBUTE_KEY, base);
        @SuppressWarnings("unchecked")
        Attribute<Direction> attr = (Attribute<Direction>) feature;
        if (attr != null) {
            Action act = ToolAttributeAction.create(this, attr, facing);
            canvas.getProject().doAction(act);
        }
    }

    @Override
    public void paintIcon(ComponentDrawContext c, int x, int y) {
        FactoryDescription desc = description;
        if (desc != null && !desc.isFactoryLoaded()) {
            Icon icon = desc.getIcon();
            if (icon != null) {
                icon.paintIcon(c.getDestination(), c.getGraphics(), x + 2, y + 2);
                return;
            }
        }

        ComponentFactory source = getFactory();
        if (source != null) {
            AttributeSet base = getBaseAttributes();
            source.paintIcon(c, x, y, base);
        }
    }

    private void expose(java.awt.Component c, int x, int y) {
        Bounds bds = getBounds();
        c.repaint(x + bds.getX(), y + bds.getY(),
            bds.getWidth(), bds.getHeight());
    }

    @Override
    public Cursor getCursor() { return cursor; }

    private void setState(Canvas canvas, int value) {
        if (value == SHOW_GHOST) {
            if (canvas.getProject().getLogisimFile().contains(canvas.getCircuit())
                    && AppPreferences.ADD_SHOW_GHOSTS.getBoolean()) {
                state = SHOW_GHOST;
            } else {
                state = SHOW_NONE;
            }
        } else{
            state = value;
        }
    }

    private Bounds getBounds() {
        Bounds ret = bounds;
        if (ret == null) {
            ComponentFactory source = getFactory();
            if (source == null) {
                ret = Bounds.EMPTY_BOUNDS;
            } else {
                AttributeSet base = getBaseAttributes();
                ret = source.getOffsetBounds(base).expand(5);
            }
            bounds = ret;
        }
        return ret;
    }
}
