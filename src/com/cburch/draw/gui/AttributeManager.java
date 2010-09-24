/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.cburch.draw.actions.ModelChangeAttributeAction;
import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.canvas.SelectionEvent;
import com.cburch.draw.canvas.SelectionListener;
import com.cburch.draw.model.AttributeMapKey;
import com.cburch.draw.model.CanvasModel;
import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.tools.AbstractTool;
import com.cburch.draw.tools.DrawingAttributeSet;
import com.cburch.draw.tools.SelectTool;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.AttributeSets;
import com.cburch.logisim.gui.generic.AttributeTable;
import com.cburch.logisim.gui.generic.AttributeTableListener;

public class AttributeManager
		implements AttributeTableListener, SelectionListener, AttributeListener,
			PropertyChangeListener {
	private Canvas canvas;
	private AttributeTable table;
	private DrawingAttributeSet generalAttrs;
	private AttributeSet attrs;
	private Map<AttributeSet, CanvasObject> selected;
	private AttributeSet selectionAttrs;
	
	public AttributeManager(Canvas canvas, AttributeTable table, DrawingAttributeSet attrs) {
		this.canvas = canvas;
		this.table = table;
		this.generalAttrs = attrs;
		this.generalAttrs.addAttributeListener(this);
		this.selected = Collections.emptyMap();
		
		canvas.getSelection().addSelectionListener(this);
		canvas.addPropertyChangeListener(Canvas.TOOL_PROPERTY, this);
		updateToolAttributes();
	}
	
	public void attributesSelected() {
		updateToolAttributes();
	}

	public void valueChangeRequested(AttributeTable table, AttributeSet attrs,
			Attribute<?> attr, Object value) {
		if (attrs == generalAttrs) {
			@SuppressWarnings("unchecked")
			Attribute<Object> a = (Attribute<Object>) attr;
			attrs.setValue(a, value);
		} else if (attrs == this.attrs) {
			setSelectedValues(attr, value);
		}
	}
	
	public void selectionChanged(SelectionEvent e) {
		Map<AttributeSet, CanvasObject> oldSel = selected;
		Map<AttributeSet, CanvasObject> newSel = new HashMap<AttributeSet, CanvasObject>();
		for (CanvasObject o : e.getSelection().getSelected()) {
			newSel.put(o.getAttributeSet(), o);
		}
		selected = newSel;
		for (AttributeSet attrs : oldSel.keySet()) {
			attrs.removeAttributeListener(this);
		}
		for (AttributeSet attrs : newSel.keySet()) {
			attrs.addAttributeListener(this);
		}
		computeAttributeList(newSel.keySet());
	}
	
	private void computeAttributeList(Set<AttributeSet> attrsSet) {
		Set<Attribute<?>> attrSet = new LinkedHashSet<Attribute<?>>();
		Iterator<AttributeSet> sit = attrsSet.iterator();
		if (sit.hasNext()) {
			AttributeSet first = sit.next();
			attrSet.addAll(first.getAttributes());
			while (sit.hasNext()) {
				AttributeSet next = sit.next();
				for (Iterator<Attribute<?>> ait = attrSet.iterator(); ait.hasNext(); ) {
					Attribute<?> attr = ait.next();
					if (!next.containsAttribute(attr)) {
						ait.remove();
					}
				}
			}
		}

		Attribute<?>[] attrs = new Attribute[attrSet.size()];
		Object[] values = new Object[attrs.length];
		int i = 0;
		for (Attribute<?> attr : attrSet) {
			attrs[i] = attr;
			values[i] = getSelectionValue(attr, attrsSet);
			i++;
		}
		selectionAttrs = AttributeSets.fixedSet(attrs, values);
		table.setAttributeSet(selectionAttrs, this);
		this.attrs = selectionAttrs;
	}
	
	private static Object getSelectionValue(Attribute<?> attr,
			Set<AttributeSet> sel) {
		Object ret = null;
		for (AttributeSet attrs : sel) {
			if (attrs.containsAttribute(attr)) {
				Object val = attrs.getValue(attr);
				if (ret == null) {
					ret = val;
				} else if (val != null && val.equals(ret)) {
					; // keep on, making sure everything else matches
				} else {
					return null;
				}
			}
		}
		return ret;
	}
	
	private void setSelectedValues(Attribute<?> attr, Object value) {
		HashMap<AttributeMapKey, Object> oldVals;
		oldVals = new HashMap<AttributeMapKey, Object>();
		HashMap<AttributeMapKey, Object> newVals;
		newVals = new HashMap<AttributeMapKey, Object>();
		for (Map.Entry<AttributeSet, CanvasObject> ent : selected.entrySet()) {
			AttributeMapKey key = new AttributeMapKey(attr, ent.getValue());
			oldVals.put(key, ent.getKey().getValue(attr));
			newVals.put(key, value);
		}
		CanvasModel model = canvas.getModel();
		canvas.doAction(new ModelChangeAttributeAction(model, oldVals, newVals));
	}

	public void attributeListChanged(AttributeEvent e) {
		if (table.getAttributeSet() == generalAttrs) {
			// showing tool attributes
			updateToolAttributes();
		} else {
			// show selection attributes
			computeAttributeList(selected.keySet());
		}
	}

	public void attributeValueChanged(AttributeEvent e) {
		if (selected.containsKey(e.getSource())) {
			@SuppressWarnings("unchecked")
			Attribute<Object> attr = (Attribute<Object>) e.getAttribute();
			Object value = getSelectionValue(attr, selected.keySet());
			selectionAttrs.setValue(attr, value);
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (prop.equals(Canvas.TOOL_PROPERTY)) {
			updateToolAttributes();
		}
	}
	
	private void updateToolAttributes() {
		Object tool = canvas.getTool();
		if (tool instanceof SelectTool) {
			computeAttributeList(selected.keySet());
		} else if (tool instanceof AbstractTool) {
			generalAttrs.setAttributes(((AbstractTool) tool).getAttributes());
			table.setAttributeSet(generalAttrs, this);
		} else {
			table.setAttributeSet(null, null);
		}
	}
}
