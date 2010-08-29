/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.AbstractComponentFactory;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentEvent;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.comp.ComponentListener;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.std.base.Clock;
import com.cburch.logisim.std.base.Pin;
import com.cburch.logisim.util.CollectionUtil;
import com.cburch.logisim.util.EventSourceWeakSupport;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringGetter;
import com.cburch.logisim.util.StringUtil;

public class Circuit extends AbstractComponentFactory {
    private class EndChangedTransaction extends CircuitTransaction {
        private Component comp;
        private Map<Location,EndData> toRemove;
        private Map<Location,EndData> toAdd;
        
        EndChangedTransaction(Component comp, Map<Location,EndData> toRemove,
                Map<Location,EndData> toAdd) {
            this.comp = comp;
            this.toRemove = toRemove;
            this.toAdd = toAdd;
        }
        
        @Override
        protected Map<Circuit,Integer> getAccessedCircuits() {
            return Collections.singletonMap(Circuit.this, READ_WRITE);
        }

        @Override
        protected void run(CircuitMutator mutator) {
            for (Location loc : toRemove.keySet()) {
                EndData removed = toRemove.get(loc);
                EndData replaced = toAdd.remove(loc);
                if (replaced == null) {
                    wires.remove(comp, removed);
                } else if (!replaced.equals(removed)) {
                    wires.replace(comp, removed, replaced);
                }
            }
            for (EndData end : toAdd.values()) {
                wires.add(comp, end);
            }   
        }
    }

    private class MyComponentListener implements ComponentListener {
        public void endChanged(ComponentEvent e) {
            locker.checkForWritePermission("ends changed");
            Component comp = e.getSource();
            HashMap<Location,EndData> toRemove = toMap(e.getOldData());
            HashMap<Location,EndData> toAdd = toMap(e.getData());
            EndChangedTransaction xn = new EndChangedTransaction(comp, toRemove, toAdd);
            locker.execute(xn);
            fireEvent(CircuitEvent.ACTION_INVALIDATE, comp);
        }

        private HashMap<Location,EndData> toMap(Object val) {
            HashMap<Location,EndData> map = new HashMap<Location,EndData>();
            if (val instanceof List) {
                @SuppressWarnings("unchecked")
                List<EndData> valList = (List<EndData>) val;
                int i = -1;
                for (EndData end : valList) {
                    i++;
                    if (end != null) {
                        map.put(end.getLocation(), end);
                    }
                }
            } else if (val instanceof EndData) {
                EndData end = (EndData) val;
                map.put(end.getLocation(), end);
            }
            return map;
        }
        
        public void componentInvalidated(ComponentEvent e) {
            fireEvent(CircuitEvent.ACTION_INVALIDATE, e.getSource());
        }
    }

    private MyComponentListener myComponentListener = new MyComponentListener();
    private AttributeSet staticAttrs;
    private EventSourceWeakSupport<CircuitListener> listeners
        = new EventSourceWeakSupport<CircuitListener>();
    private HashSet<Component> comps = new HashSet<Component>(); // doesn't include wires
    CircuitPins pins = new CircuitPins();
    CircuitWires wires = new CircuitWires();
        // wires is package-protected for CircuitState and Analyze only.
    private ArrayList<Component> clocks = new ArrayList<Component>();
    private CircuitLocker locker;
    private WeakHashMap<Subcircuit,Circuit> circuitsUsingThis;

    public Circuit(String name) {
        staticAttrs = CircuitAttributes.createBaseAttrs(this, name);
        locker = new CircuitLocker();
        circuitsUsingThis = new WeakHashMap<Subcircuit,Circuit>();
    }
    
    CircuitLocker getLocker() {
        return locker;
    }
    
    Collection<Circuit> getCircuitsUsingThis() {
        return circuitsUsingThis.values();
    }
    
    public void mutatorClear() {
        locker.checkForWritePermission("clear");

        Set<Component> oldComps = comps;
        comps = new HashSet<Component>();
        pins = new CircuitPins();
        wires = new CircuitWires();
        clocks.clear();
        for (Component comp : oldComps) {
            if (comp instanceof Subcircuit) {
                Subcircuit sub = (Subcircuit) comp;
                Circuit subcirc = (Circuit) sub.getFactory();
                subcirc.circuitsUsingThis.remove(sub);
            }
        }
        fireEvent(CircuitEvent.ACTION_CLEAR, oldComps);
    }

    @Override
    public String toString() {
        return staticAttrs.getValue(CircuitAttributes.NAME_ATTR);
    }
    
    public AttributeSet getStaticAttributes() {
        return staticAttrs;
    }

    //
    // Listener methods
    //
    public void addCircuitListener(CircuitListener what) {
        listeners.add(what);
    }

    public void removeCircuitListener(CircuitListener what) {
        listeners.remove(what);
    }

    void fireEvent(int action, Object data) {
        fireEvent(new CircuitEvent(action, this, data));
    }

    private void fireEvent(CircuitEvent event) {
        for (CircuitListener l : listeners) {
            l.circuitChanged(event);
        }
    }

