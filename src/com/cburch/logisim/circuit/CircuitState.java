/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.cburch.logisim.circuit.Propagator.SetData;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentState;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.base.Clock;
import com.cburch.logisim.std.base.Pin;
import com.cburch.logisim.util.ArraySet;
import com.cburch.logisim.util.SmallSet;

public class CircuitState {
	private class MyCircuitListener implements CircuitListener {
		public void circuitChanged(CircuitEvent event) {
			int action = event.getAction();
			if (action == CircuitEvent.ACTION_ADD) {
				Component comp = (Component) event.getData();
				if (comp instanceof Wire) {
					Wire w = (Wire) comp;
					markPointAsDirty(w.getEnd0());
					markPointAsDirty(w.getEnd1());
				} else {
					markComponentAsDirty(comp);
				}
			} else if (action == CircuitEvent.ACTION_REMOVE) {
				Component comp = (Component) event.getData();
				if (comp instanceof Subcircuit) {
					// disconnect from tree
					CircuitState substate = (CircuitState) getData(comp);
					if (substate != null && substate.parentComp == comp) {
						substates.remove(substate);
						substate.parentState = null;
						substate.parentComp = null;
					}
				}

				if (comp instanceof Wire) {
					Wire w = (Wire) comp;
					markPointAsDirty(w.getEnd0());
					markPointAsDirty(w.getEnd1());
				} else {
					if (base != null) base.checkComponentEnds(CircuitState.this, comp);
					dirtyComponents.remove(comp);
				}
			} else if (action == CircuitEvent.ACTION_CLEAR) {
				substates.clear();
				wireData = null;
				componentData.clear();
				values.clear();
				dirtyComponents.clear();
				dirtyPoints.clear();
				causes.clear();
			} else if (action == CircuitEvent.ACTION_CHANGE) {
				Object data = event.getData();
				if (data instanceof Collection) {
					@SuppressWarnings("unchecked")
					Collection<Component> comps = (Collection<Component>) data;
					markComponentsDirty(comps);
					if (base != null) {
						for (Component comp : comps) {
						    base.checkComponentEnds(CircuitState.this, comp);
						}
					}
				} else {
					Component comp = (Component) event.getData();
					markComponentAsDirty(comp);
					if (base != null) base.checkComponentEnds(CircuitState.this, comp);
				}
			} else if (action == CircuitEvent.ACTION_INVALIDATE) {
				Component comp = (Component) event.getData();
				markComponentAsDirty(comp);
				// TODO detemine if this should really be missing if (base != null) base.checkComponentEnds(CircuitState.this, comp);
			} else if (action == CircuitEvent.TRANSACTION_DONE) {
				ReplacementMap map = event.getResult().getReplacementMap(circuit);
				if (map != null) {
					for (Component comp : map.getReplacedComponents()) {
						Object compState = componentData.remove(comp);
						if (compState != null) {
						    Class<?> compFactory = comp.getFactory().getClass();
						    boolean found = false;
						    for (Component repl : map.get(comp)) {
						        if (repl.getFactory().getClass() == compFactory) {
						            found = true;
						            setData(repl, compState);
						            break;
						        }
						    }
						    if (!found && compState instanceof CircuitState) {
						        CircuitState sub = (CircuitState) compState;
						        sub.parentState = null;
						        substates.remove(sub);
						    }
						}
					}
				}
			}
		}
	}

	private MyCircuitListener myCircuitListener = new MyCircuitListener();
	private Propagator base = null; // base of tree of CircuitStates
	private Project proj; // project where circuit lies
	private Circuit circuit; // circuit being simulated

	private CircuitState parentState = null; // parent in tree of CircuitStates
	private Subcircuit parentComp = null; // subcircuit component containing this state
	private ArraySet<CircuitState> substates = new ArraySet<CircuitState>();

	private CircuitWires.State wireData = null;
	private HashMap<Component,Object> componentData = new HashMap<Component,Object>();
	private HashMap<Location,Value> values = new HashMap<Location,Value>();
	private SmallSet<Component> dirtyComponents = new SmallSet<Component>();
	private SmallSet<Location> dirtyPoints = new SmallSet<Location>();
	HashMap<Location,SetData> causes = new HashMap<Location,SetData>();

