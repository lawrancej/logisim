/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.UnmodifiableList;

abstract class Rectangular extends DrawingMember {
	private Bounds bounds; // excluding the stroke's width
	private AttributeOption paintType;
	private int strokeWidth;
	private Color strokeColor;
	private Color fillColor;
	
	public Rectangular(int x, int y, int w, int h) {
		bounds = Bounds.create(x, y, w, h);
		paintType = DrawAttr.PAINT_STROKE;
		strokeWidth = 1;
		strokeColor = Color.BLACK;
		fillColor = Color.WHITE;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Rectangular) {
			Rectangular that = (Rectangular) other;
			return this.bounds.equals(that.bounds)
				&& this.paintType == that.paintType
				&& this.strokeWidth == that.strokeWidth
				&& this.strokeColor.equals(that.strokeColor)
				&& this.fillColor.equals(that.fillColor);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		int ret = bounds.hashCode();
		ret = ret * 31 + paintType.hashCode();
		ret = ret * 31 + strokeWidth;
		ret = ret * 31 + strokeColor.hashCode();
		ret = ret * 31 + fillColor.hashCode();
		return ret;
	}
	
	public int getX() {
		return bounds.getX();
	}
	
	public int getY() {
		return bounds.getY();
	}
	
	public int getWidth() {
		return bounds.getWidth();
	}
	
	public int getHeight() {
		return bounds.getHeight();
	}
	
	public AttributeOption getPaintType() {
		return paintType;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <V> V getValue(Attribute<V> attr) {
		if (attr == DrawAttr.PAINT_TYPE) {
			return (V) paintType;
		} else if (attr == DrawAttr.STROKE_COLOR) {
			return (V) strokeColor;
		} else if (attr == DrawAttr.FILL_COLOR) {
			return (V) fillColor;
		} else if (attr == DrawAttr.STROKE_WIDTH) {
			return (V) Integer.valueOf(strokeWidth);
		} else {
			return null;
		}
	}
	
	@Override
	public void updateValue(Attribute<?> attr, Object value) {
		if (attr == DrawAttr.PAINT_TYPE) {
			paintType = (AttributeOption) value;
			fireAttributeListChanged();
		} else if (attr == DrawAttr.STROKE_COLOR) {
			strokeColor = (Color) value;
		} else if (attr == DrawAttr.FILL_COLOR) {
			fillColor = (Color) value;
		} else if (attr == DrawAttr.STROKE_WIDTH) {
			strokeWidth = ((Integer) value).intValue();
		}
	}
	
	public Bounds getBounds() {
		int wid = strokeWidth;
		if (wid < 2) {
			return bounds;
		} else {
			return bounds.expand(wid / 2);
		}
	}
	
	@Override
	public void translate(int dx, int dy) {
		bounds = bounds.translate(dx, dy);
	}
	
	public List<Location> getHandles(Location handle, int dx, int dy) {
		Bounds bds = bounds;
		int rx = bds.getX();
		int ry = bds.getY();
		int rw = bds.getWidth();
		int rh = bds.getHeight();
		Location[] ret = new Location[4];
		int hx = handle == null ? Integer.MIN_VALUE : handle.getX();
		int hy = handle == null ? Integer.MIN_VALUE : handle.getY();
		for (int i = 0; i < ret.length; i++) {
			int x;
			int y;
			switch (i) {
			case 0:  x = rx;      y = ry; break;
			case 1:  x = rx + rw; y = ry; break;
			case 2:  x = rx + rw; y = ry + rh; break;
			default: x = rx;      y = ry + rh;
			}
			if (x == hx) {
				x += dx;
			}
			if (y == hy) {
				y += dy;
			}
			ret[i] = Location.create(x, y);
		}
		return UnmodifiableList.create(ret);
	}

	@Override
	public void moveHandle(Location handle, int dx, int dy) {
		Bounds bds = bounds;
		int x0 = bds.getX();
		int y0 = bds.getY();
		int x1 = x0 + bds.getWidth();
		int y1 = y0 + bds.getHeight();
		int xh = handle.getX();
		int yh = handle.getY();
		if (x0 == xh) x0 += dx;
		if (x1 == xh) x1 += dx;
		if (y0 == yh) y0 += dy;
		if (y1 == yh) y1 += dy;
		if (x1 < x0) {
			int t = x0; x0 = x1; x1 = t;
		}
		if (y1 < y0) {
			int t = y0; y0 = y1; y1 = t;
		}
		bounds = Bounds.create(x0, y0, x1 - x0, y1 - y0);
	}
	
	public void paint(Graphics g, Location handle, int handleDx, int handleDy) {
		if (handle == null) {
			Bounds bds = bounds;
			draw(g, bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
		} else {
			List<Location> handles = getHandles(handle, handleDx, handleDy);
			Location p0 = handles.get(0);
			Location p1 = handles.get(2);
			int x0 = p0.getX();
			int y0 = p0.getY();
			int x1 = p1.getX();
			int y1 = p1.getY();
			if (x1 < x0) { int t = x0; x0 = x1; x1 = t; }
			if (y1 < y0) { int t = y0; y0 = y1; y1 = t; }
	
			draw(g, x0, y0, x1 - x0, y1 - y0);
		}
	}
	
	protected int getStrokeWidth() {
		return strokeWidth;
	}
	
	public boolean contains(Location loc) {
		Bounds b = bounds;
		int x = b.getX();
		int y = b.getY();
		int w = b.getWidth();
		int h = b.getHeight();
		int qx = loc.getX();
		int qy = loc.getY();
		int tol = getStrokeWidth() / 2;
		if (qx >= x - tol && qx < x + w + tol
				&& qy >= y - tol && qy < y + h + tol) {
			return contains(x, y, w, h, loc);
		} else {
			return false;
		}
	}
	
	protected abstract boolean contains(int x, int y, int w, int h, Location q);
	protected abstract void draw(Graphics g, int x, int y, int w, int h);
}
