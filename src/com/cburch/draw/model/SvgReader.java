/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import java.awt.Color;
import java.util.List;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.UnmodifiableList;

public class SvgReader {
	private SvgReader() { }
	
	public static DrawingMember createShape(Element elt) {
		String name = elt.getTagName();
		DrawingMember ret;
		if (name.equals("rect")) {
			ret = createRectangle(elt);
		} else if (name.equals("ellipse")) {
			ret = createOval(elt);
		} else if (name.equals("line")) {
			ret = createLine(elt);
		} else if (name.equals("polyline")) {
			ret = createPolyline(elt);
		} else if (name.equals("polygon")) {
			ret = createPolygon(elt);
		} else {
			return null;
		}
		List<Attribute<?>> attrs = ret.getAttributes();
		if (attrs.contains(DrawAttr.PAINT_TYPE)) {
			String stroke = elt.getAttribute("stroke");
			String fill = elt.getAttribute("fill");
			if (stroke.equals("") || stroke.equals("none")) {
				ret.setValue(DrawAttr.PAINT_TYPE, DrawAttr.PAINT_FILL);
			} else if (fill.equals("") || fill.equals("none")) {
				ret.setValue(DrawAttr.PAINT_TYPE, DrawAttr.PAINT_STROKE);
			} else {
				ret.setValue(DrawAttr.PAINT_TYPE, DrawAttr.PAINT_STROKE_FILL);
			}
		}
		attrs = ret.getAttributes(); // since changing paintType could change it
		if (attrs.contains(DrawAttr.STROKE_WIDTH) && elt.hasAttribute("stroke-width")) {
			Integer width = Integer.valueOf(elt.getAttribute("stroke-width"));
			ret.setValue(DrawAttr.STROKE_WIDTH, width);
		}
		if (attrs.contains(DrawAttr.STROKE_COLOR)) {
			String color = elt.getAttribute("stroke");
			String opacity = elt.getAttribute("stroke-opacity");
			if (!color.equals("none")) {
				ret.setValue(DrawAttr.STROKE_COLOR, getColor(color, opacity));
			}
		}
		if (attrs.contains(DrawAttr.FILL_COLOR)) {
			String color = elt.getAttribute("fill");
			String opacity = elt.getAttribute("fill-opacity");
			if (!color.equals("none")) {
				ret.setValue(DrawAttr.FILL_COLOR, getColor(color, opacity));
			}
		}
		return ret;
	}
	
	private static DrawingMember createRectangle(Element elt) {
		int x = Integer.parseInt(elt.getAttribute("x"));
		int y = Integer.parseInt(elt.getAttribute("y"));
		int w = Integer.parseInt(elt.getAttribute("width"));
		int h = Integer.parseInt(elt.getAttribute("height"));
		if (elt.hasAttribute("rx")) {
			DrawingMember ret = new RoundRectangle(x, y, w, h);
			int rx = Integer.parseInt(elt.getAttribute("rx"));
			ret.setValue(DrawAttr.CORNER_RADIUS, Integer.valueOf(rx));
			return ret;
		} else {
			return new Rectangle(x, y, w, h);
		}
	}

	private static DrawingMember createOval(Element elt) {
		double cx = Double.parseDouble(elt.getAttribute("cx"));
		double cy = Double.parseDouble(elt.getAttribute("cy"));
		double rx = Double.parseDouble(elt.getAttribute("rx"));
		double ry = Double.parseDouble(elt.getAttribute("ry"));
		int x = (int) Math.round(cx - rx);
		int y = (int) Math.round(cy - ry);
		int w = (int) Math.round(rx * 2);
		int h = (int) Math.round(ry * 2);
		return new Oval(x, y, w, h);
	}
	
	private static DrawingMember createLine(Element elt) {
		int x0 = Integer.parseInt(elt.getAttribute("x1"));
		int y0 = Integer.parseInt(elt.getAttribute("y1"));
		int x1 = Integer.parseInt(elt.getAttribute("x2"));
		int y1 = Integer.parseInt(elt.getAttribute("y2"));
		return new Line(x0, y0, x1, y1);
	}
	
	private static DrawingMember createPolygon(Element elt) {
		return new Polygon(parsePoints(elt.getAttribute("points")));
	}
	
	private static DrawingMember createPolyline(Element elt) {
		return new Polyline(parsePoints(elt.getAttribute("points")));
	}
	
	private static List<Location> parsePoints(String points) {
		Pattern patt = Pattern.compile("[ ,\n\r\t]+");
		String[] toks = patt.split(points);
		Location[] ret = new Location[toks.length / 2];
		for (int i = 0; i < ret.length; i++) {
			int x = Integer.parseInt(toks[2 * i]);
			int y = Integer.parseInt(toks[2 * i + 1]);
			ret[i] = Location.create(x, y);
		}
		return UnmodifiableList.create(ret);
	}
	
	private static Color getColor(String hue, String opacity) {
		int r;
		int g;
		int b;
		if (hue == null || hue.equals("")) {
			r = 0;
			g = 0;
			b = 0;
		} else {
			r = Integer.parseInt(hue.substring(1, 3), 16);
			g = Integer.parseInt(hue.substring(3, 5), 16);
			b = Integer.parseInt(hue.substring(5, 7), 16);
		}
		int a;
		if (opacity == null || opacity.equals("")) {
			a = 255;
		} else {
			a = (int) Math.round(Double.parseDouble(opacity) * 255);
		}
		return new Color(r, g, b, a);
	}
}
