/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.actions;

import java.util.Collection;
import java.util.Collections;

import com.cburch.draw.model.CanvasModel;
import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.undo.Action;
import static com.cburch.logisim.util.LocaleString.*;

public abstract class ModelAction extends Action {
	private CanvasModel model;
	
	public ModelAction(CanvasModel model) {
		this.model = model;
	}
	
	public Collection<CanvasObject> getObjects() {
		return Collections.emptySet();
	}

	@Override
	public abstract String getName();
	
	abstract void doSub(CanvasModel model);
	
	abstract void undoSub(CanvasModel model);

	@Override
	public final void doIt() {
		doSub(model);
	}

	@Override
	public final void undo() {
		undoSub(model);
	}
	
	public CanvasModel getModel() {
		return model;
	}
	
	static String getShapesName(Collection<CanvasObject> coll) {
		if (coll.size() != 1) {
			return _("shapeMultiple");
		} else {
			CanvasObject shape = coll.iterator().next();
			return shape.getDisplayName();
		}
	}
}
