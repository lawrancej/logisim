/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.actions;

import java.util.Collection;
import java.util.Collections;

import com.cburch.draw.canvas.CanvasModel;
import com.cburch.draw.canvas.CanvasObject;
import com.cburch.logisim.data.Location;

public class ModelDeleteHandleAction extends ModelAction {
	private CanvasObject handleObject;
	private Location handle;
	private Location remainingHandle;
	
	public ModelDeleteHandleAction(CanvasModel model, CanvasObject shape, Location handle) {
		super(model);
		this.handleObject = shape;
		this.handle = handle;
	}

	@Override
	public Collection<CanvasObject> getObjects() {
		return Collections.singleton(handleObject);
	}

	@Override
	public String getName() {
		return Strings.get("actionDeleteHandle");
	}
	
	@Override
	void doSub(CanvasModel model) {
		remainingHandle = model.deleteHandle(handleObject, handle);
	}
	
	@Override
	void undoSub(CanvasModel model) {
		if(handleObject.canInsertHandle(remainingHandle)) {
			model.insertHandle(handleObject, remainingHandle, handle);
		}
	}
}
