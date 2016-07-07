/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.instance;

import java.awt.*;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.WireSet;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.gates.AbstractGate;
import com.cburch.logisim.std.gates.GateAttributes;

public class InstancePainter implements InstanceState {
    private ComponentDrawContext context;
    private InstanceComponent comp;
    private InstanceFactory factory;
    private AttributeSet attrs;

    public InstancePainter(ComponentDrawContext context,
            InstanceComponent instance) {
        this.context = context;
        this.comp = instance;
    }

    void setInstance(InstanceComponent value) {
        this.comp = value;
    }

    void setFactory(InstanceFactory factory, AttributeSet attrs) {
        this.comp = null;
        this.factory = factory;
        this.attrs = attrs;
    }

    @Override
    public InstanceFactory getFactory() {
        return comp == null ? factory : (InstanceFactory) comp.getFactory();
    }

    //
    // methods related to the context of the canvas
    //
    public WireSet getHighlightedWires() {
        return context.getHighlightedWires();
    }

    public boolean getShowState() {
        return context.getShowState();
    }

    public boolean isPrintView() {
        return context.isPrintView();
    }

    public boolean shouldDrawColor() {
        return context.shouldDrawColor();
    }

    public java.awt.Component getDestination() {
        return context.getDestination();
    }

    public Graphics getGraphics() {
        return context.getGraphics();
    }

    public Circuit getCircuit() {
        return context.getCircuit();
    }

    public Object getGateShape() {
        return context.getGateShape();
    }

    @Override
    public boolean isCircuitRoot() {
        return !context.getCircuitState().isSubstate();
    }

    @Override
    public long getTickCount() {
        return context.getCircuitState().getPropagator().getTickCount();
    }

    //
    // methods related to the circuit state
    //
    @Override
    public Project getProject() {
        return context.getCircuitState().getProject();
    }

    @Override
    public Value getPort(int portIndex) {
        InstanceComponent c = comp;
        CircuitState s = context.getCircuitState();
        if (c != null && s != null) {
            return s.getValue(c.getEnd(portIndex).getLocation());
        } else {
            return Value.UNKNOWN;
        }
    }

    @Override
    public void setPort(int portIndex, Value value, int delay) {
        throw new UnsupportedOperationException("setValue on InstancePainter");
    }

    @Override
    public InstanceData getData() {
        CircuitState circState = context.getCircuitState();
        if (circState == null || comp == null) {
            throw new UnsupportedOperationException("setData on InstancePainter");
        } else {
            return (InstanceData) circState.getData(comp);
        }
    }

    @Override
    public void setData(InstanceData value) {
        CircuitState circState = context.getCircuitState();
        if (circState == null || comp == null) {
            throw new UnsupportedOperationException("setData on InstancePainter");
        } else {
            circState.setData(comp, value);
        }
    }

    //
    // methods related to the instance
    //
    @Override
    public Instance getInstance() {
        InstanceComponent c = comp;
        return c == null ? null : c.getInstance();
    }

    public Location getLocation() {
        InstanceComponent c = comp;
        return c == null ? Location.create(0, 0) : c.getLocation();
    }

    @Override
    public boolean isPortConnected(int index) {
        Circuit circ = context.getCircuit();
        Location loc = comp.getEnd(index).getLocation();
        return circ.isConnected(loc, comp);
    }

    public Bounds getOffsetBounds() {
        InstanceComponent c = comp;
        if (c == null) {
            return factory.getOffsetBounds(attrs);
        } else {
            Location loc = c.getLocation();
            return c.getBounds().translate(-loc.getX(), -loc.getY());
        }
    }

    public Bounds getBounds() {
        InstanceComponent c = comp;
        return c == null ? factory.getOffsetBounds(attrs) : c.getBounds();
    }

    @Override
    public AttributeSet getAttributeSet() {
        InstanceComponent c = comp;
        return c == null ? attrs : c.getAttributeSet();
    }

    @Override
    public <E> E getAttributeValue(Attribute<E> attr) {
        InstanceComponent c = comp;
        AttributeSet as = c == null ? attrs : c.getAttributeSet();
        return as.getValue(attr);
    }

    @Override
    public void fireInvalidated() {
        comp.fireInvalidated();
    }

    //
    // helper methods for drawing common elements in components
    //
    public void drawBounds() {
        context.drawBounds(comp);
    }

    public void drawRectangle(Bounds bds, String label) {
        context.drawRectangle(bds.getX(), bds.getY(),
                bds.getWidth(), bds.getHeight(), label);
    }

    public void drawRectangle(int x, int y,
            int width, int height, String label) {
        context.drawRectangle(x, y, width, height, label);
    }

    public void drawDongle(int x, int y) {
        context.drawDongle(x, y);
    }

    public void drawPort(int i) {
        context.drawPin(comp, i);
    }

    public void drawPort(int i, String label, Direction dir) {
        context.drawPin(comp, i, label, dir);
    }

    public void drawPorts() {
        context.drawPins(comp);
    }

    public void drawClock(int i, Direction dir) {
        context.drawClock(comp, i, dir);
    }

    public void drawHandles() {
        context.drawHandles(comp);
    }

    public void drawHandle(Location loc) {
        context.drawHandle(loc);
    }

    public void drawHandle(int x, int y) {
        context.drawHandle(x, y);
    }

    public void drawLabel() {
        if (comp != null) {
            comp.drawLabel(context);
        }
    }

    public void drawNegatedInput(Direction facing, Location inputLocation) {
        Location center = inputLocation.translate(facing, 5);
        drawDongle(getLocation().getX() + center.getX(),
                getLocation().getY() + center.getY());
    }

    public void paintRectangular(int width, int height, AbstractGate gate) {
        int don = gate.isNegateOutput() ? 10 : 0;
        drawRectangle(-width, -height / 2, width - don, height,
                gate.getRectangularLabel(getAttributeSet()));
        if (gate.isNegateOutput()) {
            drawDongle(-5, 0);
        }
    }

    public void paintNegatedInputs(AbstractGate gate) {
        for (int i = 0; i < ((GateAttributes) getAttributeSet()).inputs; i++) {
            int negatedBit = (((GateAttributes) getAttributeSet()).negated >> i) & 1;
            if (negatedBit == 1) {
                drawNegatedInput(((GateAttributes) getAttributeSet()).facing, gate.getInputOffset((GateAttributes) getAttributeSet(), i));
            }
        }
    }

    public void moveToLocation() {
        getGraphics().translate(getLocation().getX(), getLocation().getY());
    }

    public void setBaseColor(Color baseColor) {
        getGraphics().setColor(baseColor);
    }

    public double rotate() {
        double rotate = 0.0;
        if (((GateAttributes) getAttributeSet()).facing != Direction.EAST && getGraphics() instanceof Graphics2D) {
            rotate = -((GateAttributes) getAttributeSet()).facing.toRadians();
            Graphics2D g2 = (Graphics2D) getGraphics();
            g2.rotate(rotate);
        }
        return rotate;
    }
}