	private static int lastId = 0;
	private int id = lastId++;

	public CircuitState(Project proj, Circuit circuit) {
		this.proj = proj;
		this.circuit = circuit;
		circuit.addCircuitListener(myCircuitListener);
	}
	
	public Project getProject() {
		return proj;
	}
	
	Subcircuit getSubcircuit() {
		return parentComp;
	}
	
	public CircuitState cloneState() {
		CircuitState ret = new CircuitState(proj, circuit);
		ret.copyFrom(this, new Propagator(ret));
		ret.parentComp = null;
		ret.parentState = null;
		return ret;
	}
	
	private void copyFrom(CircuitState src, Propagator base) {
		this.base = base;
		this.parentComp = src.parentComp;
		this.parentState = src.parentState;
		HashMap<CircuitState,CircuitState> substateData = new HashMap<CircuitState,CircuitState>();
		this.substates = new ArraySet<CircuitState>();
		for (CircuitState oldSub : src.substates) {
			CircuitState newSub = new CircuitState(src.proj, oldSub.circuit);
			newSub.copyFrom(oldSub, base);
			newSub.parentState = this;
			this.substates.add(newSub);
			substateData.put(oldSub, newSub);
		}
		for (Component key : src.componentData.keySet()) {
			Object oldValue = src.componentData.get(key);
			if (oldValue instanceof CircuitState) {
				Object newValue = substateData.get(oldValue);
				if (newValue != null) this.componentData.put(key, newValue);
				else this.componentData.remove(key);
			} else {
				Object newValue;
				if (oldValue instanceof ComponentState) {
					newValue = ((ComponentState) oldValue).clone();
				} else {
					newValue = oldValue;
				}
				this.componentData.put(key, newValue);
			}
		}
		for (Location key : src.causes.keySet()) {
			Propagator.SetData oldValue = src.causes.get(key);
			Propagator.SetData newValue = oldValue.cloneFor(this);
			this.causes.put(key, newValue);
		}
		if (src.wireData != null) {
			this.wireData = (CircuitWires.State) src.wireData.clone();
		}
		this.values.putAll(src.values);
		this.dirtyComponents.addAll(src.dirtyComponents);
		this.dirtyPoints.addAll(src.dirtyPoints);
	}

	@Override
	public String toString() {
		return "State" + id + "[" + circuit.getName() + "]";
	}

	//
	// public methods
	//
	public Circuit getCircuit() {
		return circuit;
	}
	
	public CircuitState getParentState() {
		return parentState;
	}
	
	public Set<CircuitState> getSubstates() { // returns Set of CircuitStates
		return substates;
	}

	public Propagator getPropagator() {
		if (base == null) {
			base = new Propagator(this);
			markAllComponentsDirty();
		}
		return base;
	}
	
	public void drawOscillatingPoints(ComponentDrawContext context) {
		if (base != null) base.drawOscillatingPoints(context);
	}

	public Object getData(Component comp) {
		return componentData.get(comp);
	}

	public void setData(Component comp, Object data) {
		if (comp instanceof Subcircuit) {
			CircuitState oldState = (CircuitState) componentData.get(comp);
			CircuitState newState = (CircuitState) data;
			if (oldState != newState) {
				// There's something new going on with this subcircuit.
				// Maybe the subcircuit is new, or perhaps it's being
				// removed.
				if (oldState != null && oldState.parentComp == comp) {
					// it looks like it's being removed
					substates.remove(oldState);
					oldState.parentState = null;
					oldState.parentComp = null;
				}
				if (newState != null && newState.parentState != this) {
					// this is the first time I've heard about this CircuitState
					substates.add(newState);
					newState.base = this.base;
					newState.parentState = this;
					newState.parentComp = (Subcircuit) comp;
					newState.markAllComponentsDirty();
				}
			}
		}
		componentData.put(comp, data);
	}

	public Value getValue(Location pt) {
		Value ret = values.get(pt);
		if (ret != null) return ret;

		BitWidth wid = circuit.getWidth(pt);
		return Value.createUnknown(wid);
	}

	public void setValue(Location pt, Value val, Component cause, int delay) {
		if (base != null) base.setValue(this, pt, val, cause, delay);
	}

