/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.canvas;

import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.cburch.logisim.data.Location;

public class CanvasModelEvent extends EventObject {
	public static final int ACTION_ADDED = 0;
	public static final int ACTION_REMOVED = 1;
	public static final int ACTION_TRANSLATED = 2;
	public static final int ACTION_HANDLE_MOVED = 3;
	public static final int ACTION_HANDLE_INSERTED = 4;
	public static final int ACTION_HANDLE_DELETED = 5;
	public static final int ACTION_ATTRIBUTE_CHANGED = 6;
	
	private int action;
	private Collection<? extends CanvasObject> affected;
	private Map<AttributeMapKey, Object> oldValues;
	private Map<AttributeMapKey, Object> newValues;
	private Location handle;
	private int dx;
	private int dy;
	
	public CanvasModelEvent(CanvasModel source, int action,
			Collection<? extends CanvasObject> affected) {
		this(source, action, affected, null, 0, 0);
	}
	
	public CanvasModelEvent(CanvasModel source, int action,
			Collection<? extends CanvasObject> affected, Location handle) {
		this(source, action, affected, handle, 0, 0);
	}
	
	public CanvasModelEvent(CanvasModel source, int action,
			Collection<? extends CanvasObject> affected, int dx, int dy) {
		this(source, action, affected, null, dx, dy);
	}
	
	public CanvasModelEvent(CanvasModel source, int action,
			Collection<? extends CanvasObject> affected, Location handle,
			int dx, int dy) {
		super(source);
		this.action = action;
		this.affected = new HashSet<CanvasObject>(affected);
		this.handle = handle;
		this.dx = dx;
		this.dy = dy;
		this.oldValues = null;
		this.newValues = null;
	}
	
	public CanvasModelEvent(CanvasModel source, int action,
			Map<AttributeMapKey, Object> oldValues,
			Map<AttributeMapKey, Object> newValues) {
		super(source);
		
		Map<AttributeMapKey, Object> oldValuesCopy;
		oldValuesCopy = new HashMap<AttributeMapKey, Object>(oldValues);
		Map<AttributeMapKey, Object> newValuesCopy;
		newValuesCopy = new HashMap<AttributeMapKey, Object>(newValues);

		this.action = action;
		this.affected = null;
		this.handle = null;
		this.dx = 0;
		this.dy = 0;
		this.oldValues = Collections.unmodifiableMap(oldValuesCopy);
		this.newValues = Collections.unmodifiableMap(newValuesCopy);
	}
	
	public int getAction() {
		return action;
	}
	
	public Collection<? extends CanvasObject> getAffected() {
		Collection<? extends CanvasObject> ret = affected;
		if(ret == null) {
			Map<AttributeMapKey, Object> newVals = newValues;
			if(newVals != null) {
				HashSet<CanvasObject> keys = new HashSet<CanvasObject>();
				for (AttributeMapKey key : newVals.keySet()) {
					keys.add(key.getObject());
				}
				ret = Collections.unmodifiableCollection(keys);
				affected = ret;
			}
		}
		return affected;
	}
	
	public Location getHandle() {
		return handle;
	}
	
	public int getDeltaX() {
		return dx;
	}
	
	public int getDeltaY() {
		return dy;
	}
	
	public Map<AttributeMapKey, Object> getOldValues() {
		return oldValues;
	}
	
	public Map<AttributeMapKey, Object> getNewValues() {
		return newValues;
	}
}
