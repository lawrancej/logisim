/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.cburch.draw.model.CanvasModel;
import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.undo.Action;
import static com.cburch.logisim.util.LocaleString.*;

public class ModelTranslateAction extends ModelAction {
	private HashSet<CanvasObject> moved;
	private int dx;
	private int dy;
	
	public ModelTranslateAction(CanvasModel model,
			Collection<CanvasObject> moved, int dx, int dy) {
		super(model);
		this.moved = new HashSet<CanvasObject>(moved);
		this.dx = dx;
		this.dy = dy;
	}
	
	@Override
	public Collection<CanvasObject> getObjects() {
		return Collections.unmodifiableSet(moved);
	}

	@Override
	public String getName() {
		return _("actionTranslate", getShapesName(moved));
	}
	
	@Override
	void doSub(CanvasModel model) {
		model.translateObjects(moved, dx, dy);
	}
	
	@Override
	void undoSub(CanvasModel model) {
		model.translateObjects(moved, -dx, -dy);
	}
	
	@Override
	public boolean shouldAppendTo(Action other) {
		if (other instanceof ModelTranslateAction) {
			ModelTranslateAction o = (ModelTranslateAction) other;
			return this.moved.equals(o.moved);
		} else {
			return false;
		}
	}

	@Override
	public Action append(Action other) {
		if (other instanceof ModelTranslateAction) {
			ModelTranslateAction o = (ModelTranslateAction) other;
			if (this.moved.equals(o.moved)) {
				return new ModelTranslateAction(getModel(), moved,
						this.dx + o.dx, this.dy + o.dy);
			}
		}
		return super.append(other);
	}
}
