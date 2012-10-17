/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.instance;

import java.awt.Graphics;

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
	
	public boolean isCircuitRoot() {
		return !context.getCircuitState().isSubstate();
	}
	
	public long getTickCount() {
		return context.getCircuitState().getPropagator().getTickCount();
	}
	
	//
	// methods related to the circuit state
	//
	public Project getProject() {
		return context.getCircuitState().getProject();
	}
	
	public Value getPort(int portIndex) {
		InstanceComponent c = comp;
		CircuitState s = context.getCircuitState();
		if (c != null && s != null) {
			return s.getValue(c.getEnd(portIndex).getLocation());
		} else {
			return Value.UNKNOWN;
		}
	}
	
	public void setPort(int portIndex, Value value, int delay) {
		throw new UnsupportedOperationException("setValue on InstancePainter");
	}
	
	public InstanceData getData() {
		CircuitState circState = context.getCircuitState();
		if (circState == null || comp == null) {
			throw new UnsupportedOperationException("setData on InstancePainter");
		} else {
			return (InstanceData) circState.getData(comp);
		}
	}
	
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
	public Instance getInstance() {
		InstanceComponent c = comp;
		return c == null ? null : c.getInstance();
	}
	
	public Location getLocation() {
		InstanceComponent c = comp;
		return c == null ? Location.create(0, 0) : c.getLocation();
	}
	
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
	
	public AttributeSet getAttributeSet() {
		InstanceComponent c = comp;
		return c == null ? attrs : c.getAttributeSet();
	}
	
	public <E> E getAttributeValue(Attribute<E> attr) {
		InstanceComponent c = comp;
		AttributeSet as = c == null ? attrs : c.getAttributeSet();
		return as.getValue(attr);
	}
	
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
}
