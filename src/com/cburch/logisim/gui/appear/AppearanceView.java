/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.gui.AttributeManager;
import com.cburch.draw.toolbar.ToolbarModel;
import com.cburch.draw.tools.DrawingAttributeSet;
import com.cburch.draw.tools.SelectTool;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.gui.generic.AttributeTable;
import com.cburch.logisim.gui.generic.CanvasPane;
import com.cburch.logisim.gui.generic.ZoomModel;
import com.cburch.logisim.gui.main.EditHandler;
import com.cburch.logisim.proj.Project;

public class AppearanceView {
	private DrawingAttributeSet attrs;
	private AppearanceCanvas canvas;
	private CanvasPane canvasPane;
	private AppearanceToolbarModel toolbarModel;
	private AttributeManager attributeManager;
	private ZoomModel zoomModel;
	private AppearanceEditHandler editHandler;
	
	public AppearanceView() {
		attrs = new DrawingAttributeSet();
		SelectTool selectTool = new SelectTool();
		canvas = new AppearanceCanvas(selectTool);
		toolbarModel = new AppearanceToolbarModel(selectTool, canvas, attrs);
		zoomModel = new AppearanceZoomModel();
		canvas.getGridPainter().setZoomModel(zoomModel);
		attributeManager = null;
		canvasPane = new CanvasPane(canvas);
		canvasPane.setZoomModel(zoomModel);
		editHandler = new AppearanceEditHandler(canvas);
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
	
	public CanvasPane getCanvasPane() {
		return canvasPane;
	}
	
	public ToolbarModel getToolbarModel() {
		return toolbarModel;
	}
	
	public ZoomModel getZoomModel() {
		return zoomModel;
	}
	
	public EditHandler getEditHandler() {
		return editHandler;
	}
	
	public AttributeSet getAttributeSet() {
		return attrs;
	}
	
	public AttributeManager getAttributeManager(AttributeTable table) {
		AttributeManager ret = attributeManager;
		if (ret == null) {
			ret = new AttributeManager(canvas, table, attrs);
			attributeManager = ret;
		}
		return ret;
	}
	
	public void setCircuit(Project proj, CircuitState circuitState) {
		canvas.setCircuit(proj, circuitState);
	}
}
