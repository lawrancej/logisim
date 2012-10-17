/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class DrawingOverlaps {
	private Map<CanvasObject, List<CanvasObject>> map;
	private Set<CanvasObject> untested;
	
	public DrawingOverlaps() {
		map = new HashMap<CanvasObject, List<CanvasObject>>();
		untested = new HashSet<CanvasObject>();
	}
	
	public Collection<CanvasObject> getObjectsOverlapping(CanvasObject o) {
		ensureUpdated();

		List<CanvasObject> ret = map.get(o);
		if (ret == null || ret.isEmpty()) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(ret);
		}
	}
	
	private void ensureUpdated() {
		for (CanvasObject o : untested) {
			ArrayList<CanvasObject> over = new ArrayList<CanvasObject>();
			for (CanvasObject o2 : map.keySet()) {
				if (o != o2 && o.overlaps(o2)) {
					over.add(o2);
					addOverlap(o2, o);
				}
			}
			map.put(o, over);
		}
		untested.clear();
	}
	
	private void addOverlap(CanvasObject a, CanvasObject b) {
		List<CanvasObject> alist = map.get(a);
		if (alist == null) {
			alist = new ArrayList<CanvasObject>();
			map.put(a, alist);
		}
		if (!alist.contains(b)) {
			alist.add(b);
		}
	}
	
	public void addShape(CanvasObject shape) {
		untested.add(shape);
	}
	
	public void removeShape(CanvasObject shape) {
		untested.remove(shape);
		List<CanvasObject> mapped = map.remove(shape);
		if (mapped != null) {
			for (CanvasObject o : mapped) {
				List<CanvasObject> reverse = map.get(o);
				if (reverse != null) {
					reverse.remove(shape);
				}
			}
		}
	}
	
	public void invalidateShape(CanvasObject shape) {
		removeShape(shape);
		untested.add(shape);
	}
	
	public void invalidateShapes(Collection<? extends CanvasObject> shapes) {
		for (CanvasObject o : shapes) {
			invalidateShape(o);
		}
	}
}
