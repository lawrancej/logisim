/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.cburch.draw.canvas.CanvasObject;
import com.cburch.draw.canvas.Selection;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.gui.main.EditHandler;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.proj.Project;

public class AppearanceEditHandler extends EditHandler {
	private AppearanceCanvas canvas;
	
	AppearanceEditHandler(AppearanceCanvas canvas) {
		this.canvas = canvas;
	}
	
	@Override
	public void computeEnabled() {
		Project proj = canvas.getProject();
		Circuit circ = canvas.getCircuit();
		Selection sel = canvas.getSelection();
		boolean selEmpty = sel.isEmpty();
		boolean canChange = proj.getLogisimFile().contains(circ);
		boolean clipExists = !Clipboard.isEmpty();
		
		setEnabled(LogisimMenuBar.CUT, !selEmpty && canChange);
		setEnabled(LogisimMenuBar.COPY, !selEmpty);
		setEnabled(LogisimMenuBar.PASTE, canChange && clipExists);
		setEnabled(LogisimMenuBar.DELETE, !selEmpty && canChange);
		setEnabled(LogisimMenuBar.DUPLICATE, !selEmpty && canChange);
		setEnabled(LogisimMenuBar.SELECT_ALL, true);
	}
	
	@Override
	public void cut() {
		canvas.getProject().doAction(ClipboardActions.cut(canvas));
	}
	
	@Override
	public void copy() {
		canvas.getProject().doAction(ClipboardActions.copy(canvas));
	}
	
	@Override
	public void paste() {
		List<CanvasObject> clip = Clipboard.get().getObjects();
		if (clip.isEmpty()) return;
		
		List<CanvasObject> add = new ArrayList<CanvasObject>(clip.size());
		for (CanvasObject o : clip) {
			add.add(o.clone());
		}
		
		// find how far we have to translate shapes so that at least one of the
		// pasted shapes doesn't match what's already in the model
		HashSet<CanvasObject> cur;
		cur = new HashSet<CanvasObject>(canvas.getModel().getObjects());
		while (true) {
			// if any shapes in "add" aren't in canvas, we are done
			boolean allMatch = true;
			for (CanvasObject o : add) {
				if (!cur.contains(o)) {
					allMatch = false;
					break;
				}
			}
			if (!allMatch) break;
			
			// otherwise translate everything by 10 pixels and repeat test
			for (CanvasObject o : add) {
				o.translate(10, 10);
			}
		}	
			
		canvas.getProject().doAction(new SelectionAction(canvas,
				Strings.getter("pasteClipboardAction"), null, add, add));
	}
	
	@Override
	public void delete() {
		Selection sel = canvas.getSelection();
		int n = sel.getSelected().size();
		List<CanvasObject> select = new ArrayList<CanvasObject>(n);
		List<CanvasObject> remove = new ArrayList<CanvasObject>(n);
		for (CanvasObject o : sel.getSelected()) {
			if (o.canRemove()) {
				remove.add(o);
			} else {
				select.add(o);
			}
		}
		
		canvas.getProject().doAction(new SelectionAction(canvas,
				Strings.getter("deleteSelectionAction"), remove, null, select));
	}
	
	@Override
	public void duplicate() {
		Selection sel = canvas.getSelection();
		int n = sel.getSelected().size();
		List<CanvasObject> select = new ArrayList<CanvasObject>(n);
		List<CanvasObject> clones = new ArrayList<CanvasObject>(n);
		for (CanvasObject o : sel.getSelected()) {
			if (o.canRemove()) {
				CanvasObject copy = o.clone();
				copy.translate(10, 10);
				clones.add(copy);
				select.add(copy);
			} else {
				select.add(o);
			}
		}
		
		canvas.getProject().doAction(new SelectionAction(canvas,
				Strings.getter("duplicateSelectionAction"), null, clones, select));
	}
	
	@Override
	public void selectAll() {
		Selection sel = canvas.getSelection();
		sel.setSelected(canvas.getModel().getObjects(), true);
		canvas.repaint();
	}
}
