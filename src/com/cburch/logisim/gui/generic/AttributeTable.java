/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.util.JDialogOk;
import com.cburch.logisim.util.JInputComponent;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

public class AttributeTable extends JTable implements LocaleListener {
	private class MyListener implements AttributeListener {
		public void attributeListChanged(AttributeEvent e) {
			if (e.getSource() != attrs) {
				e.getSource().removeAttributeListener(this);
				return;
			}
			model.fireTableChanged();
		}
		public void attributeValueChanged(AttributeEvent e) {
			if (e.getSource() != attrs) {
				e.getSource().removeAttributeListener(this);
				return;
			}
			model.fireTableChanged();
		}
	}

	private static class AttributeData {
		AttributeSet attrs;
		Attribute<?> attr;
		Component comp;

		AttributeData() { }
	}

	private static class MyDialog extends JDialogOk {
		JInputComponent input;
		Object value;

		public MyDialog(Dialog parent, JInputComponent input) {
			super(parent, Strings.get("attributeDialogTitle"), true);
			configure(input);
		}

		public MyDialog(Frame parent, JInputComponent input) {
			super(parent, Strings.get("attributeDialogTitle"), true);
			configure(input);
		}

		private void configure(JInputComponent input) {
			this.input = input;
			this.value = input.getValue();
			
			// Thanks to Christophe Jacquet, who contributed a fix to this
			// so that when the dialog is resized, the component within it
			// is resized as well. (Tracker #2024479)
			JPanel p = new JPanel(new BorderLayout());
			p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			p.add((JComponent) input, BorderLayout.CENTER);
			getContentPane().add(p, BorderLayout.CENTER);

			pack();
		}

		@Override
		public void okClicked() {
			value = input.getValue();
		}

		public Object getValue() {
			return value;
		}
	}

	private class Model implements TableModel {
		LinkedList<TableModelListener> listeners = new LinkedList<TableModelListener>();
		private Object lastValue = null; // to prevent two messages being shown
		private long lastUpdate;         // due to duplicate calls to setValueAt

		public void addTableModelListener(TableModelListener l) {
			listeners.add(l);
		}

		public void removeTableModelListener(TableModelListener l) {
			listeners.remove(l);
		}

		void fireTableChanged() {
			TableModelEvent e = new TableModelEvent(this);
			for (TableModelListener l : new ArrayList<TableModelListener>(listeners)) {
				l.tableChanged(e);
			}
		}

		public int getColumnCount() {
			return 2;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Attribute";
			else                 return "Value";
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			else                 return Object.class;
		}

		public int getRowCount() {
			if (attrs == null) return 0;
			else              return attrs.getAttributes().size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (attrs == null) return null;
			Attribute<?> attrBase = attrs.getAttributes().get(rowIndex);
			@SuppressWarnings("unchecked")
			Attribute<Object> attr = (Attribute<Object>) attrBase;
			if (attr == null)           return null;
			else if (columnIndex == 0)  return attr.getDisplayName();
			else {
				Object val = attrs.getValue(attr);
				if (val == null) return "";
				else            return attr.toDisplayString(val);
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return false;
			} else {
				if (attrs == null) return false;
				Attribute<?> attr = attrs.getAttributes().get(rowIndex);
				return !attrs.isReadOnly(attr);
			}
		}

		public void setValueAt(Object aValue, int rowIndex,
				int columnIndex) {
			Attribute<?> attr = findCurrentAttribute();
			if (attr == null) {
				if (attrs == null) return;
				List<Attribute<?>> attrList = attrs.getAttributes();
				if (rowIndex >= attrList.size()) return;
				attr = attrList.get(rowIndex);
			}
			if (attr == null || aValue == null) return;
			
			try {
				if (aValue instanceof String) {
					aValue = attr.parse((String) aValue);
				}
				AttributeTableListener l = listener;
				if (l != null) {
					l.valueChangeRequested(AttributeTable.this, attrs, attr, aValue);
				}
			} catch (ClassCastException e) {
				String msg = Strings.get("attributeChangeInvalidError")
					+ ": " + e;
				JOptionPane.showMessageDialog(parent, msg,
						Strings.get("attributeChangeInvalidTitle"),
						JOptionPane.WARNING_MESSAGE);
			} catch (NumberFormatException e) {
				long now = System.currentTimeMillis();
				if (aValue.equals(lastValue) && now < lastUpdate + 500) {
					return;
				}
				lastValue = aValue;
				lastUpdate = System.currentTimeMillis();

				String msg = Strings.get("attributeChangeInvalidError");
				String emsg = e.getMessage();
				if (emsg != null && emsg.length() > 0) msg += ": " + emsg;
				msg += ".";
				JOptionPane.showMessageDialog(parent, msg,
						Strings.get("attributeChangeInvalidTitle"),
						JOptionPane.WARNING_MESSAGE);
			}
		}

		private Attribute<?> findCurrentAttribute() {
			for (int i = 0; i < history.length; i++) {
				if (history[i] != null && history[i].comp == editorComp) {
					return history[i].attr;
				}
			}
			return null;
		}

	}

