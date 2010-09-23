/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.cburch.draw.canvas.Selection;
import com.cburch.draw.model.CanvasModel;
import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.util.ZOrder;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.util.StringGetter;

class SelectionAction extends Action {
	private StringGetter displayName;
	private AppearanceCanvas canvas;
	private CanvasModel canvasModel;
	private Map<CanvasObject, Integer> toRemove;
	private Collection<CanvasObject> toAdd;
	private Collection<CanvasObject> oldSelection;
	private Collection<CanvasObject> newSelection;
	
	public SelectionAction(AppearanceCanvas canvas, StringGetter displayName,
			Collection<CanvasObject> toRemove, Collection<CanvasObject> toAdd,
			Collection<CanvasObject> newSelection) {
		this.canvas = canvas;
		this.canvasModel = canvas.getModel();
		this.displayName = displayName;
		this.toRemove = toRemove == null ? null : ZOrder.getZIndex(toRemove, canvasModel);
		this.toAdd = toAdd;
		this.oldSelection = new ArrayList<CanvasObject>(canvas.getSelection().getSelected());
		this.newSelection = newSelection;
	}

	@Override
	public String getName() {
		return displayName.get();
	}
	
	@Override
	public void doIt(Project proj) {
		Selection sel = canvas.getSelection();
		sel.clearSelected();
		if (toRemove != null) canvasModel.removeObjects(toRemove.keySet());
		int dest = AppearanceCanvas.getMaxIndex(canvasModel);
		if (toAdd != null) canvasModel.addObjects(dest, toAdd);
		sel.setSelected(newSelection, true);
		canvas.repaint();
	}
	
	@Override
	public void undo(Project proj) {
		Selection sel = canvas.getSelection();
		sel.clearSelected();
		if (toAdd != null) canvasModel.removeObjects(toAdd);
		if (toRemove != null) canvasModel.addObjects(toRemove);
		sel.setSelected(oldSelection, true);
		canvas.repaint();
	}
}
