/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import java.util.List;

import com.cburch.draw.canvas.CanvasObject;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Location;

public class Drawables {
	private Drawables() { }
	
	public static CanvasObject createLine(int x0, int y0, int x1, int y1,
			AttributeSet attrs) {
		return apply(attrs, new Line(x0, y0, x1, y1));
	}
	
	public static CanvasObject createRectangle(int x, int y, int w, int h,
			AttributeSet attrs) {
		return apply(attrs, new Rectangle(x, y, w, h));
	}
	
	public static CanvasObject createRoundRectangle(int x, int y, int w, int h,
			AttributeSet attrs) {
		return apply(attrs, new RoundRectangle(x, y, w, h));
	}
	
	public static CanvasObject createOval(int x, int y, int w, int h,
			AttributeSet attrs) {
		return apply(attrs, new Oval(x, y, w, h));
	}
	
	public static CanvasObject createPolygon(List<Location> locations, AttributeSet attrs) {
		return apply(attrs, new Polygon(locations));
	}
	
	public static CanvasObject createPolyline(List<Location> locations, AttributeSet attrs) {
		return apply(attrs, new Polyline(locations));
	}
	
	private static CanvasObject apply(AttributeSet attrs, CanvasObject drawable) {
		DrawingMember d = (DrawingMember) drawable;
		for (Attribute<?> attr : attrs.getAttributes()) {
			@SuppressWarnings("unchecked")
			Attribute<Object> a = (Attribute<Object>) attr;
			Object value = attrs.getValue(attr);
			d.setValue(a, value);
		}
		return drawable;
	}
}