	private class CellEditor
			implements TableCellEditor, FocusListener, ActionListener {
		LinkedList<CellEditorListener> listeners = new LinkedList<CellEditorListener>();

		//
		// TableCellListener management
		//
		public void addCellEditorListener(CellEditorListener l) {
			// Adds a listener to the list that's notified when the
			// editor stops, or cancels editing. 
			listeners.add(l);
		}

		public void removeCellEditorListener(CellEditorListener l) {
			// Removes a listener from the list that's notified 
			listeners.remove(l);
		}

		public void fireEditingCanceled() {
			ChangeEvent e = new ChangeEvent(AttributeTable.this);
			for (CellEditorListener l : new ArrayList<CellEditorListener>(listeners)) {
				l.editingCanceled(e);
			}
		}

		public void fireEditingStopped() {
			ChangeEvent e = new ChangeEvent(AttributeTable.this);
			for (CellEditorListener l : new ArrayList<CellEditorListener>(listeners)) {
				l.editingStopped(e);
			}
		}

		//
		// other TableCellEditor methods
		//
		public void cancelCellEditing() {
			// Tells the editor to cancel editing and not accept any
			// partially edited value. 
			fireEditingCanceled();
		}

		public boolean stopCellEditing() {
			// Tells the editor to stop editing and accept any partially
			// edited value as the value of the editor. 
			fireEditingStopped();
			return true;
		}

		public Object getCellEditorValue() {
			// Returns the value contained in the editor. 
			Component comp = editorComp;
			if (comp instanceof JTextField) {
				return ((JTextField) comp).getText();
			} else if (comp instanceof JComboBox) {
				Object val = ((JComboBox) comp).getSelectedItem();
				return val;
			} else {
				return null;
			}
		}

		public boolean isCellEditable(EventObject anEvent) {
			// Asks the editor if it can start editing using anEvent. 
			return true;
		}

		public boolean shouldSelectCell(EventObject anEvent) {
			// Returns true if the editing cell should be selected,
			// false otherwise. 
			return true;
		}

		public Component getTableCellEditorComponent(
				JTable table, Object value, boolean isSelected,
				int row, int column) {
			if (column == 0 || attrs == null) {
				return new JLabel(value.toString());
			} else {
				if (editorComp != null) editorComp.transferFocus();

				Attribute<?> attrBase = attrs.getAttributes().get(row);
				@SuppressWarnings("unchecked")
				Attribute<Object> attr = (Attribute<Object>) attrBase;
				Component ret = attr.getCellEditor(parent, attrs.getValue(attrBase));
				if (ret instanceof JComboBox) {
					((JComboBox) ret).addActionListener(this);
				} else if (ret instanceof JInputComponent) {
					JInputComponent input = (JInputComponent) ret;
					MyDialog dlog;
					Window parent = AttributeTable.this.parent;
					if (parent instanceof Frame) {
						dlog = new MyDialog((Frame) parent, input);
					} else {
						dlog = new MyDialog((Dialog) parent, input);
					}
					dlog.setVisible(true);
					Object retval = dlog.getValue();
					AttributeTableListener l = listener;
					if (l != null) {
						l.valueChangeRequested(AttributeTable.this, attrs, attr, retval);
					}
					return new JLabel(attr.toDisplayString(retval));
				} else {
					ret.addFocusListener(this);
				}

				AttributeData n = history[history.length - 1];
				if (n == null) n = new AttributeData();
				for (int i = history.length - 1; i > 0; i--) {
					history[i] = history[i - 1];
				}
				n.attrs = attrs;
				n.attr = attr;
				n.comp = ret;

				return ret;
			}
		}

		//
		// FocusListener methods
		//
		public void focusLost(FocusEvent e) {
			Object dst = e.getOppositeComponent();
			if (dst instanceof Component) {
				Component p = (Component) dst;
				while (p != null && !(p instanceof Window)) {
					if (p == AttributeTable.this) {
						// switch to another place in this table,
						// no problem
						return;
					}
					p = p.getParent();
				}
				// focus transferred outside table; stop editing
				editor.stopCellEditing();
			}
		}

		public void focusGained(FocusEvent e) { }

		//
		// ActionListener methods
		//
		public void actionPerformed(ActionEvent e) {
			stopCellEditing();
		}

	}

	private Window parent;
	private Model model = new Model();
	private CellEditor editor = new CellEditor();
	private AttributeSet attrs = null;
	private AttributeTableListener listener = null;
	private MyListener attrsListener = new MyListener();
	private AttributeData[] history = new AttributeData[5];

	public AttributeTable(Window parent) {
		this.parent = parent;
		setModel(model);
		setDefaultEditor(Object.class, editor);
		setTableHeader(null);
		setRowHeight(20);
		LocaleManager.addLocaleListener(this);
	}

	public AttributeSet getAttributeSet() {
		return attrs;
	}
	
	public AttributeTableListener getAttributeTableListener() {
		return listener;
	}

	public void setAttributeSet(AttributeSet attrs, AttributeTableListener l) {
		if (attrs != this.attrs) {
			removeEditor();
			if (this.attrs != null) {
				this.attrs.removeAttributeListener(attrsListener);
			}
			this.attrs = attrs;
			if (this.attrs != null) {
				this.attrs.addAttributeListener(attrsListener);
			}
			this.listener = l;
			model.fireTableChanged();
		}
	}

	public void localeChanged() {
		model.fireTableChanged();
	}

}
