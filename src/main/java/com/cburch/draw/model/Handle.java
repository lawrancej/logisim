/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import com.cburch.logisim.data.Location;

public class Handle {
	private CanvasObject object;
	private int x;
	private int y;
	
	public Handle(CanvasObject object, int x, int y) {
		this.object = object;
		this.x = x;
		this.y = y;
	}
	
	public Handle(CanvasObject object, Location loc) {
		this(object, loc.getX(), loc.getY());
	}
	
	public CanvasObject getObject() {
		return object;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public Location getLocation() {
		return Location.create(x, y);
	}
	
	public boolean isAt(Location loc) {
		return x == loc.getX() && y == loc.getY();
	}
	
	public boolean isAt(int xq, int yq) {
		return x == xq && y == yq;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Handle) {
			Handle that = (Handle) other;
			return this.object.equals(that.object) && this.x == that.x
				&& this.y == that.y;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return (this.object.hashCode() * 31 + x) * 31 + y;
	}
}
