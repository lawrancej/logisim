/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.canvas;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.cburch.logisim.data.Location;

public class Selection {
	private ArrayList<SelectionListener> listeners;
	private HashSet<CanvasObject> selected;
	private Set<CanvasObject> selectedView;
	private HashSet<CanvasObject> suppressed;
	private Set<CanvasObject> suppressedView;
	private CanvasObject handleShape;
	private Location handleLocation;
	private int handleDx;
	private int handleDy;
	private int moveDx;
	private int moveDy;
	
	protected Selection() {
		listeners = new ArrayList<SelectionListener>();
		selected = new HashSet<CanvasObject>();
		suppressed = new HashSet<CanvasObject>();
		selectedView = Collections.unmodifiableSet(selected);
		suppressedView = Collections.unmodifiableSet(suppressed);
	}
	
	public void addSelectionListener(SelectionListener l) {
		listeners.add(l);
	}
	
	public void removeSelectionListener(SelectionListener l) {
		listeners.remove(l);
	}
	
	private void fireChanged(int action, Collection<CanvasObject> affected) {
		SelectionEvent e = null;
		for (SelectionListener listener : listeners) {
			if(e == null) e = new SelectionEvent(this, action, affected);
			listener.selectionChanged(e);
		}
	}
	
	public boolean isEmpty() {
		return selected.isEmpty();
	}
	
	public boolean isSelected(CanvasObject shape) {
		return selected.contains(shape);
	}
	
	public Set<CanvasObject> getSelected() {
		return selectedView;
	}
	
	public void clearSelected() {
		if(!selected.isEmpty()) {
			ArrayList<CanvasObject> oldSelected;
			oldSelected = new ArrayList<CanvasObject>(selected);
			selected.clear();
			handleShape = null;
			suppressed.clear();
			fireChanged(SelectionEvent.ACTION_REMOVED, oldSelected);
		}
	}
	
	public void setSelected(CanvasObject shape, boolean value) {
		setSelected(Collections.singleton(shape), value);
	}
	
	public void setSelected(Collection<CanvasObject> shapes, boolean value) {
		if(value) {
			ArrayList<CanvasObject> added;
			added = new ArrayList<CanvasObject>(shapes.size());
			for (CanvasObject shape : shapes) {
				if(selected.add(shape)) {
					added.add(shape);
				}
			}
			if (!added.isEmpty()) {
				fireChanged(SelectionEvent.ACTION_ADDED, added);
			}
		} else {
			ArrayList<CanvasObject> removed;
			removed = new ArrayList<CanvasObject>(shapes.size());
			for (CanvasObject shape : shapes) {
				if(selected.remove(shape)) {
					suppressed.remove(shape);
					if(handleShape == shape) handleShape = null;
					removed.add(shape);
				}
			}
			if (!removed.isEmpty()) {
				fireChanged(SelectionEvent.ACTION_REMOVED, removed);
			}
		}
	}
	
	public void toggleSelected(Collection<CanvasObject> shapes) {
		ArrayList<CanvasObject> added;
		added = new ArrayList<CanvasObject>(shapes.size());
		ArrayList<CanvasObject> removed;
		removed = new ArrayList<CanvasObject>(shapes.size());
		for (CanvasObject shape : shapes) {
			if (selected.contains(shape)) {
				selected.remove(shape);
				suppressed.remove(shape);
				if(handleShape == shape) handleShape = null;
				removed.add(shape);
			} else {
				selected.add(shape);
				added.add(shape);
			}
		}
		if (!removed.isEmpty()) {
			fireChanged(SelectionEvent.ACTION_REMOVED, removed);
		}
		if (!added.isEmpty()) {
			fireChanged(SelectionEvent.ACTION_ADDED, added);
		}
	}
	
	public Set<CanvasObject> getDrawsSuppressed() {
		return suppressedView;
	}
	
	public void clearDrawsSuppressed() {
		suppressed.clear();
		handleDx = 0;
		handleDy = 0;
	}
	
	public CanvasObject getHandleShape() {
		return handleShape;
	}
	
	public Location getHandleLocation() {
		return handleLocation;
	}
	
	public void setHandleSelected(CanvasObject shape, Location handle) {
		handleShape = shape;
		handleLocation = handle;
		handleDx = 0;
		handleDy = 0;
	}
	
	public Location getHandleDelta() {
		return Location.create(handleDx, handleDy);
	}

	public void setHandleDelta(int dx, int dy) {
		suppressed.add(handleShape);
		handleDx = dx;
		handleDy = dy;
	}
	
	public void setMovingShapes(Collection<CanvasObject> shapes, int dx, int dy) {
		suppressed.addAll(shapes);
		moveDx = dx;
		moveDy = dy;
	}
	
	public Location getMovingDelta() {
		return Location.create(moveDx, moveDy);
	}
	
	public void setMovingDelta(int dx, int dy) {
		moveDx = dx;
		moveDy = dy;
	}
	
	public void drawSuppressed(Graphics g, CanvasObject shape) {
		if(shape == handleShape) {
			shape.paint(g, handleLocation, handleDx, handleDy);
		} else {
			g.translate(moveDx, moveDy);
			shape.paint(g, null, 0, 0);
		}
	}

	void modelChanged(CanvasModelEvent event) {
		int action = event.getAction();
		switch(action) {
		case CanvasModelEvent.ACTION_REMOVED:
			Collection<? extends CanvasObject> affected = event.getAffected();
			if (affected != null) {
				selected.removeAll(affected);
				suppressed.removeAll(affected);
				if(affected.contains(handleShape)) handleShape = null;
			}
			break;
		case CanvasModelEvent.ACTION_HANDLE_DELETED:
		case CanvasModelEvent.ACTION_HANDLE_INSERTED:
			CanvasObject hanShape = handleShape;
			if(hanShape != null && event.getAffected().contains(hanShape)) {
				handleShape = null;
			}
			break;
		}
	}
}
