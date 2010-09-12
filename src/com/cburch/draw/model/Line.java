/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Line2D;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.UnmodifiableList;

class Line extends DrawingMember {
	static final int ON_LINE_THRESH = 4;
	
	private int x0;
	private int y0;
	private int x1;
	private int y1;
	private Bounds bounds;
	private int strokeWidth;
	private Color strokeColor;
	
	public Line(int x0, int y0, int x1, int y1) {
		this.x0 = x0;
		this.y0 = y0;
		this.x1 = x1;
		this.y1 = y1;
		bounds = Bounds.create(x0, y0, 0, 0).add(x1, y1);
		strokeWidth = 1;
		strokeColor = Color.BLACK;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Line) {
			Line that = (Line) other;
			return this.x0 == that.x0
				&& this.y0 == that.x1
				&& this.x1 == that.y0
				&& this.y1 == that.y1
				&& this.strokeWidth == that.strokeWidth
				&& this.strokeColor.equals(that.strokeColor);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		int ret = x0 * 31 + y0;
		ret = ret * 31 + x1;
		ret = ret * 31 + y1;
		ret = ret * 31 + strokeWidth;
		ret = ret * 31 + strokeColor.hashCode();
		return ret;
	}

	
	@Override
	public Element toSvgElement(Document doc) {
		return SvgCreator.createLine(doc, this);
	}

	public Location getEnd0() {
		return Location.create(x0, y0);
	}
	
	public Location getEnd1() {
		return Location.create(x1, y1);
	}
	
	public String getDisplayName() {
		return Strings.get("shapeLine");
	}

	@Override
	public List<Attribute<?>> getAttributes() {
		return DrawAttr.ATTRS_STROKE;
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
	
	public boolean contains(Location loc) {
		int x = loc.getX();
		int y = loc.getY();
		double d = Line2D.ptLineDistSq(x0, y0, x1, y1, x, y);
		int thresh = Math.max(ON_LINE_THRESH, strokeWidth / 2);
		if (d < thresh * thresh) {
			if (x0 < x1) {
				if (x < x0 || x > x1) return false;
			} else {
				if (x < x1 || x > x0) return false;
			}
			if (y0 < y1) {
				if (y < y0 || y > y1) return false;
			} else {
				if (y < y1 || y > y0) return false;
			}
			return true;
		} else {
			return false;
		}
	}
	
	/*
	private double distanceSquared(int xq, int yq) {
		int x0 = this.x0;
		int y0 = this.y0;
		int x1 = this.x1;
		int y1 = this.y1;
		int dx = x1 - x0;
		int dy = y1 - y0;
		int dxq0 = xq - x0;
		int dyq0 = yq - y0;
		int rNum = dxq0 * dx + dyq0 * dy;
		int rDen = dx * dx + dy * dy;
		
		if (rNum >= 0 && rNum <= rDen) {
			int sNum = (y0 - yq) * dx - (x0 - xq) * dy;
			return (double) (sNum * sNum) / rDen;
		} else {
			double d0 = dxq0 * dxq0 + dyq0 * dyq0;
			double d1 = (xq-x1)*(xq-x1) + (yq-y1)*(yq-y1);
			return Math.min(d0, d1);
		}
	}
	*/
	
	@Override
	public void translate(int dx, int dy) {
		x0 += dx;
		y0 += dy;
		x1 += dx;
		y1 += dy;
	}
	
	public List<Location> getHandles() {
		return UnmodifiableList.create(new Location[] {
				Location.create(x0, y0), Location.create(x1, y1) });
	}
	
	public List<Location> getHandles(Location handle, int dx, int dy) {
		Location[] ret = { Location.create(x0, y0), Location.create(x1, y1) };
		if (ret[0].equals(handle)) ret[0] = ret[0].translate(dx, dy);
		if (ret[1].equals(handle)) ret[1] = ret[1].translate(dx, dy);
		return UnmodifiableList.create(ret);
	}
	
	@Override
	public void moveHandle(Location handle, int dx, int dy) {
		int hx = handle.getX();
		int hy = handle.getY();
		if (x0 == hx && y0 == hy) {
			x0 += dx;
			y0 += dy;
		}
		if (x1 == hx && y1 == hy) {
			x1 += dx;
			y1 += dy;
		}
		bounds = Bounds.create(x0, y0, 0, 0).add(x1, y1);
	}
	
	public void paint(Graphics g, Location handle, int dx, int dy) {
		if (setForStroke(g)) {
			int x0 = this.x0;
			int y0 = this.y0;
			int x1 = this.x1;
			int y1 = this.y1;
			int hx = handle == null ? Integer.MIN_VALUE : handle.getX();
			int hy = handle == null ? Integer.MIN_VALUE : handle.getY();
			if (x0 == hx && y0 == hy) {
				x0 += dx;
				y0 += dy;
			}
			if (x1 == hx && y1 == hy) {
				x1 += dx;
				y1 += dy;
			}
			g.drawLine(x0, y0, x1, y1);
		}
	}

}
