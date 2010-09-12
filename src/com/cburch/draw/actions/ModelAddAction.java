/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.cburch.draw.canvas.CanvasModel;
import com.cburch.draw.canvas.CanvasObject;

public class ModelAddAction extends ModelAction {
	private ArrayList<CanvasObject> added;
	
	public ModelAddAction(CanvasModel model, CanvasObject added) {
		this(model, Collections.singleton(added));
	}
		
	public ModelAddAction(CanvasModel model, Collection<CanvasObject> added) {
		super(model);
		this.added = new ArrayList<CanvasObject>(added);
	}
	
	@Override
	public Collection<CanvasObject> getObjects() {
		return Collections.unmodifiableList(added);
	}

	@Override
	public String getName() {
		return Strings.get("actionAdd", getShapesName(added));
	}
	
	@Override
	void doSub(CanvasModel model) {
		model.addObjects(added);
	}
	
	@Override
	void undoSub(CanvasModel model) {
		model.removeObjects(added);
	}
}
