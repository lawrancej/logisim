/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.canvas;

import java.awt.Graphics;
import java.util.List;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;

public interface CanvasObject {
	public abstract CanvasObject clone();
	public abstract String getDisplayName();
	public abstract AttributeSet getAttributeSet();
	public abstract Bounds getBounds();
	public abstract boolean contains(Location loc);
	public abstract List<Location> getHandles(Location handle, int dx, int dy);
	public abstract boolean canRemove();
	public abstract boolean canMoveHandle(Location handle);
	public abstract boolean canInsertHandle(Location handle);
	public abstract boolean canDeleteHandle(Location handle);
	public abstract void paint(Graphics g, Location handle,
			int handleDx, int handleDy);
	
	public void translate(int dx, int dy);
	public <V> void setValue(Attribute<V> attr, V value);
}
