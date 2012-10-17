/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.generic;

public interface AttrTableModel {
	public void addAttrTableModelListener(AttrTableModelListener listener);
	public void removeAttrTableModelListener(AttrTableModelListener listener);
	
	public String getTitle();
	public int getRowCount();
	public AttrTableModelRow getRow(int rowIndex);
}