    void addPinListener(CircuitPinListener l) { pins.addPinListener(l); }
    void removePinListener(CircuitPinListener l) { pins.removePinListener(l); }

    //
    // access methods
    //
    // getName given in ComponentFactory methods
    public Set<WidthIncompatibilityData> getWidthIncompatibilityData() {
        return wires.getWidthIncompatibilityData();
    }

    public BitWidth getWidth(Location p) {
        return wires.getWidth(p);
    }

    public Location getWidthDeterminant(Location p) {
        return wires.getWidthDeterminant(p);
    }
    
    public boolean hasConflict(Component comp) {
        return wires.points.hasConflict(comp);
    }
    
    public Component getExclusive(Location loc) {
        return wires.points.getExclusive(loc);
    }

    private Set<Component> getComponents() {
        return CollectionUtil.createUnmodifiableSetUnion(comps, wires.getWires());
    }
    
    public boolean contains(Component c) {
        return comps.contains(c) || wires.getWires().contains(c);
    }

    public Set<Wire> getWires() {
        return wires.getWires();
    }

    public Set<Component> getNonWires() {
        return comps;
    }

    public Collection<? extends Component> getComponents(Location loc) {
        return wires.points.getComponents(loc);
    }
    
    public Collection<? extends Component> getSplitCauses(Location loc) {
        return wires.points.getSplitCauses(loc);
    }
    
    public Collection<Wire> getWires(Location loc) {
        return wires.points.getWires(loc);
    }
    
    public Collection<? extends Component> getNonWires(Location loc) {
        return wires.points.getNonWires(loc);
    }
    
    public boolean isConnected(Location loc, Component ignore) {
        for (Component o : wires.points.getComponents(loc)) {
            if (o != ignore) return true;
        }
        return false;
    }
    
    public Set<Location> getSplitLocations() {
        return wires.points.getSplitLocations();
    }

    public Collection<Component> getAllContaining(Location pt) {
        HashSet<Component> ret = new HashSet<Component>();
        for (Component comp : getComponents()) {
            if (comp.contains(pt)) ret.add(comp);
        }
        return ret;
    }

    public Collection<Component> getAllContaining(Location pt, Graphics g) {
        HashSet<Component> ret = new HashSet<Component>();
        for (Component comp : getComponents()) {
            if (comp.contains(pt, g)) ret.add(comp);
        }
        return ret;
    }

    public Collection<Component> getAllWithin(Bounds bds) {
        HashSet<Component> ret = new HashSet<Component>();
        for (Component comp : getComponents()) {
            if (bds.contains(comp.getBounds())) ret.add(comp);
        }
        return ret;
    }

    public Collection<Component> getAllWithin(Bounds bds, Graphics g) {
        HashSet<Component> ret = new HashSet<Component>();
        for (Component comp : getComponents()) {
            if (bds.contains(comp.getBounds(g))) ret.add(comp);
        }
        return ret;
    }
    
    public WireSet getWireSet(Wire start) {
        return wires.getWireSet(start);
    }

    public Bounds getBounds() {
        Iterator<Component> it = comps.iterator();
        if (!it.hasNext()) return wires.getWireBounds();
        Component first = it.next();
        Bounds firstBounds = first.getBounds();
        int xMin = firstBounds.getX();
        int yMin = firstBounds.getY();
        int xMax = xMin + firstBounds.getWidth();
        int yMax = yMin + firstBounds.getHeight();
        while (it.hasNext()) {
            Component c = it.next();
            Bounds bds = c.getBounds();
            int x0 = bds.getX(); int x1 = x0 + bds.getWidth();
            int y0 = bds.getY(); int y1 = y0 + bds.getHeight();
            if (x0 < xMin) xMin = x0;
            if (x1 > xMax) xMax = x1;
            if (y0 < yMin) yMin = y0;
            if (y1 > yMax) yMax = y1;
        }
        return Bounds.create(xMin, yMin, xMax - xMin, yMax - yMin)
            .add(wires.getWireBounds());
    }

    public Bounds getBounds(Graphics g) {
        Bounds ret = wires.getWireBounds();
        int xMin = ret.getX();
        int yMin = ret.getY();
        int xMax = xMin + ret.getWidth();
        int yMax = yMin + ret.getHeight();
        for (Component c : comps) {
            Bounds bds = c.getBounds(g);
            int x0 = bds.getX(); int x1 = x0 + bds.getWidth();
            int y0 = bds.getY(); int y1 = y0 + bds.getHeight();
            if (x0 < xMin) xMin = x0;
            if (x1 > xMax) xMax = x1;
            if (y0 < yMin) yMin = y0;
            if (y1 > yMax) yMax = y1;
        }
        return Bounds.create(xMin, yMin, xMax - xMin, yMax - yMin);
    }

    ArrayList<Component> getClocks() {
        return clocks;
    }

    //
    // action methods
    //
    public void setName(String name) {
        staticAttrs.setValue(CircuitAttributes.NAME_ATTR, name);
    }

