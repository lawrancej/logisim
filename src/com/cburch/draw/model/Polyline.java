/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import java.awt.Graphics;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Location;

class Polyline extends Poly {
	public Polyline(List<Location> locations) {
		super(locations);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Polyline) {
			return super.equals(other);
		} else {
			return false;
		}
	}

	public String getDisplayName() {
		return Strings.get("shapePolyline");
	}
	
	@Override
	public Element toSvgElement(Document doc) {
		return SvgCreator.createPolyline(doc, this);
	}
	
	@Override
	public List<Attribute<?>> getAttributes() {
		return DrawAttr.ATTRS_STROKE;
	}

	public boolean canInsertHandle(int handleIndex) {
		return handleIndex > 0;
	}

	@Override
	public boolean contains(Location loc) {
		int thresh = Math.max(Line.ON_LINE_THRESH, getStrokeWidth() / 2);
		return ptBorderDistSq(loc) < thresh * thresh;
	}
	
	@Override
	public void draw(Graphics g, int[] xs, int[] ys) {
		if (setForStroke(g)) g.drawPolyline(xs, ys, xs.length);
	}
}
