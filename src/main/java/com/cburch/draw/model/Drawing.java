/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cburch.draw.canvas.Selection;
import com.cburch.draw.shapes.Text;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.util.EventSourceWeakSupport;

public class Drawing implements CanvasModel {
	private EventSourceWeakSupport<CanvasModelListener> listeners;
	private ArrayList<CanvasObject> canvasObjects;
	private DrawingOverlaps overlaps;
	
	public Drawing() {
		listeners = new EventSourceWeakSupport<CanvasModelListener>();
		canvasObjects = new ArrayList<CanvasObject>();
		overlaps = new DrawingOverlaps();
	}
	
	public void addCanvasModelListener(CanvasModelListener l) {
		listeners.add(l);
	}
	
	public void removeCanvasModelListener(CanvasModelListener l) {
		listeners.remove(l);
	}
	
	protected boolean isChangeAllowed(CanvasModelEvent e) {
		return true;
	}

	private void fireChanged(CanvasModelEvent e) {
		for (CanvasModelListener listener : listeners) {
			listener.modelChanged(e);
		}
	}

	public void paint(Graphics g, Selection selection) {
		Set<CanvasObject> suppressed = selection.getDrawsSuppressed();
		for (CanvasObject shape : getObjectsFromBottom()) {
			Graphics dup = g.create();
			if (suppressed.contains(shape)) {
				selection.drawSuppressed(dup, shape);
			} else {
				shape.paint(dup, null);
			}
			dup.dispose();
		}
	}
	
	public List<CanvasObject> getObjectsFromTop() {
		ArrayList<CanvasObject> ret = new ArrayList<CanvasObject>(getObjectsFromBottom());
		Collections.reverse(ret);
		return ret;
	}
	
	public List<CanvasObject> getObjectsFromBottom() {
		return Collections.unmodifiableList(canvasObjects);
	}
	
