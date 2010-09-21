/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.shapes;

import java.awt.Graphics;
import java.util.List;

import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.model.Handle;
import com.cburch.draw.model.HandleGesture;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.UnmodifiableList;

abstract class Rectangular extends FillableCanvasObject {
	private Bounds bounds; // excluding the stroke's width
	
	public Rectangular(int x, int y, int w, int h) {
		bounds = Bounds.create(x, y, w, h);
	}
	
	@Override
	public boolean matches(CanvasObject other) {
		if (other instanceof Rectangular) {
			Rectangular that = (Rectangular) other;
			return this.bounds.equals(that.bounds)
				&& super.matches(that);
		} else {
			return false;
		}
	}

	@Override
	public int matchesHashCode() {
		return bounds.hashCode() * 31 + super.matchesHashCode();
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
	
	@Override
	public Bounds getBounds() {
		int wid = getStrokeWidth();
		Object type = getPaintType();
		if (wid < 2 || type == DrawAttr.PAINT_FILL) {
			return bounds;
		} else {
			return bounds.expand(wid / 2);
		}
	}
	
	@Override
	public void translate(int dx, int dy) {
		bounds = bounds.translate(dx, dy);
	}
	
	@Override
	public List<Handle> getHandles(HandleGesture gesture) {
		Bounds bds = bounds;
		int rx = bds.getX();
		int ry = bds.getY();
		int rw = bds.getWidth();
		int rh = bds.getHeight();
		Handle[] ret = new Handle[4];
		Handle h = gesture == null ? null : gesture.getHandle();
		int hx = h == null ? Integer.MIN_VALUE : h.getX();
		int hy = h == null ? Integer.MIN_VALUE : h.getY();
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
				x += gesture.getDeltaX();
			}
			if (y == hy) {
				y += gesture.getDeltaY();
			}
			ret[i] = new Handle(this, x, y);
		}
		return UnmodifiableList.create(ret);
	}
	
	@Override
	public boolean canMoveHandle(Handle handle) {
		return true;
	}

	@Override
	public Handle moveHandle(HandleGesture gesture) {
		Bounds bds = bounds;
		int x0 = bds.getX();
		int y0 = bds.getY();
		int x1 = x0 + bds.getWidth();
		int y1 = y0 + bds.getHeight();
		Handle h = gesture.getHandle();
		int xh = h.getX();
		int yh = h.getY();
		int dx = gesture.getDeltaX();
		int dy = gesture.getDeltaY();
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
		return new Handle(this, xh + dx, yh + dy);
	}
	
	@Override
	public void paint(Graphics g, HandleGesture gesture) {
		if (gesture == null) {
			Bounds bds = bounds;
			draw(g, bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
		} else {
			List<Handle> handles = getHandles(gesture);
			Handle p0 = handles.get(0);
			Handle p1 = handles.get(2);
			int x0 = p0.getX();
			int y0 = p0.getY();
			int x1 = p1.getX();
			int y1 = p1.getY();
			if (x1 < x0) { int t = x0; x0 = x1; x1 = t; }
			if (y1 < y0) { int t = y0; y0 = y1; y1 = t; }
	
			draw(g, x0, y0, x1 - x0, y1 - y0);
		}
	}
	
	@Override
	public boolean contains(Location loc, boolean assumeFilled) {
		Object type = getPaintType();
		if (assumeFilled && type == DrawAttr.PAINT_STROKE) {
			type = DrawAttr.PAINT_STROKE_FILL;
		}
		Bounds b = bounds;
		int x = b.getX();
		int y = b.getY();
		int w = b.getWidth();
		int h = b.getHeight();
		int qx = loc.getX();
		int qy = loc.getY();
		if (type == DrawAttr.PAINT_FILL) {
			return isInRect(qx, qy, x, y, w, h) && contains(x, y, w, h, loc);
		} else if (type == DrawAttr.PAINT_STROKE) {
			int stroke = getStrokeWidth();
			int tol2 = Math.max(2 * Line.ON_LINE_THRESH, stroke);
			int tol = tol2 / 2;
			return isInRect(qx, qy, x - tol, y - tol, w + tol2, h + tol2)
					&& contains(x - tol, y - tol, w + tol2, h + tol2, loc)
					&& !contains(x + tol, y + tol, w - tol2, h - tol2, loc);
		} else if (type == DrawAttr.PAINT_STROKE_FILL) {
			int stroke = getStrokeWidth();
			int tol2 = stroke;
			int tol = tol2 / 2;
			return isInRect(qx, qy, x - tol, y - tol, w + tol2, h + tol2)
				&& contains(x - tol, y - tol, w + tol2, h + tol2, loc);
		} else {
			return false;
		}
	}
	
	boolean isInRect(int qx, int qy, int x0, int y0, int w, int h) {
		return qx >= x0 && qx < x0 + w && qy >= y0 && qy < y0 + h;
	}
	
	protected abstract boolean contains(int x, int y, int w, int h, Location q);
	protected abstract void draw(Graphics g, int x, int y, int w, int h);
}
