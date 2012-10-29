/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import com.cburch.logisim.util.JDialogOk;
import com.cburch.logisim.util.JInputComponent;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedList;
import static com.cburch.logisim.util.LocaleString.*;

public class AttrTable extends JPanel implements LocaleListener {
	private static final AttrTableModel NULL_ATTR_MODEL = new NullAttrModel();
	
	private static class NullAttrModel implements AttrTableModel {
		public void addAttrTableModelListener(AttrTableModelListener listener) { }
		public void removeAttrTableModelListener(AttrTableModelListener listener) { }
		
		public String getTitle() { return null; }
		public int getRowCount() { return 0; }
		public AttrTableModelRow getRow(int rowIndex) { return null; }
	}
	
	private static class TitleLabel extends JLabel {
		@Override
		public Dimension getMinimumSize() {
			Dimension ret = super.getMinimumSize();
			return new Dimension(1, ret.height);
		}
	}

	private static class MyDialog extends JDialogOk {
		JInputComponent input;
		Object value;

		public MyDialog(Dialog parent, JInputComponent input) {
			super(parent, _("attributeDialogTitle"), true);
			configure(input);
		}

		public MyDialog(Frame parent, JInputComponent input) {
			super(parent, _("attributeDialogTitle"), true);
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

	private class TableModelAdapter
			implements TableModel, AttrTableModelListener {
		Window parent;
		LinkedList<TableModelListener> listeners;
		AttrTableModel attrModel;
		
		TableModelAdapter(Window parent, AttrTableModel attrModel) {
			this.parent = parent;
			this.listeners = new LinkedList<TableModelListener>();
			this.attrModel = attrModel;
		}
		
		void setAttrTableModel(AttrTableModel value) {
			if (attrModel != value) {
				TableCellEditor editor = table.getCellEditor();
				if (editor != null) {
					editor.cancelCellEditing();
				}
				attrModel.removeAttrTableModelListener(this);
				attrModel = value;
				attrModel.addAttrTableModelListener(this);
				fireTableChanged();
			}
		}

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
			else                  return "Value";
		}

		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		public int getRowCount() {
			return attrModel.getRowCount();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return attrModel.getRow(rowIndex).getLabel();
			} else {
				return attrModel.getRow(rowIndex).getValue();
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex > 0 && attrModel.getRow(rowIndex).isValueEditable();
		}

		public void setValueAt(Object value, int rowIndex,
				int columnIndex) {
			if (columnIndex > 0) {
				try {
					attrModel.getRow(rowIndex).setValue(value);
				} catch (AttrTableSetException e) {
					JOptionPane.showMessageDialog(parent, e.getMessage(),
							_("attributeChangeInvalidTitle"),
							JOptionPane.WARNING_MESSAGE);
				}
			}
		}
		
		//
		// AttrTableModelListener methods
		//
		public void attrTitleChanged(AttrTableModelEvent e) {
			if (e.getSource() != attrModel) {
				attrModel.removeAttrTableModelListener(this);
				return;
			}
			updateTitle();
		}
		
		public void attrStructureChanged(AttrTableModelEvent e) {
			if (e.getSource() != attrModel) {
				attrModel.removeAttrTableModelListener(this);
				return;
			}
			TableCellEditor ed = table.getCellEditor();
			if (ed != null) {
				ed.cancelCellEditing();
			}
			fireTableChanged();
		}
		
		public void attrValueChanged(AttrTableModelEvent e) {
			if (e.getSource() != attrModel) {
				attrModel.removeAttrTableModelListener(this);
				return;
			}
			int row = e.getRowIndex();
			TableCellEditor ed = table.getCellEditor();
			if (row >= 0 && ed instanceof CellEditor
					&& attrModel.getRow(row) == ((CellEditor) ed).currentRow) {
				ed.cancelCellEditing();
			}
			fireTableChanged();
		}
	}

	private class CellEditor
			implements TableCellEditor, FocusListener, ActionListener {
		LinkedList<CellEditorListener> listeners = new LinkedList<CellEditorListener>();
		AttrTableModelRow currentRow;
		Component currentEditor;

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
			ChangeEvent e = new ChangeEvent(AttrTable.this);
			for (CellEditorListener l : new ArrayList<CellEditorListener>(listeners)) {
				l.editingCanceled(e);
			}
		}

