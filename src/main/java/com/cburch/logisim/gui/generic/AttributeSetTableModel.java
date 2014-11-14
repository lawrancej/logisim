/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

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
import static com.cburch.logisim.util.LocaleString.*;

public abstract class AttributeSetTableModel
        implements AttrTableModel, AttributeListener {
    private class AttrRow implements AttrTableModelRow {
        private Attribute<Object> attr;

        AttrRow(Attribute<?> attr) {
            @SuppressWarnings("unchecked")
            Attribute<Object> objAttr = (Attribute<Object>) attr;
            this.attr = objAttr;
        }

        @Override
        public String getLabel() {
            return attr.getDisplayName();
        }

        @Override
        public String getValue() {
            Object value = attrs.getValue(attr);
            if (value == null) {
                return "";
            } else {
                try {
                    return attr.toDisplayString(value);
                } catch (Exception e) {
                    return "???";
                }
            }
        }

        @Override
        public boolean isValueEditable() {
            return !attrs.isReadOnly(attr);
        }

        @Override
        public Component getEditor(Window parent) {
            Object value = attrs.getValue(attr);
            return attr.getCellEditor(parent, value);
        }

        @Override
        public void setValue(Object value) throws AttrTableSetException {
            Attribute<Object> attr = this.attr;
            if (attr == null || value == null) {
                return;
            }


            try {
                if (value instanceof String) {
                    value = attr.parse((String) value);
                }
                setValueRequested(attr, value);
            } catch (ClassCastException e) {
                String msg = getFromLocale("attributeChangeInvalidError")
                    + ": " + e;
                throw new AttrTableSetException(msg);
            } catch (NumberFormatException e) {
                String msg = getFromLocale("attributeChangeInvalidError");
                String emsg = e.getMessage();
                if (emsg != null && emsg.length() > 0) {
                    msg += ": " + emsg;
                }

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
        if (attrs != null) {
            for (Attribute<?> attr : attrs.getAttributes()) {
                AttrRow row = new AttrRow(attr);
                rowMap.put(attr, row);
                rows.add(row);
            }
        }
    }

    @Override
    public abstract String getTitle();

    public AttributeSet getAttributeSet() {
        return attrs;
    }

    public void setAttributeSet(AttributeSet value) {
        if (attrs != value) {
            if (!listeners.isEmpty()) {
                attrs.removeAttributeListener(this);
            }
            attrs = value;
            if (!listeners.isEmpty()) {
                attrs.addAttributeListener(this);
            }
            attributeListChanged(null);
        }
    }

    @Override
    public void addAttrTableModelListener(AttrTableModelListener listener) {
        if (listeners.isEmpty() && attrs != null) {
            attrs.addAttributeListener(this);
        }
        listeners.add(listener);
    }

    @Override
    public void removeAttrTableModelListener(AttrTableModelListener listener) {
        listeners.remove(listener);
        if (listeners.isEmpty() && attrs != null) {
            attrs.removeAttributeListener(this);
        }
    }

    protected void fireTitleChanged() {
        AttrTableModelEvent event = new AttrTableModelEvent(this);
        for (AttrTableModelListener l : listeners) {
            l.attrTitleChanged(event);
        }
    }

    protected void fireStructureChanged() {
        AttrTableModelEvent event = new AttrTableModelEvent(this);
        for (AttrTableModelListener l : listeners) {
            l.attrStructureChanged(event);
        }
    }

    protected void fireValueChanged(int index) {
        AttrTableModelEvent event = new AttrTableModelEvent(this, index);
        for (AttrTableModelListener l : listeners) {
            l.attrValueChanged(event);
        }
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public AttrTableModelRow getRow(int rowIndex) {
        return rows.get(rowIndex);
    }

    protected abstract void setValueRequested(Attribute<Object> attr, Object value)
        throws AttrTableSetException;

    //
    // AttributeListener methods
    //
    @Override
    public void attributeListChanged(AttributeEvent e) {
        // if anything has changed, don't do anything
        int index = 0;
        boolean match = true;
        int rowsSize = rows.size();
        for (Attribute<?> attr : attrs.getAttributes()) {
            if (index >= rowsSize || rows.get(index).attr != attr) {
                match = false;
                break;
            }
            index++;
        }
        if (match && index == rows.size()) {
            return;
        }


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

        fireStructureChanged();
    }

    @Override
    public void attributeValueChanged(AttributeEvent e) {
        Attribute<?> attr = e.getAttribute();
        AttrTableModelRow row = rowMap.get(attr);
        if (row != null) {
            int index = rows.indexOf(row);
            if (index >= 0) {
                fireValueChanged(index);
            }
        }
    }

}
