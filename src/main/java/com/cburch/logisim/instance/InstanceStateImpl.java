/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.instance;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.proj.Project;

class InstanceStateImpl implements InstanceState {
    private CircuitState circuitState;
    private Component component;

    public InstanceStateImpl(CircuitState circuitState, Component component) {
        this.circuitState = circuitState;
        this.component = component;
    }

    public void repurpose(CircuitState circuitState, Component component) {
        this.circuitState = circuitState;
        this.component = component;
    }

    CircuitState getCircuitState() {
        return circuitState;
    }

    @Override
    public Project getProject() {
        return circuitState.getProject();
    }

    @Override
    public Instance getInstance() {
        if (component instanceof InstanceComponent) {
            return ((InstanceComponent) component).getInstance();
        } else {
            return null;
        }
    }

    @Override
    public InstanceFactory getFactory() {
        if (component instanceof InstanceComponent) {
            InstanceComponent comp = (InstanceComponent) component;
            return (InstanceFactory) comp.getFactory();
        } else {
            return null;
        }
    }

    @Override
    public AttributeSet getAttributeSet() {
        return component.getAttributeSet();
    }

    @Override
    public <E> E getAttributeValue(Attribute<E> attr) {
        return component.getAttributeSet().getValue(attr);
    }

    @Override
    public Value getPort(int portIndex) {
        EndData data = component.getEnd(portIndex);
        return circuitState.getValue(data.getLocation());
    }

    @Override
    public boolean isPortConnected(int index) {
        Circuit circ = circuitState.getCircuit();
        Location loc = component.getEnd(index).getLocation();
        return circ.isConnected(loc, component);
    }

    @Override
    public void setPort(int portIndex, Value value, int delay) {
        EndData end = component.getEnd(portIndex);
        circuitState.setValue(end.getLocation(), value, component, delay);
    }

    @Override
    public InstanceData getData() {
        InstanceData ret = (InstanceData) circuitState.getData(component);
        return ret;
    }

    @Override
    public void setData(InstanceData value) {
        circuitState.setData(component, value);
    }

    @Override
    public void fireInvalidated() {
        if (component instanceof InstanceComponent) {
            ((InstanceComponent) component).fireInvalidated();
        }
    }

    @Override
    public boolean isCircuitRoot() {
        return !circuitState.isSubstate();
    }

    @Override
    public long getTickCount() {
        return circuitState.getPropagator().getTickCount();
    }
}
