/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import java.awt.Color;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.cburch.logisim.data.Location;

class SvgCreator {
	private SvgCreator() { }
	
	public static Element createRectangle(Document doc, Rectangle rect) {
		return createRectangular(doc, rect);
	}

	public static Element createRoundRectangle(Document doc,
			RoundRectangle rrect) {
		Element elt = createRectangular(doc, rrect);
		int r = rrect.getValue(DrawAttr.CORNER_RADIUS).intValue();
		elt.setAttribute("rx", "" + r);
		elt.setAttribute("ry", "" + r);
		return elt;
	}
	
	private static Element createRectangular(Document doc, Rectangular rect) {
		Element elt = doc.createElement("rect");
		elt.setAttribute("x", "" + rect.getX());
		elt.setAttribute("y", "" + rect.getY());
		elt.setAttribute("width", "" + rect.getWidth());
		elt.setAttribute("height", "" + rect.getHeight());
		populateFill(elt, rect);
		return elt;
	}

	public static Element createOval(Document doc, Oval oval) {
		double x = oval.getX();
		double y = oval.getY();
		double width = oval.getWidth();
		double height = oval.getHeight();
		Element elt = doc.createElement("ellipse");
		elt.setAttribute("cx", "" + (x + width / 2));
		elt.setAttribute("cy", "" + (y + height / 2));
		elt.setAttribute("rx", "" + (width / 2));
		elt.setAttribute("ry", "" + (height / 2));
		populateFill(elt, oval);
		return elt;
	}
	
	public static Element createLine(Document doc, Line line) {
		Element elt = doc.createElement("line");
		Location v1 = line.getEnd0();
		Location v2 = line.getEnd1();
		elt.setAttribute("x1", "" + v1.getX());
		elt.setAttribute("y1", "" + v1.getY());
		elt.setAttribute("x2", "" + v2.getX());
		elt.setAttribute("y2", "" + v2.getY());
		populateStroke(elt, line);
		return elt;
	}
	
	public static Element createPolygon(Document doc, Polygon poly) {
		Element elt = doc.createElement("polygon");
		populatePoly(elt, poly);
		populateFill(elt, poly);
		return elt;
	}
	
	public static Element createPolyline(Document doc, Polyline poly) {
		Element elt = doc.createElement("polyline");
		populatePoly(elt, poly);
		populateStroke(elt, poly);
		return elt;
	}
	
	private static void populatePoly(Element elt, Poly poly) {
		StringBuilder points = new StringBuilder();
		boolean first = true;
		for (Location v : poly.getVertices()) {
			if (!first) points.append(" ");
			points.append(v.getX() + "," + v.getY());
			first = false;
		}
		elt.setAttribute("points", points.toString());
	}
	
	private static void populateFill(Element elt, DrawingMember shape) {
		Object type = shape.getValue(DrawAttr.PAINT_TYPE);
		if (type == DrawAttr.PAINT_FILL) {
			elt.setAttribute("stroke", "none");
		} else {
			populateStroke(elt, shape);
		}
		if (type == DrawAttr.PAINT_STROKE) {
			elt.setAttribute("fill", "none");
		} else {
			Color fill = shape.getValue(DrawAttr.FILL_COLOR);
			elt.setAttribute("fill", getColorString(fill));
			elt.setAttribute("fill-opacity", getOpacityString(fill));
		}
	}
	
	private static void populateStroke(Element elt, DrawingMember shape) {
		Integer width = shape.getValue(DrawAttr.STROKE_WIDTH);
		if (width != null && width.intValue() != 1) {
			elt.setAttribute("stroke-width", width.toString());
		}
		Color stroke = shape.getValue(DrawAttr.STROKE_COLOR);
		elt.setAttribute("stroke", getColorString(stroke));
		elt.setAttribute("stroke-opacity", getOpacityString(stroke));
		elt.setAttribute("fill", "none");
	}
	
	private static String getColorString(Color color) {
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(),
				color.getBlue());
	}
	
	private static String getOpacityString(Color color) {
		return String.format("%5.3f", color.getAlpha() / 255.0);
	}
}
