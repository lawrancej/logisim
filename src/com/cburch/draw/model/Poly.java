/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Line2D;
import java.util.List;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.UnmodifiableList;

abstract class Poly extends DrawingMember {
	private Location[] locations;
	private int[] xs;
	private int[] ys;
	private Bounds bounds;
	private int strokeWidth;
	private Color strokeColor;
	
	public Poly(List<Location> locations) {
		Location[] locs = locations.toArray(new Location[locations.size()]);
		
		this.locations = locs;
		xs = new int[locs.length];
		ys = new int[locs.length];
		recomputeBounds();
		strokeWidth = 1;
		strokeColor = Color.BLACK;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Poly) {
			Poly that = (Poly) other;
			if (this.locations.length != that.locations.length) return false;
			for (int i = 0, n = this.locations.length; i < n; i++) {
				if (!this.locations[i].equals(that.locations[i])) return false;
			}
			return this.strokeWidth == that.strokeWidth
				&& this.strokeColor.equals(that.strokeColor);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		int ret = strokeWidth * 31 + strokeColor.hashCode();
		for (Location loc : this.locations) {
			ret = (ret * 31 + loc.getX()) * 31 + loc.getY();
		}
		return ret;
	}
	
	@Override
	public Poly clone() {
		Poly ret = (Poly) super.clone();
		ret.locations = this.locations.clone();
		ret.xs = this.xs.clone();
		ret.ys = this.ys.clone();
		return ret;
	}
	
	public List<Location> getVertices() {
		return UnmodifiableList.create(locations);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <V> V getValue(Attribute<V> attr) {
		if (attr == DrawAttr.STROKE_COLOR) {
			return (V) strokeColor;
		} else if (attr == DrawAttr.STROKE_WIDTH) {
			return (V) Integer.valueOf(strokeWidth);
		} else {
			return null;
		}
	}
	
	@Override
	public void updateValue(Attribute<?> attr, Object value) {
		if (attr == DrawAttr.STROKE_COLOR) {
			strokeColor = (Color) value;
		} else if (attr == DrawAttr.STROKE_WIDTH) {
			strokeWidth = ((Integer) value).intValue();
		}
	}
	
	public Bounds getBounds() {
		return bounds;
	}
	
	@Override
	public void translate(int dx, int dy) {
		Location[] locs = locations;
		for(int i = 0; i < locs.length; i++) {
			locs[i] = locs[i].translate(dx, dy);
		}
		recomputeBounds();
	}
	
	public List<Location> getHandles(Location handle, int dx, int dy) {
		Location[] locs = locations;
		Location[] ret = new Location[locs.length];
		for (int i = 0, n = locs.length; i < n; i++) {
			Location loc = locs[i];
			if (loc.equals(handle)) ret[i] = loc.translate(dx, dy);
			else ret[i] = loc;
		}
		return UnmodifiableList.create(ret);
	}
	
	@Override
	public void moveHandle(Location handle, int dx, int dy) {
		Location[] locs = locations;
		for (int i = 0, n = locs.length; i < n; i++) {
			if (locs[i].equals(handle)) locs[i] = handle.translate(dx, dy);
		}
		recomputeBounds();
	}
	
	@Override
	public boolean canInsertHandle(Location handle) {
		return true;
	}
	
	@Override
	public boolean canDeleteHandle(Location handle) {
		return locations.length >= 4;
	}
	
	@Override
	public Location insertHandle(Location handle, Location desiredLocation) {
		Location[] oldLocs = locations;
		Location[] newLocs = new Location[oldLocs.length + 1];
		Location addedHandle = null;
		for(int i = 0; i < newLocs.length; i++) {
			if (addedHandle != null) {
				newLocs[i] = oldLocs[i - 1];
			} else if (oldLocs[i].equals(handle)) { // this is what we're removing
				if (desiredLocation == null) {
					Location a = oldLocs[i];
					Location b = oldLocs[(i + oldLocs.length - 1) % oldLocs.length];
					int x = (a.getX() + b.getX()) / 2;
					int y = (a.getY() + b.getY()) / 2;
					addedHandle = Location.create(x, y);
				} else {
					addedHandle = desiredLocation;
				}
				newLocs[i] = addedHandle;
			} else {
				newLocs[i] = oldLocs[i];
			}
		}
		if (addedHandle == null) {
			throw new IllegalArgumentException("no such handle");
		}
		locations = newLocs;
		xs = new int[newLocs.length];
		ys = new int[newLocs.length];
		recomputeBounds();
		return addedHandle;
	}
	
	@Override
	public Location deleteHandle(Location handle) {
		Location[] oldLocs = locations;
		Location[] newLocs = new Location[oldLocs.length - 1];
		Location ret = null;
		for(int i = 0; i < oldLocs.length; i++) {
			if (ret != null) {
				newLocs[i - 1] = oldLocs[i];
			} else if (oldLocs[i].equals(handle)) {
				ret = oldLocs[(i + (oldLocs.length - 1)) % oldLocs.length];
			} else {
				newLocs[i] = oldLocs[i];
			}
		}
		locations = newLocs;
		xs = new int[newLocs.length];
		ys = new int[newLocs.length];
		recomputeBounds();
		return ret;
	}

	protected void recomputeBounds() {
		Location[] locs = locations;
		Bounds bds = Bounds.create(locs[0]);
		for(int i = 1; i < locs.length; i++) bds = bds.add(locs[i]);
		bounds = strokeWidth < 2 ? bds : bds.expand(strokeWidth / 2);
	}
	
	public void draw(Graphics g, int xOffs, int yOffs) {
		int[] x = xs;
		int[] y = ys;
		Location[] locs = locations;
		for(int i = 0; i < xs.length; i++) {
			x[i] = locs[i].getX() + xOffs;
			y[i] = locs[i].getY() + yOffs;
		}
		draw(g, x, y);
	}
	
	public void paint(Graphics g, Location handle, int handleDx, int handleDy) {
		int[] x = xs;
		int[] y = ys;
		int hx = handle == null ? Integer.MIN_VALUE : handle.getX();
		int hy = handle == null ? Integer.MIN_VALUE : handle.getY();
		Location[] locs = locations;
		for(int i = 0; i < xs.length; i++) {
			int xx = locs[i].getX();
			int yy = locs[i].getY();
			if (xx == hx && yy == hy) {
				xx += handleDx;
				yy += handleDy;
			}
			x[i] = xx;
			y[i] = yy;
		}
		draw(g, x, y);
	}
	
	protected int getStrokeWidth() {
		return strokeWidth;
	}
	
	protected double ptBorderDistSq(Location loc) {
		int xq = loc.getX();
		int yq = loc.getY();
		Location[] locs = locations;
		double min = Double.MAX_VALUE;
		if (locs.length > 0) {
			Location first = locs[0];
			int x0 = first.getX();
			int y0 = first.getY();
			for(int i = 1; i < locs.length; i++) {
				Location next = locs[i];
				int x1 = next.getX();
				int y1 = next.getY();
				double d = Line2D.ptLineDistSq(x0, y0, x1, y1, xq, yq);
				if (d < min) min = d;
				x0 = x1;
				y0 = y1;
			}
		}
		return min;
	}

	public abstract boolean contains(Location loc);
	protected abstract void draw(Graphics g, int[] x, int[] y);
}
