/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.canvas;

import java.awt.Graphics;
import java.util.Collection;
import java.util.Map;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;

public interface CanvasModel {
	// listener methods
	public void addCanvasModelListener(CanvasModelListener l);
	public void removeCanvasModelListener(CanvasModelListener l);
	
	// methods that don't change any data in the model
	public void paint(Graphics g, Selection selection);
	public Collection<CanvasObject> getObjects();
	public CanvasObject getObjectAt(int x, int y);
	public Collection<CanvasObject> getObjectsIn(Bounds bds);

	// methods that alter the model
	public void addObjects(Collection<? extends CanvasObject> shapes);
	public void removeObjects(Collection<? extends CanvasObject> shapes);
	public void translateObjects(Collection<? extends CanvasObject> shapes, int dx, int dy);
	public void moveHandle(CanvasObject shape, Location handle, int dx, int dy);
	public Location insertHandle(CanvasObject shape, Location handle, Location desiredLocation);
	public Location deleteHandle(CanvasObject shape, Location handle);
	public void setAttributeValues(Map<AttributeMapKey,Object> values);
}
