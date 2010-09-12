/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import java.awt.Graphics;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Location;

class RoundRectangle extends Rectangular {
	private int radius;
	
	public RoundRectangle(int x, int y, int w, int h) {
		super(x, y, w, h);
		this.radius = 10;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof RoundRectangle) {
			RoundRectangle that = (RoundRectangle) other;
			return super.equals(other) && this.radius == that.radius;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() * 31 + radius;
	}

	
	public String getDisplayName() {
		return Strings.get("shapeRoundRect");
	}
	
	@Override
	public Element toSvgElement(Document doc) {
		return SvgCreator.createRoundRectangle(doc, this);
	}
	
	@Override
	public List<Attribute<?>> getAttributes() {
		return DrawAttr.getRoundRectAttributes(getPaintType());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <V> V getValue(Attribute<V> attr) {
		if (attr == DrawAttr.CORNER_RADIUS) {
			return (V) Integer.valueOf(radius);
		} else {
			return super.getValue(attr);
		}
	}

	@Override
	public void updateValue(Attribute<?> attr, Object value) {
		if (attr == DrawAttr.CORNER_RADIUS) {
			radius = ((Integer) value).intValue();
		} else {
			super.updateValue(attr, value);
		}
	}
	
	@Override
	protected boolean contains(int x, int y, int w, int h, Location q) {
		int qx = q.getX();
		int qy = q.getY();
		int r = radius + getStrokeWidth() / 2;
		if (qx < x + r) {
			if (qy < y + r) return inCircle(qx, qy, x + r, y + r, r);
			else if (qy < y + h - r) return true;
			else return inCircle(qx, qy, x + r, y + h - r, r);
		} else if (qx < x + w - r) {
			return true;
		} else {
			if (qy < y + r) return inCircle(qx, qy, x + w - r, y + r, r);
			else if (qy < y + h - r) return true;
			else return inCircle(qx, qy, x + w - r, y + h - r, r);
		}
	}
	
	private static boolean inCircle(int qx, int qy, int cx, int cy, int r) {
		int dx = qx - cx;
		int dy = qy - cy;
		return dx * dx + dy * dy < r * r;
	}
	
	@Override
	public void draw(Graphics g, int x, int y, int w, int h) {
		int diam = 2 * radius;
		if (setForFill(g)) g.fillRoundRect(x, y, w, h, diam, diam);
		if (setForStroke(g)) g.drawRoundRect(x, y, w, h, diam, diam);
	}
}
