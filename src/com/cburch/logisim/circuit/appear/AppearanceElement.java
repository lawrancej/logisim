/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit.appear;

import java.awt.Graphics;
import java.util.Collections;
import java.util.List;

import com.cburch.draw.model.DrawingMember;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;

public abstract class AppearanceElement extends DrawingMember {
	private Location location;
	
	public AppearanceElement(Location location) {
		this.location = location;
	}
	
	public Location getLocation() {
		return location;
	}

	@Override
	public List<Attribute<?>> getAttributes() {
		return Collections.emptyList();
	}

	@Override
	public <V> V getValue(Attribute<V> attr) {
		return null;
	}
	
	@Override
	public boolean canRemove() {
		return false;
	}
	
	@Override
	public boolean canMoveHandle(Location handle) {
		return false;
	}

	@Override
	public void moveHandle(Location handle, int dx, int dy) {
		// nothing to do
	}

	@Override
	protected void updateValue(Attribute<?> attr, Object value) {
		// nothing to do
	}

	@Override
	public void translate(int dx, int dy) {
		location = location.translate(dx, dy);
	}

	protected boolean isInCircle(Location loc, int radius) {
		int dx = loc.getX() - location.getX();
		int dy = loc.getY() - location.getY();
		return dx * dx + dy * dy < radius * radius;
	}

	protected Bounds getBounds(int radius) {
		return Bounds.create(location.getX() - radius, location.getY() - radius,
				2 * radius, 2 * radius);
	}

	public abstract void paint(Graphics g, Location handle, int handleDx, int handleDy);

	public abstract String getDisplayName();
}