    void mutatorAdd(Component c) {
        locker.checkForWritePermission("add");

        if (c instanceof Wire) {
            Wire w = (Wire) c;
            if (w.getEnd0().equals(w.getEnd1())) return;
            boolean added = wires.add(w);
            if (!added) return;
        } else {
            // add it into the circuit
            boolean added = comps.add(c);
            if (!added) return;

            wires.add(c);
            ComponentFactory factory = c.getFactory();
            if (factory instanceof Pin) {
                pins.addPin(c);
            } else if (factory instanceof Clock) {
                clocks.add(c);
            } else if (factory instanceof Circuit) {
                Circuit subcirc = (Circuit) factory;
                subcirc.circuitsUsingThis.put((Subcircuit) c, this);
            }
            c.addComponentListener(myComponentListener);
        }
        fireEvent(CircuitEvent.ACTION_ADD, c);
    }

    void mutatorRemove(Component c) {
        locker.checkForWritePermission("remove");

        if (c instanceof Wire) {
            wires.remove(c);
        } else {
            wires.remove(c);
            comps.remove(c);
            ComponentFactory factory = c.getFactory();
            if (factory instanceof Pin) {
                pins.removePin(c);
            } else if (factory instanceof Clock) {
                clocks.remove(c);
            } else if (factory instanceof Circuit) {
                Circuit subcirc = (Circuit) factory;
                subcirc.circuitsUsingThis.remove(c);
            }
            c.removeComponentListener(myComponentListener);
        }
        fireEvent(CircuitEvent.ACTION_REMOVE, c);
    }

    //
    // Graphics methods
    //
    public void draw(ComponentDrawContext context, Collection<Component> hidden) {
        Graphics g = context.getGraphics();
        Graphics g_copy = g.create();
        context.setGraphics(g_copy);
        wires.draw(context, hidden);

        if (hidden == null || hidden.size() == 0) {
            for (Component c : comps) {
                Graphics g_new = g.create();
                context.setGraphics(g_new);
                g_copy.dispose();
                g_copy = g_new;

                c.draw(context);
            }
        } else {
            for (Component c : comps) {
                if (!hidden.contains(c)) {
                    Graphics g_new = g.create();
                    context.setGraphics(g_new);
                    g_copy.dispose();
                    g_copy = g_new;

                    c.draw(context);
                }
            }
        }
        context.setGraphics(g);
        g_copy.dispose();
    }

    //
    // ComponentFactory methods
    //
    @Override
    public String getName() {
        return staticAttrs.getValue(CircuitAttributes.NAME_ATTR);
    }

    @Override
    public StringGetter getDisplayGetter() {
        String name = staticAttrs.getValue(CircuitAttributes.NAME_ATTR);
        return StringUtil.constantGetter(name);
    }

    @Override
    public Component createComponent(Location loc, AttributeSet attrs) {
        return new Subcircuit(loc, this, attrs);
    }

    @Override
    public Bounds getOffsetBounds(AttributeSet attrs) {
        return pins.getOffsetBounds(attrs);
    }

    @Override
    public AttributeSet createAttributeSet() {
        return new CircuitAttributes(this);
    }
    
    @Override
    public Object getFeature(Object key, AttributeSet attrs) {
        if (key == FACING_ATTRIBUTE_KEY) return StdAttr.FACING;
        return super.getFeature(key, attrs);
    }
    
    @Override
    public void drawGhost(ComponentDrawContext context, Color color, int x,
            int y, AttributeSet attrs) {
        super.drawGhost(context, color, x, y, attrs);
        
        Graphics g = context.getGraphics();
        Bounds bds = getOffsetBounds(attrs).translate(x, y);
        GraphicsUtil.switchToWidth(g, 2);
        Direction facing = attrs.getValue(StdAttr.FACING);
        int ax;
        int ay;
        int an;
        if (facing == Direction.SOUTH) {
            ax = bds.getX() + bds.getWidth() - 1;
            ay = bds.getY() + bds.getHeight() / 2;
            an = 90;
        } else if (facing == Direction.NORTH) {
            ax = bds.getX() + 1;
            ay = bds.getY() + bds.getHeight() / 2;
            an = -90;
        } else if (facing == Direction.WEST) {
            ax = bds.getX() + bds.getWidth() / 2;
            ay = bds.getY() + bds.getHeight() - 1;
            an = 0;
        } else {
            ax = bds.getX() + bds.getWidth() / 2;
            ay = bds.getY() + 1;
            an = 180;
        }
        g.drawArc(ax - 4, ay - 4, 8, 8, an, 180);
        g.setColor(Color.BLACK);
    }

    //
    // helper methods for other classes in package
    //
    void configureComponent(Subcircuit comp) {
        // for Subcircuit to get the pins on the subcircuit configured
        pins.configureComponent(comp);
    }

    public static boolean isInput(Component comp) {
        return comp.getEnd(0).getType() != EndData.INPUT_ONLY;
    }
}