	public void markComponentAsDirty(Component comp) {
		dirtyComponents.add(comp);
	}

	public void markComponentsDirty(Collection<Component> comps) {
		dirtyComponents.addAll(comps);
	}

	public void markPointAsDirty(Location pt) {
		dirtyPoints.add(pt);
	}
	
	public InstanceState getInstanceState(Component comp) {
		Object factory = comp.getFactory();
		if (factory instanceof InstanceFactory) {
			return ((InstanceFactory) factory).createInstanceState(this, comp);
		} else {
			throw new RuntimeException("getInstanceState requires instance component");
		}
	}
	
	public InstanceState getInstanceState(Instance instance) {
		Object factory = instance.getFactory();
		if (factory instanceof InstanceFactory) {
			return ((InstanceFactory) factory).createInstanceState(this, instance);
		} else {
			throw new RuntimeException("getInstanceState requires instance component");
		}
	}

	//
	// methods for other classes within package
	//
	public boolean isSubstate() {
		return parentState != null;
	}

	void processDirtyComponents() {
		if (!dirtyComponents.isEmpty()) {
			// This seeming wasted copy is to avoid ConcurrentModifications
			// if we used an iterator instead.
			Component[] toProcess = dirtyComponents.toArray(new Component[0]);
			dirtyComponents.clear();
			for (Component comp : toProcess) {
				if (comp != null) {
					comp.propagate(this);
					if (comp.getFactory() instanceof Pin && parentState != null) {
						// should be propagated in superstate
						parentComp.propagate(parentState);
					}
				}
			}
		}

		CircuitState[] subs = new CircuitState[substates.size()];
		for (CircuitState substate : substates.toArray(subs)) {
			substate.processDirtyComponents();
		}
	}

	void processDirtyPoints() {
		if (!dirtyPoints.isEmpty()) {
			circuit.wires.propagate(this, dirtyPoints);
			dirtyPoints.clear();
		}

		CircuitState[] subs = new CircuitState[substates.size()];
		for (CircuitState substate : substates.toArray(subs)) {
			substate.processDirtyPoints();
		}
	}
	
	void reset() {
		wireData = null;
		for (Iterator<Component> it = componentData.keySet().iterator(); it.hasNext(); ) {
			Component comp = it.next();
			if (!(comp instanceof Subcircuit)) it.remove();
		}
		values.clear();
		dirtyComponents.clear();
		dirtyPoints.clear();
		causes.clear();
		markAllComponentsDirty();
		
		for (CircuitState sub : substates) {
			sub.reset();
		}
	}

	boolean tick(int ticks) {
		boolean ret = false;
		for (Component clock : circuit.getClocks()) {
			ret |= Clock.tick(this, ticks, clock);
		}

		CircuitState[] subs = new CircuitState[substates.size()];
		for (CircuitState substate : substates.toArray(subs)) {
			ret |= substate.tick(ticks);
		}
		return ret;
	}

	CircuitWires.State getWireData() {
		return wireData;
	}

	void setWireData(CircuitWires.State data) {
		wireData = data;
	}

	Value getComponentOutputAt(Location p) {
		// for CircuitWires - to get values, ignoring wires' contributions
		Propagator.SetData cause_list = causes.get(p);
		return Propagator.computeValue(cause_list);
	}

	Value getValueByWire(Location p) {
		return values.get(p);
	}

	void setValueByWire(Location p, Value v) {
		// for CircuitWires - to set value at point
		boolean changed;
		if (v == Value.NIL) {
			Object old = values.remove(p);
			changed = (old != null && old != Value.NIL);
		} else {
			Object old = values.put(p, v);
			changed = !v.equals(old);
		}
		if (changed) {
			boolean found = false;
			for (Component comp : circuit.getComponents(p)) {
				if (!(comp instanceof Wire) && !(comp instanceof Splitter)) {
					found = true;
					markComponentAsDirty(comp);
				}
			}
			// NOTE: this will cause a double-propagation on components
			// whose outputs have just changed.
			
			if (found && base != null) base.locationTouched(this, p);
		}
	}

	//
	// private methods
	// 
	private void markAllComponentsDirty() {
		dirtyComponents.addAll(circuit.getNonWires());
	}
}
