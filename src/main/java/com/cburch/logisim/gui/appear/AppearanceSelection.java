/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import java.util.Collection;

import com.cburch.draw.canvas.Selection;
import com.cburch.draw.model.CanvasObject;
import com.cburch.logisim.circuit.appear.AppearanceElement;

public class AppearanceSelection extends Selection {
	@Override
	public void setMovingShapes(Collection<? extends CanvasObject> shapes, int dx, int dy) {
		if (shouldSnap(shapes)) {
			dx = (dx + 5) / 10 * 10;
			dy = (dy + 5) / 10 * 10;
		}
		super.setMovingShapes(shapes, dx, dy);
	}

	@Override
	public void setMovingDelta(int dx, int dy) {
		if (shouldSnap(getSelected())) {
			dx = (dx + 5) / 10 * 10;
			dy = (dy + 5) / 10 * 10;
		}
		super.setMovingDelta(dx, dy);
	}
	
	private boolean shouldSnap(Collection<? extends CanvasObject> shapes) {
		for (CanvasObject o : shapes) {
			if (o instanceof AppearanceElement) {
				return true;
			}
		}
		return false;
	}
}