		public void fireEditingStopped() {
			ChangeEvent e = new ChangeEvent(AttrTable.this);
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
			Component comp = currentEditor;
			if (comp instanceof JTextField) {
				return ((JTextField) comp).getText();
			} else if (comp instanceof JComboBox) {
				return ((JComboBox) comp).getSelectedItem();
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

		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int rowIndex, int columnIndex) {
			AttrTableModel attrModel = tableModel.attrModel;
			AttrTableModelRow row = attrModel.getRow(rowIndex);
			
			if (columnIndex == 0) {
				return new JLabel(row.getLabel());
			} else {
				if (currentEditor != null) currentEditor.transferFocus();

				Component editor = row.getEditor(parent);
				if (editor instanceof JComboBox) {
					((JComboBox) editor).addActionListener(this);
					editor.addFocusListener(this);
				} else if (editor instanceof JInputComponent) {
					JInputComponent input = (JInputComponent) editor;
					MyDialog dlog;
					Window parent = AttrTable.this.parent;
					if (parent instanceof Frame) {
						dlog = new MyDialog((Frame) parent, input);
					} else {
						dlog = new MyDialog((Dialog) parent, input);
					}
					dlog.setVisible(true);
					Object retval = dlog.getValue();
					try {
						row.setValue(retval);
					} catch (AttrTableSetException e) {
						JOptionPane.showMessageDialog(parent, e.getMessage(),
								_("attributeChangeInvalidTitle"),
								JOptionPane.WARNING_MESSAGE);
					}
					editor = new JLabel(row.getValue());
				} else {
					editor.addFocusListener(this);
				}
				currentRow = row;
				currentEditor = editor;
				return editor;
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
					if (p == AttrTable.this) {
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
	private boolean titleEnabled;
	private JLabel title;
	private JTable table;
	private TableModelAdapter tableModel;
	private CellEditor editor = new CellEditor();

	public AttrTable(Window parent) {
		super(new BorderLayout());
		this.parent = parent;
		
		titleEnabled = true;
		title = new TitleLabel();
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setVerticalAlignment(SwingConstants.CENTER);
		tableModel = new TableModelAdapter(parent, NULL_ATTR_MODEL);
		table = new JTable(tableModel);
		table.setDefaultEditor(Object.class, editor);
		table.setTableHeader(null);
		table.setRowHeight(20);
		
		Font baseFont = title.getFont();
		int titleSize = Math.round(baseFont.getSize() * 1.2f);
		Font titleFont = baseFont.deriveFont((float) titleSize).deriveFont(Font.BOLD);
		title.setFont(titleFont);
		Color bgColor = new Color(240, 240, 240);
		setBackground(bgColor);
		table.setBackground(bgColor);
		Object renderer = table.getDefaultRenderer(String.class);
		if (renderer instanceof JComponent) {
			((JComponent) renderer).setBackground(Color.WHITE);
		}
		
		JScrollPane tableScroll = new JScrollPane(table);
		
		this.add(title, BorderLayout.PAGE_START);
		this.add(tableScroll, BorderLayout.CENTER);
		LocaleManager.addLocaleListener(this);
		localeChanged();
	}
	
	public void setTitleEnabled(boolean value) {
		titleEnabled = value;
		updateTitle();
	}
	
	public boolean getTitleEnabled() {
		return titleEnabled;
	}
	
	public void setAttrTableModel(AttrTableModel value) {
		tableModel.setAttrTableModel(value == null ? NULL_ATTR_MODEL : value);
		updateTitle();
	}
	
	public AttrTableModel getAttrTableModel() {
		return tableModel.attrModel;
	}

	public void localeChanged() {
		updateTitle();
		tableModel.fireTableChanged();
	}
	
	private void updateTitle() {
		if (titleEnabled) {
			String text = tableModel.attrModel.getTitle();
			if (text == null) {
				title.setVisible(false);
			} else {
				title.setText(text);
				title.setVisible(true);
			}
		} else {
			title.setVisible(false);
		}
	}
}
