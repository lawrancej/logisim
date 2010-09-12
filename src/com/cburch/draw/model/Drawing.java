/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.cburch.draw.canvas.AttributeMapKey;
import com.cburch.draw.canvas.CanvasModel;
import com.cburch.draw.canvas.CanvasModelEvent;
import com.cburch.draw.canvas.CanvasModelListener;
import com.cburch.draw.canvas.CanvasObject;
import com.cburch.draw.canvas.Selection;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.EventSourceWeakSupport;

public class Drawing implements CanvasModel {
	private EventSourceWeakSupport<CanvasModelListener> listeners;
	private ArrayList<CanvasObject> canvasObjects;
	
	public Drawing() {
		listeners = new EventSourceWeakSupport<CanvasModelListener>();
		canvasObjects = new ArrayList<CanvasObject>();
	}
	
	public void addCanvasModelListener(CanvasModelListener l) {
		listeners.add(l);
	}
	
	public void removeCanvasModelListener(CanvasModelListener l) {
		listeners.remove(l);
	}

	private void fireChanged(int action,
			Collection<? extends CanvasObject> affected,
			Location handle, int dx, int dy) {
		CanvasModelEvent e = null;
		for (CanvasModelListener listener : listeners) {
			if (e == null) {
				e = new CanvasModelEvent(this, action, affected, handle, dx, dy);
			}
			listener.modelChanged(e);
		}
	}

	private void fireChanged(int action, CanvasObject affected,
			Location handle, int dx, int dy) {
		CanvasModelEvent e = null;
		for (CanvasModelListener listener : listeners) {
			if (e == null) {
				Collection<CanvasObject> coll = Collections.singleton(affected);
				e = new CanvasModelEvent(this, action, coll, handle, dx, dy);
			}
			listener.modelChanged(e);
		}
	}

	private void fireChanged(int action, Map<AttributeMapKey, Object> oldValues,
			Map<AttributeMapKey, Object> newValues) {
		CanvasModelEvent e = null;
		for (CanvasModelListener listener : listeners) {
			if (e == null) {
				e = new CanvasModelEvent(this, action, oldValues, newValues);
			}
			listener.modelChanged(e);
		}
	}

	public void paint(Graphics g, Selection selection) {
		Set<CanvasObject> suppressed = selection.getDrawsSuppressed();
		for (CanvasObject shape : getObjects()) {
			Graphics dup = g.create();
			if (suppressed.contains(shape)) {
				selection.drawSuppressed(dup, shape);
			} else {
				shape.paint(dup, null, 0, 0);
			}
			dup.dispose();
		}
	}
	
	public Collection<CanvasObject> getObjects() {
		return Collections.unmodifiableList(canvasObjects);
	}

	public CanvasObject getObjectAt(int x, int y) {
		Location loc = Location.create(x, y);
		for (int i = canvasObjects.size() - 1; i >= 0; i--) {
			CanvasObject shape = canvasObjects.get(i);
			if (shape.contains(loc)) return shape;
		}
		return null;
	}
	
	public Collection<CanvasObject> getObjectsIn(Bounds bds) {
		ArrayList<CanvasObject> ret = null;
		for (CanvasObject shape : getObjects()) {
			if (bds.contains(shape.getBounds())) {
				if (ret == null) ret = new ArrayList<CanvasObject>();
				ret.add(shape);
			}
		}
		if (ret == null) {
			return Collections.emptyList();
		} else {
			return ret;
		}
	}

	public void addObjects(Collection<? extends CanvasObject> shapes) {
		ArrayList<CanvasObject> added = new ArrayList<CanvasObject>(shapes.size());
		for(CanvasObject shape : shapes) {
			if (!canvasObjects.contains(shape)) {
				canvasObjects.add(shape);
				added.add(shape);
			}
		}
		if (!added.isEmpty()) {
			fireChanged(CanvasModelEvent.ACTION_ADDED, added, null, 0, 0);
		}
	}
	
	public void removeObjects(Collection<? extends CanvasObject> shapes) {
		ArrayList<CanvasObject> removed = new ArrayList<CanvasObject>(shapes.size());
		for (CanvasObject shape : shapes) {
			boolean done = canvasObjects.remove(shape);
			if (done) {
				removed.add(shape);
			}
		}
		if (!removed.isEmpty()) {
			fireChanged(CanvasModelEvent.ACTION_REMOVED, removed, null, 0, 0);
		}
	}
	
	public void translateObjects(Collection<? extends CanvasObject> shapes,
			int dx, int dy) {
		if(dx != 0 || dy != 0) {
			boolean found = false;
			for (CanvasObject shape : shapes) {
				if (canvasObjects.contains(shape)) {
					found = true;
					shape.translate(dx, dy);
				}
			}
			if (found) {
				fireChanged(CanvasModelEvent.ACTION_TRANSLATED, shapes, null, dx, dy);
			}
		}
	}

	public void moveHandle(CanvasObject shape, Location handle, int dx, int dy) {
		if (canvasObjects.contains(shape) && (dx != 0 || dy != 0)) {
			((DrawingMember) shape).moveHandle(handle, dx, dy);
			fireChanged(CanvasModelEvent.ACTION_HANDLE_MOVED, shape, handle, dx, dy);
		}
	}

	public Location insertHandle(CanvasObject shape, Location handle, Location desiredLoc) {
		Location inserted = ((DrawingMember) shape).insertHandle(handle, desiredLoc);
		fireChanged(CanvasModelEvent.ACTION_HANDLE_INSERTED, shape, handle, 0, 0);
		return inserted;
	}
	
	public Location deleteHandle(CanvasObject shape, Location handle) {
		Location remainingHandle = ((DrawingMember) shape).deleteHandle(handle);
		fireChanged(CanvasModelEvent.ACTION_HANDLE_DELETED, shape, handle, 0, 0);
		return remainingHandle;
	}
	
	public void setAttributeValues(Map<AttributeMapKey, Object> values) {
		HashMap<AttributeMapKey, Object> oldValues;
		oldValues = new HashMap<AttributeMapKey, Object>();
		for (Map.Entry<AttributeMapKey, Object> entry : values.entrySet()) {
			AttributeMapKey key = entry.getKey();
			Object value = entry.getValue();
			@SuppressWarnings("unchecked")
			Attribute<Object> attr = (Attribute<Object>) key.getAttribute();
			DrawingMember shape = (DrawingMember) key.getObject();
			oldValues.put(key, shape.getValue(attr));
			shape.setValue(attr, value);
		}
		fireChanged(CanvasModelEvent.ACTION_ATTRIBUTE_CHANGED, oldValues, values);
	}
}
