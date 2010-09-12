/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.actions;

import java.util.Collection;
import java.util.Collections;

import com.cburch.draw.canvas.CanvasModel;
import com.cburch.draw.canvas.CanvasObject;
import com.cburch.draw.undo.Action;
import com.cburch.logisim.data.Location;

public class ModelMoveHandleAction extends ModelAction {
	private CanvasObject handleObject;
	private Location handle;
	private int dx;
	private int dy;
	
	public ModelMoveHandleAction(CanvasModel model, CanvasObject shape,
			Location handle, int dx, int dy) {
		super(model);
		this.handleObject = shape;
		this.handle = handle;
		this.dx = dx;
		this.dy = dy;
	}

	@Override
	public Collection<CanvasObject> getObjects() {
		return Collections.singleton(handleObject);
	}

	@Override
	public String getName() {
		return Strings.get("actionMoveHandle");
	}
	
	@Override
	void doSub(CanvasModel model) {
		model.moveHandle(handleObject, handle, dx, dy);
	}
	
	@Override
	void undoSub(CanvasModel model) {
		model.moveHandle(handleObject, handle.translate(dx, dy), -dx, -dy);
	}
	
	@Override
	public boolean shouldAppendTo(Action other) {
		if(other instanceof ModelMoveHandleAction) {
			ModelMoveHandleAction o = (ModelMoveHandleAction) other;
			return this.handleObject == o.handleObject && this.handle.equals(o.handle);
		} else {
			return false;
		}
	}
	
	@Override
	public Action append(Action other) {
		if(other instanceof ModelMoveHandleAction) {
			ModelMoveHandleAction o = (ModelMoveHandleAction) other;
			if(this.handleObject == o.handleObject && this.handle.equals(o.handle)) {
				return new ModelMoveHandleAction(getModel(), this.handleObject,
						this.handle, this.dx + o.dx, this.dy + o.dy);
			}
		}
		return super.append(other);
	}
}
