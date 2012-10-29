/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.cburch.draw.model.CanvasModel;
import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.util.ZOrder;
import static com.cburch.logisim.util.LocaleString.*;

public class ModelRemoveAction extends ModelAction {
	private Map<CanvasObject, Integer> removed;

	public ModelRemoveAction(CanvasModel model, CanvasObject removed) {
		this(model, Collections.singleton(removed));
	}	
	
	public ModelRemoveAction(CanvasModel model, Collection<CanvasObject> removed) {
		super(model);
		this.removed = ZOrder.getZIndex(removed, model);
	}
	
	@Override
	public Collection<CanvasObject> getObjects() {
		return Collections.unmodifiableSet(removed.keySet());
	}

	@Override
	public String getName() {
		return _("actionRemove", getShapesName(removed.keySet()));
	}
	
	@Override
	void doSub(CanvasModel model) {
		model.removeObjects(removed.keySet());
	}
	
	@Override
	void undoSub(CanvasModel model) {
		model.addObjects(removed);
	}
}
