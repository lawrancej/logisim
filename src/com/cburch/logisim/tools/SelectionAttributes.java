/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.AbstractAttributeSet;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.gui.main.AttributeTable;
import com.cburch.logisim.gui.main.AttributeTableListener;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.gui.main.CircuitAttributeListener;
import com.cburch.logisim.gui.main.Selection;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.util.UnmodifiableList;

class SelectionAttributes extends AbstractAttributeSet
		implements AttributeTableListener {
	private static final Attribute<?>[] EMPTY_ATTRIBUTES = new Attribute<?>[0];
	private static final Object[] EMPTY_VALUES = new Object[0];
	
	private class Listener implements Selection.Listener, AttributeListener {
		public void selectionChanged(Selection.Event e) {
			updateList(true);
		}

		public void attributeListChanged(AttributeEvent e) {
			if (listening) updateList(false);
		}

		public void attributeValueChanged(AttributeEvent e) {
			if (listening) updateList(false);
		}
	}
	
	private Listener listener;
	private boolean listening;
	private Project project;
	private Circuit circuit;
	private Selection oldSelection;
	private Set<Component> selected;
	private Attribute<?>[] attrs;
	private boolean[] readOnly;
	private Object[] values;
	private List<Attribute<?>> attrsView;
	
	public SelectionAttributes() {
		this.listener = new Listener();
		this.listening = true;
		this.oldSelection = null;
		this.selected = Collections.emptySet();
		this.attrs = EMPTY_ATTRIBUTES;
		this.values = EMPTY_VALUES;
		this.attrsView = Collections.emptyList();
	}
	
	void setCanvas(Canvas value) {
		Selection oldSel = oldSelection;
		Selection newSel = value.getSelection();
		project = value.getProject();
		circuit = value.getCircuit();
		if (newSel != oldSel) {
			if (oldSel != null) oldSel.removeListener(listener);
			oldSelection = newSel;
			if (newSel != null) newSel.addListener(listener);
			updateList(true);
		}
	}
	
	void setListening(boolean value) {
		if (listening != value) {
			listening = value;
			if (value) updateList(false);
		}
	}
	
	private void updateList(boolean ignoreIfSelectionSame) {
		Selection selection = oldSelection;
		Set<Component> oldSel = selected;
		Set<Component> newSel;
		if (selection == null) newSel = Collections.emptySet();
		else newSel = createSet(selection.getComponents());
		if (haveSameElements(newSel, oldSel)) {
			if (ignoreIfSelectionSame) return;
			newSel = oldSel;
		} else {
			for (Component o : oldSel) {
				if (!newSel.contains(o)) {
					o.getAttributeSet().removeAttributeListener(listener);
				}
			}
			for (Component o : newSel) {
				if (!oldSel.contains(o)) {
					o.getAttributeSet().addAttributeListener(listener);
				}
			}
		}
		
		LinkedHashMap<Attribute<Object>,Object> attrMap = computeAttributes(newSel);
		boolean same = isSame(attrMap, this.attrs, this.values);
		
		if (same) {
			if (newSel != oldSel) this.selected = newSel;
		} else {
			Attribute<?>[] newAttrs = new Attribute[attrMap.size()];
			Object[] newValues = new Object[newAttrs.length];
			boolean[] newReadOnly = new boolean[newAttrs.length];
			int i = -1;
			for (Map.Entry<Attribute<Object>,Object> entry : attrMap.entrySet()) {
				i++;
				newAttrs[i] = entry.getKey();
				newValues[i] = entry.getValue();
				newReadOnly[i] = computeReadOnly(newSel, newAttrs[i]);
			}
			if (newSel != oldSel) this.selected = newSel;
			this.attrs = newAttrs;
			this.attrsView = new UnmodifiableList<Attribute<?>>(newAttrs);
			this.values = newValues;
			this.readOnly = newReadOnly;
			fireAttributeListChanged();
		}
	}
	
	private static Set<Component> createSet(Collection<Component> comps) {
		boolean includeWires = true;
		for (Component comp : comps) {
			if (!(comp instanceof Wire)) { includeWires = false; break; }
		}
		
		if (includeWires) {
			return new HashSet<Component>(comps);
		} else {
			HashSet<Component> ret = new HashSet<Component>();
			for (Component comp : comps) {
				if (!(comp instanceof Wire)) ret.add(comp);
			}
			return ret;
		}
	}

	private static boolean haveSameElements(Collection<Component> a, Collection<Component> b) {
		if (a == null) {
			return b == null ? true : b.size() == 0;
		} else if (b == null) {
			return a.size() == 0;
		} else if (a.size() != b.size()) {
			return false;
		} else {
			for (Component item : a) {
				if (!b.contains(item)) return false;
			}
			return true;
		}
	}
	
	private static LinkedHashMap<Attribute<Object>,Object> computeAttributes(Collection<Component> newSel)  {
		LinkedHashMap<Attribute<Object>,Object> attrMap;
		attrMap = new LinkedHashMap<Attribute<Object>,Object>();
		Iterator<Component> sit = newSel.iterator();
		if (sit.hasNext()) {
			AttributeSet first = sit.next().getAttributeSet();
			for (Attribute<?> attr : first.getAttributes()) {
				@SuppressWarnings("unchecked")
				Attribute<Object> attrObj = (Attribute<Object>) attr;
				attrMap.put(attrObj, first.getValue(attr));
			}
			while (sit.hasNext()) {
				AttributeSet next = sit.next().getAttributeSet();
				Iterator<Attribute<Object>> ait = attrMap.keySet().iterator();
				while (ait.hasNext()) {
					Attribute<Object> attr = ait.next();
					if (next.containsAttribute(attr)) {
						Object v = attrMap.get(attr);
						if (v != null && !v.equals(next.getValue(attr))) {
						    attrMap.put(attr, null);
						}
					} else {
						ait.remove();
					}
				}
			}
		}
		return attrMap;
	}
	
	private static boolean isSame(LinkedHashMap<Attribute<Object>,Object> attrMap,
			Attribute<?>[] oldAttrs, Object[] oldValues) {
		if (oldAttrs.length != attrMap.size()) {
			return false;
		} else {
			int j = -1;
			for (Map.Entry<Attribute<Object>,Object> entry : attrMap.entrySet()) {
				j++;

				Attribute<Object> a = entry.getKey();
				if (oldAttrs[j] != a || j >= oldValues.length) return false;
				Object ov = oldValues[j];
				Object nv = entry.getValue();
				if (ov == null ? nv != null : !ov.equals(nv)) return false;
			}
			return true;
		}
	}
	
	private static boolean computeReadOnly(Collection<Component> sel, Attribute<?> attr) {
		for (Component comp : sel) {
			AttributeSet attrs = comp.getAttributeSet();
			if (attrs.isReadOnly(attr)) return true;
		}
		return false;
	}

	@Override
	protected void copyInto(AbstractAttributeSet dest) {
		throw new UnsupportedOperationException("SelectionAttributes.copyInto");
	}

	@Override
	public List<Attribute<?>> getAttributes() {
		if (selected.isEmpty() && circuit != null) {
			return circuit.getStaticAttributes().getAttributes();
		} else {
			return attrsView;
		}
	}
	
	@Override
	public boolean isReadOnly(Attribute<?> attr) {
		if (!project.getLogisimFile().contains(circuit)) return true;
		if (selected.isEmpty() && circuit != null) {
			return circuit.getStaticAttributes().isReadOnly(attr);
		} else {
			int i = findIndex(attr);
			boolean[] ro = readOnly;
			return i >= 0 && i < ro.length ? ro[i] : true;
		}
	}
	
	@Override
	public boolean isToSave(Attribute<?> attr) {
		return false;
	}

	@Override
	public <V> V getValue(Attribute<V> attr) {
		if (selected.isEmpty() && circuit != null) {
			return circuit.getStaticAttributes().getValue(attr);
		} else {
			int i = findIndex(attr);
			Object[] vs = values;
			@SuppressWarnings("unchecked")
			V ret = (V) (i >= 0 && i < vs.length ? vs[i] : null);
			return ret;
		}
	}

	@Override
	public <V> void setValue(Attribute<V> attr, V value) {
		if (selected.isEmpty() && circuit != null) {
			circuit.getStaticAttributes().setValue(attr, value);
		} else {
			int i = findIndex(attr);
			Object[] vs = values;
			if (i >= 0 && i < vs.length) {
				vs[i] = value;
				for (Component comp : selected) {
					comp.getAttributeSet().setValue(attr, value);
				}
			}
		}
	}
	
	private int findIndex(Attribute<?> attr) {
		Attribute<?>[] as = attrs;
		for (int i = 0; i < as.length; i++) {
			if (attr == as[i]) return i;
		}
		return -1;
	}

	public void valueChangeRequested(AttributeTable table, AttributeSet attrs,
			Attribute<?> attr, Object value) {
		if (selected.isEmpty() && circuit != null) {
			CircuitAttributeListener list = new CircuitAttributeListener(project, circuit);
			list.valueChangeRequested(table, attrs, attr, value);
		} else {
			SetAttributeAction act = new SetAttributeAction(circuit,
					Strings.getter("selectionAttributeAction"));
			for (Component comp : selected) {
				act.set(comp, attr, value);
			}
			project.doAction(act);
		}
	}
}
