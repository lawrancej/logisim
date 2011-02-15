/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.tools.AbstractTool;
import com.cburch.draw.tools.DrawingAttributeSet;
import com.cburch.draw.tools.SelectTool;
import com.cburch.logisim.gui.generic.AttrTable;

public class AttrTableDrawManager implements PropertyChangeListener {
	private Canvas canvas;
	private AttrTable table;
	private AttrTableSelectionModel selectionModel;
	private AttrTableToolModel toolModel;
	
	public AttrTableDrawManager(Canvas canvas, AttrTable table, DrawingAttributeSet attrs) {
		this.canvas = canvas;
		this.table = table;
		this.selectionModel = new AttrTableSelectionModel(canvas);
		this.toolModel = new AttrTableToolModel(attrs, null);
		
		canvas.addPropertyChangeListener(Canvas.TOOL_PROPERTY, this);
		updateToolAttributes();
	}
	
	public void attributesSelected() {
		updateToolAttributes();
	}

	//
	// PropertyChangeListener method
	//
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (prop.equals(Canvas.TOOL_PROPERTY)) {
			updateToolAttributes();
		}
	}
	
	private void updateToolAttributes() {
		Object tool = canvas.getTool();
		if (tool instanceof SelectTool) {
			table.setAttrTableModel(selectionModel);
		} else if (tool instanceof AbstractTool) {
			toolModel.setTool((AbstractTool) tool);
			table.setAttrTableModel(toolModel);
		} else {
			table.setAttrTableModel(null);
		}
	}
}
