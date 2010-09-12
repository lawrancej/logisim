/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import java.util.ArrayList;

import com.cburch.draw.canvas.CanvasObject;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Project;

public class RevertAppearanceAction extends Action {
	private Circuit circuit;
	private ArrayList<CanvasObject> old;
	
	public RevertAppearanceAction(Circuit circuit) {
		this.circuit = circuit;
	}
	
	@Override
	public String getName() {
		return Strings.get("revertAppearanceAction");
	}

	@Override
	public void doIt(Project proj) {
		old = new ArrayList<CanvasObject>(circuit.getAppearance().getObjects());
		circuit.getAppearance().setDefaultAppearance(true);
	}

	@Override
	public void undo(Project proj) {
		circuit.getAppearance().setObjectsForce(old);
	}
}
