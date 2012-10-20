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
import com.cburch.logisim.circuit.appear.AppearanceAnchor;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
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
	private Location anchorNewLocation;
	private Direction anchorNewFacing;
	private Location anchorOldLocation;
	private Direction anchorOldFacing;
	
	public SelectionAction(AppearanceCanvas canvas, StringGetter displayName,
			Collection<CanvasObject> toRemove, Collection<CanvasObject> toAdd,
			Collection<CanvasObject> newSelection, Location anchorLocation,
			Direction anchorFacing) {
		this.canvas = canvas;
		this.canvasModel = canvas.getModel();
		this.displayName = displayName;
		this.toRemove = toRemove == null ? null : ZOrder.getZIndex(toRemove, canvasModel);
		this.toAdd = toAdd;
		this.oldSelection = new ArrayList<CanvasObject>(canvas.getSelection().getSelected());
		this.newSelection = newSelection;
		this.anchorNewLocation = anchorLocation;
		this.anchorNewFacing = anchorFacing;
	}

	@Override
	public String getName() {
		return displayName.toString();
	}
	
	@Override
	public void doIt(Project proj) {
		Selection sel = canvas.getSelection();
		sel.clearSelected();
		if (toRemove != null) canvasModel.removeObjects(toRemove.keySet());
		int dest = AppearanceCanvas.getMaxIndex(canvasModel) + 1;
		if (toAdd != null) canvasModel.addObjects(dest, toAdd);

		AppearanceAnchor anchor = findAnchor(canvasModel);
		if (anchor != null && anchorNewLocation != null) {
			anchorOldLocation = anchor.getLocation();
			anchor.translate(anchorNewLocation.getX() - anchorOldLocation.getX(),
					anchorNewLocation.getY() - anchorOldLocation.getY());
		}
		if (anchor != null && anchorNewFacing != null) {
			anchorOldFacing = anchor.getFacing();
			anchor.setValue(AppearanceAnchor.FACING, anchorNewFacing);
		}
		sel.setSelected(newSelection, true);
		canvas.repaint();
	}
	
	private AppearanceAnchor findAnchor(CanvasModel canvasModel) {
		for (Object o : canvasModel.getObjectsFromTop()) {
			if (o instanceof AppearanceAnchor) {
				return (AppearanceAnchor) o;
			}
		}
		return null;
	}
	
	@Override
	public void undo(Project proj) {
		AppearanceAnchor anchor = findAnchor(canvasModel);
		if (anchor != null && anchorOldLocation != null) {
			anchor.translate(anchorOldLocation.getX() - anchorNewLocation.getX(),
					anchorOldLocation.getY() - anchorNewLocation.getY());
		}
		if (anchor != null && anchorOldFacing != null) {
			anchor.setValue(AppearanceAnchor.FACING, anchorOldFacing);
		}
		Selection sel = canvas.getSelection();
		sel.clearSelected();
		if (toAdd != null) canvasModel.removeObjects(toAdd);
		if (toRemove != null) canvasModel.addObjects(toRemove);
		sel.setSelected(oldSelection, true);
		canvas.repaint();
	}
}
