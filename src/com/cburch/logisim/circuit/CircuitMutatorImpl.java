/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;

class CircuitMutatorImpl implements CircuitMutator {
	private ArrayList<CircuitChange> log;
	private HashMap<Circuit,ReplacementMap> replacements;
	private HashSet<Circuit> modified;
	
	public CircuitMutatorImpl() {
		log = new ArrayList<CircuitChange>();
		replacements = new HashMap<Circuit,ReplacementMap>();
		modified = new HashSet<Circuit>();
	}
	
	public void clear(Circuit circuit) {
		HashSet<Component> comps = new HashSet<Component>(circuit.getNonWires());
		comps.addAll(circuit.getWires());
		if (!comps.isEmpty()) modified.add(circuit);
		log.add(CircuitChange.clear(circuit, comps));

		ReplacementMap repl = new ReplacementMap();
		for (Component comp : comps) repl.remove(comp);
		getMap(circuit).append(repl);
		
		circuit.mutatorClear();
	}
	
	public void add(Circuit circuit, Component comp) {
		modified.add(circuit);
		log.add(CircuitChange.add(circuit, comp));
		
		ReplacementMap repl = new ReplacementMap();
		repl.add(comp);
		getMap(circuit).append(repl);
		
		circuit.mutatorAdd(comp);
	}
	
	public void remove(Circuit circuit, Component comp) {
		if (circuit.contains(comp)) {
			modified.add(circuit);
			log.add(CircuitChange.remove(circuit, comp));
	
			ReplacementMap repl = new ReplacementMap();
			repl.remove(comp);
			getMap(circuit).append(repl);
			
			circuit.mutatorRemove(comp);
		}
	}
	
	public void replace(Circuit circuit, Component prev, Component next) {
		replace(circuit, new ReplacementMap(prev, next));
	}
	
	public void replace(Circuit circuit, ReplacementMap repl) {
		if (!repl.isEmpty()) {
			modified.add(circuit);
			log.add(CircuitChange.replace(circuit, repl));
	
			repl.freeze();
			getMap(circuit).append(repl);
	
			for (Component c : repl.getRemovals()) {
				circuit.mutatorRemove(c);
			}
			for (Component c : repl.getAdditions()) {
				circuit.mutatorAdd(c);
			}
		}
	}
	
	public void set(Circuit circuit, Component comp, Attribute<?> attr,
			Object newValue) {
		if (circuit.contains(comp)) {
			modified.add(circuit);
			@SuppressWarnings("unchecked")
			Attribute<Object> a = (Attribute<Object>) attr;
			AttributeSet attrs = comp.getAttributeSet();
			Object oldValue = attrs.getValue(a);
			log.add(CircuitChange.set(circuit, comp, attr, oldValue, newValue));
			attrs.setValue(a, newValue);
		}
	}
	
	public void setForCircuit(Circuit circuit, Attribute<?> attr,
			Object newValue) {
		@SuppressWarnings("unchecked")
		Attribute<Object> a = (Attribute<Object>) attr;
		AttributeSet attrs = circuit.getStaticAttributes();
		Object oldValue = attrs.getValue(a);
		log.add(CircuitChange.setForCircuit(circuit, attr, oldValue, newValue));
		attrs.setValue(a, newValue);
	}
	
	private ReplacementMap getMap(Circuit circuit) {
		ReplacementMap ret = replacements.get(circuit);
		if (ret == null) {
			ret = new ReplacementMap();
			replacements.put(circuit, ret);
		}
		return ret;
	}
	
	CircuitTransaction getReverseTransaction() {
		CircuitMutation ret = new CircuitMutation();
		ArrayList<CircuitChange> log = this.log;
		for (int i = log.size() - 1; i >= 0; i--) {
			ret.change(log.get(i).getReverseChange());
		}
		return ret;
	}
	
	ReplacementMap getReplacementMap(Circuit circuit) {
		return replacements.get(circuit);
	}
	
	void markModified(Circuit circuit) {
		modified.add(circuit);
	}
	
	Collection<Circuit> getModifiedCircuits() {
		return Collections.unmodifiableSet(modified);
	}
}