	public Collection<CanvasObject> getObjectsIn(Bounds bds) {
		ArrayList<CanvasObject> ret = null;
		for (CanvasObject shape : getObjectsFromBottom()) {
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
	
	public Collection<CanvasObject> getObjectsOverlapping(CanvasObject shape) {
		return overlaps.getObjectsOverlapping(shape);
	}

	public void addObjects(int index, Collection<? extends CanvasObject> shapes) {
		LinkedHashMap<CanvasObject, Integer> indexes;
		indexes = new LinkedHashMap<CanvasObject, Integer>();
		int i = index;
		for (CanvasObject shape : shapes) {
			indexes.put(shape, Integer.valueOf(i));
			i++;
		}
		addObjectsHelp(indexes);
	}

	public void addObjects(Map<? extends CanvasObject, Integer> shapes) {
		addObjectsHelp(shapes);
	}
	
	private void addObjectsHelp(Map<? extends CanvasObject, Integer> shapes) {
		// this is separate method so that subclass can call super.add to either
		// of the add methods, and it won't get redirected into the subclass
		// in calling the other add method
		CanvasModelEvent e = CanvasModelEvent.forAdd(this, shapes.keySet());
		if (!shapes.isEmpty() && isChangeAllowed(e)) {
			for (Map.Entry<? extends CanvasObject, Integer> entry : shapes.entrySet()) {
				CanvasObject shape = entry.getKey();
				int index = entry.getValue().intValue();
				canvasObjects.add(index, shape);
				overlaps.addShape(shape);
			}
			fireChanged(e);
		}
	}
	
	public void removeObjects(Collection<? extends CanvasObject> shapes) {
		List<CanvasObject> found = restrict(shapes);
		CanvasModelEvent e = CanvasModelEvent.forRemove(this, found);
		if (!found.isEmpty() && isChangeAllowed(e)) {
			for (CanvasObject shape : found) {
				canvasObjects.remove(shape);
				overlaps.removeShape(shape);
			}
			fireChanged(e);
		}
	}
	
	public void translateObjects(Collection<? extends CanvasObject> shapes,
			int dx, int dy) {
		List<CanvasObject> found = restrict(shapes);
		CanvasModelEvent e = CanvasModelEvent.forTranslate(this, found, dx, dy);
		if (!found.isEmpty() && (dx != 0 || dy != 0) && isChangeAllowed(e)) {
			for (CanvasObject shape : shapes) {
				shape.translate(dx, dy);
				overlaps.invalidateShape(shape);
			}
			fireChanged(e);
		}
	}
	
	public void reorderObjects(List<ReorderRequest> requests) {
		boolean hasEffect = false;
		for (ReorderRequest r : requests) {
			if (r.getFromIndex() != r.getToIndex()) {
				hasEffect = true;
			}
		}
		CanvasModelEvent e = CanvasModelEvent.forReorder(this, requests);
		if (hasEffect && isChangeAllowed(e)) {
			for (ReorderRequest r : requests) {
				if (canvasObjects.get(r.getFromIndex()) != r.getObject()) {
					throw new IllegalArgumentException("object not present"
							+ " at indicated index: " + r.getFromIndex());
				}
				canvasObjects.remove(r.getFromIndex());
				canvasObjects.add(r.getToIndex(), r.getObject());
			}
			fireChanged(e);
		}
	}

	public Handle moveHandle(HandleGesture gesture) {
		CanvasModelEvent e = CanvasModelEvent.forMoveHandle(this, gesture);
		CanvasObject o = gesture.getHandle().getObject();
		if (canvasObjects.contains(o)
				&& (gesture.getDeltaX() != 0 || gesture.getDeltaY() != 0)
				&& isChangeAllowed(e)) {
			Handle moved = o.moveHandle(gesture);
			gesture.setResultingHandle(moved);
			overlaps.invalidateShape(o);
			fireChanged(e);
			return moved;
		} else {
			return null;
		}
	}

	public void insertHandle(Handle desired, Handle previous) {
		CanvasObject obj = desired.getObject();
		CanvasModelEvent e = CanvasModelEvent.forInsertHandle(this, desired);
		if (isChangeAllowed(e)) {
			obj.insertHandle(desired, previous);
			overlaps.invalidateShape(obj);
			fireChanged(e);
		}
	}
	
	public Handle deleteHandle(Handle handle) {
		CanvasModelEvent e = CanvasModelEvent.forDeleteHandle(this, handle);
		if (isChangeAllowed(e)) {
			CanvasObject o = handle.getObject();
			Handle ret = o.deleteHandle(handle);
			overlaps.invalidateShape(o);
			fireChanged(e);
			return ret;
		} else {
			return null;
		}
	}
	
	public void setAttributeValues(Map<AttributeMapKey, Object> values) {
		HashMap<AttributeMapKey, Object> oldValues;
		oldValues = new HashMap<AttributeMapKey, Object>();
		for (AttributeMapKey key : values.keySet()) {
			@SuppressWarnings("unchecked")
			Attribute<Object> attr = (Attribute<Object>) key.getAttribute();
			Object oldValue = key.getObject().getValue(attr);
			oldValues.put(key, oldValue);
		}
		CanvasModelEvent e = CanvasModelEvent.forChangeAttributes(this, oldValues, values);
		if (isChangeAllowed(e)) {
			for (Map.Entry<AttributeMapKey, Object> entry : values.entrySet()) {
				AttributeMapKey key = entry.getKey();
				CanvasObject shape = key.getObject();
				@SuppressWarnings("unchecked")
				Attribute<Object> attr = (Attribute<Object>) key.getAttribute();
				shape.setValue(attr, entry.getValue());
				overlaps.invalidateShape(shape);
			}
			fireChanged(e);
		}
	}

	public void setText(Text text, String value) {
		String oldValue = text.getText();
		CanvasModelEvent e = CanvasModelEvent.forChangeText(this, text,
				oldValue, value);
		if (canvasObjects.contains(text) && !oldValue.equals(value)
				&& isChangeAllowed(e)) {
			text.setText(value);
			overlaps.invalidateShape(text);
			fireChanged(e);
		}
	}

	private ArrayList<CanvasObject> restrict(
			Collection<? extends CanvasObject> shapes) {
		ArrayList<CanvasObject> ret;
		ret = new ArrayList<CanvasObject>(shapes.size());
		for (CanvasObject shape : shapes) {
			if (canvasObjects.contains(shape)) {
				ret.add(shape);
			}
		}
		return ret;
	}
}
