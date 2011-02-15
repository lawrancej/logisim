/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.gui.AttrTableDrawManager;
import com.cburch.draw.toolbar.ToolbarModel;
import com.cburch.draw.tools.DrawingAttributeSet;
import com.cburch.draw.tools.SelectTool;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.gui.generic.AttrTable;
import com.cburch.logisim.gui.generic.BasicZoomModel;
import com.cburch.logisim.gui.generic.CanvasPane;
import com.cburch.logisim.gui.generic.ZoomModel;
import com.cburch.logisim.gui.main.EditHandler;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;

public class AppearanceView {
	private static final double[] ZOOM_OPTIONS = { 100, 150, 200, 300, 400, 600, 800 };

	private DrawingAttributeSet attrs;
	private AppearanceCanvas canvas;
	private CanvasPane canvasPane;
	private AppearanceToolbarModel toolbarModel;
	private AttrTableDrawManager attrTableManager;
	private ZoomModel zoomModel;
	private AppearanceEditHandler editHandler;
	
	public AppearanceView() {
		attrs = new DrawingAttributeSet();
		SelectTool selectTool = new SelectTool();
		canvas = new AppearanceCanvas(selectTool);
		toolbarModel = new AppearanceToolbarModel(selectTool, canvas, attrs);
		zoomModel = new BasicZoomModel(AppPreferences.APPEARANCE_SHOW_GRID,
				AppPreferences.APPEARANCE_ZOOM, ZOOM_OPTIONS);
		canvas.getGridPainter().setZoomModel(zoomModel);
		attrTableManager = null;
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
	
	public AttrTableDrawManager getAttrTableDrawManager(AttrTable table) {
		AttrTableDrawManager ret = attrTableManager;
		if (ret == null) {
			ret = new AttrTableDrawManager(canvas, table, attrs);
			attrTableManager = ret;
		}
		return ret;
	}
	
	public void setCircuit(Project proj, CircuitState circuitState) {
		canvas.setCircuit(proj, circuitState);
	}
}
