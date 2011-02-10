package com.cburch.logisim.gui.generic;

public interface AttrTableModel {
	public void addAttrTableModelListener(AttrTableModelListener listener);
	public void removeAttrTableModelListener(AttrTableModelListener listener);
	
	public int getRowCount();
	public AttrTableModelRow getRow(int rowIndex);
}
