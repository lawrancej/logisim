/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.cburch.draw.canvas.CanvasModel;
import com.cburch.draw.canvas.CanvasObject;

public class ModelRemoveAction extends ModelAction {
	private ArrayList<CanvasObject> removed;

	public ModelRemoveAction(CanvasModel model, CanvasObject removed) {
		this(model, Collections.singleton(removed));
	}	
	
	public ModelRemoveAction(CanvasModel model, Collection<CanvasObject> removed) {
		super(model);
		this.removed = new ArrayList<CanvasObject>(removed);
	}
	
	@Override
	public Collection<CanvasObject> getObjects() {
		return Collections.unmodifiableList(removed);
	}

	@Override
	public String getName() {
		return Strings.get("actionRemove", getShapesName(removed));
	}
	
	@Override
	void doSub(CanvasModel model) {
		model.removeObjects(removed);
	}
	
	@Override
	void undoSub(CanvasModel model) {
		model.addObjects(removed);
	}
}
