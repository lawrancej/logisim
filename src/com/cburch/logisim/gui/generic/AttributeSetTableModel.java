package com.cburch.logisim.gui.generic;

import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;

public abstract class AttributeSetTableModel
		implements AttrTableModel, AttributeListener {
	private class AttrRow implements AttrTableModelRow {
		private Attribute<Object> attr;
		
		AttrRow(Attribute<?> attr) {
			@SuppressWarnings("unchecked")
			Attribute<Object> objAttr = (Attribute<Object>) attr;
			this.attr = objAttr;
		}
		
		public String getLabel() {
			return attr.getDisplayName();
		}
		
		public String getValue() {
			return attr.toDisplayString(attrs.getValue(attr));
		}
		
		public boolean isValueEditable() {
			return !attrs.isReadOnly(attr);
		}
		
		public Component getEditor(Window parent) {
			Object value = attrs.getValue(attr);
			return attr.getCellEditor(parent, value);
		}
		
		public void setValue(Object value) throws AttrTableSetException {
			Attribute<Object> attr = this.attr;
			if (attr == null || value == null) return;
			
			try {
				if (value instanceof String) {
					value = attr.parse((String) value);
				}
				requestSetValue(attr, value);
			} catch (ClassCastException e) {
				String msg = Strings.get("attributeChangeInvalidError")
					+ ": " + e;
				throw new AttrTableSetException(msg);
			} catch (NumberFormatException e) {
				/*
				long now = System.currentTimeMillis();
				if (aValue.equals(lastValue) && now < lastUpdate + 500) {
					return;
				}
				lastValue = aValue;
				lastUpdate = System.currentTimeMillis();
				*/

				String msg = Strings.get("attributeChangeInvalidError");
				String emsg = e.getMessage();
				if (emsg != null && emsg.length() > 0) msg += ": " + emsg;
				msg += ".";
				throw new AttrTableSetException(msg);
			}
		}
	}
	
	private ArrayList<AttrTableModelListener> listeners;
	private AttributeSet attrs;
	private HashMap<Attribute<?>, AttrRow> rowMap;
	private ArrayList<AttrRow> rows;
	
	public AttributeSetTableModel(AttributeSet attrs) {
		this.attrs = attrs;
		this.listeners = new ArrayList<AttrTableModelListener>();
		this.rowMap = new HashMap<Attribute<?>, AttrRow>();
		this.rows = new ArrayList<AttrRow>();
		for (Attribute<?> attr : attrs.getAttributes()) {
			AttrRow row = new AttrRow(attr);
			rowMap.put(attr, row);
			rows.add(row);
		}
	}

	public void addAttrTableModelListener(AttrTableModelListener listener) {
		if (listeners.isEmpty()) {
			attrs.addAttributeListener(this);
		}
		listeners.add(listener);
	}

	public void removeAttrTableModelListener(AttrTableModelListener listener) {
		listeners.remove(listener);
		if (listeners.isEmpty()) {
			attrs.removeAttributeListener(this);
		}
	}

	public int getRowCount() {
		return rows.size();
	}

	public AttrTableModelRow getRow(int rowIndex) {
		return rows.get(rowIndex);
	}

	protected abstract void requestSetValue(Attribute<Object> attr, Object value);
	
	//
	// AttributeListener methods
	//
	public void attributeListChanged(AttributeEvent e) {
		// if anything has changed, don't do anything
		int index = 0;
		boolean match = true;
		for (Attribute<?> attr : attrs.getAttributes()) {
			if (rows.get(index).attr != attr) { match = false; break; }
			index++;
		}
		if (!match || index != rows.size()) return;
		
		// compute the new list of rows, possible adding into hash map
		ArrayList<AttrRow> newRows = new ArrayList<AttrRow>();
		HashSet<Attribute<?>> missing = new HashSet<Attribute<?>>(rowMap.keySet());
		for (Attribute<?> attr : attrs.getAttributes()) {
			AttrRow row = rowMap.get(attr);
			if (row == null) {
				row = new AttrRow(attr);
				rowMap.put(attr, row);
			} else {
				missing.remove(attr);
			}
			newRows.add(row);
		}
		rows = newRows;
		for (Attribute<?> attr : missing) {
			rowMap.remove(attr);
		}

		// fire event
		AttrTableModelEvent event = new AttrTableModelEvent(this);
		for (AttrTableModelListener l : listeners) {
			l.attrStructureChanged(event);
		}
	}

	public void attributeValueChanged(AttributeEvent e) {
		Attribute<?> attr = e.getAttribute();
		AttrTableModelRow row = rowMap.get(attr);
		if (row != null) {
			int index = rows.indexOf(row);
			if (index >= 0) {
				AttrTableModelEvent event = new AttrTableModelEvent(this, index);
				for (AttrTableModelListener l : listeners) {
					l.attrValueChanged(event);
				}
			}
		}
	}

}
