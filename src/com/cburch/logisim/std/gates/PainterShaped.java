/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.gates;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;

import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.util.GraphicsUtil;

public class PainterShaped {
	private PainterShaped() { }
	
	private static HashMap<Integer,int[]> INPUT_LENGTHS = new HashMap<Integer,int[]>();
	
	static void paintAnd(InstancePainter painter, int width, int height) {
		Graphics g = painter.getGraphics();
		GraphicsUtil.switchToWidth(g, 2);
		int[] xp = new int[] { -width / 2, -width + 1, -width + 1, -width / 2 }; 
		int[] yp = new int[] { -width / 2, -width / 2, width / 2, width / 2 };
		GraphicsUtil.drawCenteredArc(g, -width / 2, 0, width / 2, -90, 180);

		g.drawPolyline(xp, yp, 4);
		if (height > width) {
			g.drawLine(-width + 1, -height / 2, -width + 1, height / 2);
		}
	}
	
	static void paintOr(InstancePainter painter, int width, int height) {
		Graphics g = painter.getGraphics();
		GraphicsUtil.switchToWidth(g, 2);
		if (width == 30) {
			GraphicsUtil.drawCenteredArc(g, -30, -21, 36, -90, 53);
			GraphicsUtil.drawCenteredArc(g, -30,  21, 36, 90, -53);
		} else {
			GraphicsUtil.drawCenteredArc(g, -50, -37, 62, -90, 53);
			GraphicsUtil.drawCenteredArc(g, -50,  37, 62, 90, -53);
		}
		paintShield(g, -width, 0, width, height);
	}
	
	static void paintNot(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		GraphicsUtil.switchToWidth(g, 2);
		if (painter.getAttributeValue(NotGate.ATTR_SIZE) == NotGate.SIZE_NARROW) {
			GraphicsUtil.switchToWidth(g, 2);
			int[] xp = new int[4];
			int[] yp = new int[4];
			xp[0] =  -6; yp[0] =  0;
			xp[1] = -19; yp[1] = -6;
			xp[2] = -19; yp[2] =  6;
			xp[3] =  -6; yp[3] =  0;
			g.drawPolyline(xp, yp, 4);
			g.drawOval(-6, -3, 6, 6);
		} else {
			int[] xp = new int[4];
			int[] yp = new int[4];
			xp[0] = -10; yp[0] = 0;
			xp[1] = -29; yp[1] = -7;
			xp[2] = -29; yp[2] = 7;
			xp[3] = -10; yp[3] = 0;
			g.drawPolyline(xp, yp, 4);
			g.drawOval(-9, -4, 9, 9);
		}
	}
	
	static void paintXor(InstancePainter painter, int width, int height) {
		Graphics g = painter.getGraphics();
		paintOr(painter, width - 10, width - 10);
		paintShield(g, -width, 0, width - 10, height);
	}

	private static void paintShield(Graphics g, int x, int y,
			int width, int height) {
		GraphicsUtil.switchToWidth(g, 2);
		if (width == 30) {
			GraphicsUtil.drawCenteredArc(g, x - 26, y, 30, -30, 60);
		} else {
			GraphicsUtil.drawCenteredArc(g, x - 43, y, 50, -30, 60);
		}
		if (height > width) {
			int extra = (height - width) / 2;
			int dx = (int) Math.round(extra * (Math.sqrt(3) / 2));
			GraphicsUtil.drawCenteredArc(g,
				x - dx, y - (width + extra) / 2,
				extra, -30, 60);
			GraphicsUtil.drawCenteredArc(g,
				x - dx, y + (width + extra) / 2,
				extra, -30, 60);
		}
	}

	static void paintInputLines(InstancePainter painter, AbstractGate factory) {
		Location loc = painter.getLocation();
		boolean printView = painter.isPrintView();
		GateAttributes attrs = (GateAttributes) painter.getAttributeSet();
		Direction facing = attrs.facing;
		int inputs = attrs.inputs;
		int negated = attrs.negated;
		
		int[] lengths = getInputLineLengths(attrs, factory);
		if (painter.getInstance() == null) { // drawing ghost - negation bubbles only
			for (int i = 0; i < inputs; i++) {
				boolean iNegated = ((negated >> i) & 1) == 1;
				if (iNegated) {
					Location offs = factory.getInputOffset(attrs, i);
					Location loci = loc.translate(offs.getX(), offs.getY());
					Location cent = loci.translate(facing, lengths[i] + 5);
					painter.drawDongle(cent.getX(), cent.getY());
				}
			}
		} else {
			Graphics g = painter.getGraphics();
			Color baseColor = g.getColor();
			GraphicsUtil.switchToWidth(g, 3);
			for (int i = 0; i < inputs; i++) {
				Location offs = factory.getInputOffset(attrs, i);
				Location src = loc.translate(offs.getX(), offs.getY());
				int len = lengths[i];
				if (len != 0 && (!printView || painter.isPortConnected(i + 1))) {
					if (painter.getShowState()) {
						Value val = painter.getPort(i + 1);
						g.setColor(val.getColor());
					} else {
						g.setColor(baseColor);
					}
					Location dst = src.translate(facing, len);
					g.drawLine(src.getX(), src.getY(), dst.getX(), dst.getY());
				}
				if (((negated >> i) & 1) == 1) {
					Location cent = src.translate(facing, lengths[i] + 5);
					g.setColor(baseColor);
					painter.drawDongle(cent.getX(), cent.getY());
					GraphicsUtil.switchToWidth(g, 3);
				}
			}
		}
	}
	
	private static int[] getInputLineLengths(GateAttributes attrs, AbstractGate factory) {
		int inputs = attrs.inputs;
		int mainHeight = ((Integer) attrs.size.getValue()).intValue();
		Integer key = Integer.valueOf(inputs * 31 + mainHeight);
		Object ret = INPUT_LENGTHS.get(key);
		if (ret != null) {
			return (int[]) ret;
		}
		
		int[] lengths = new int[inputs];
		INPUT_LENGTHS.put(key, lengths);
		Location loc0 = factory.getInputOffset(attrs, 0);
		Location locn = factory.getInputOffset(attrs, inputs - 1);
		int totalHeight = 10 + loc0.manhattanDistanceTo(locn);
		int wingHeight = (totalHeight - mainHeight) / 2;
		double wingCenterX = wingHeight * Math.sqrt(3) / 2;
		double mainCenterX = mainHeight * Math.sqrt(3) / 2;
		
		for (int i = 0; i < inputs; i++) {
			Location loci = factory.getInputOffset(attrs, i);
			int disti = 5 + loc0.manhattanDistanceTo(loci);
			if (disti > totalHeight - disti) { // ensure on top half
				disti = totalHeight - disti;
			}
			double dx;
			if (disti < wingHeight) { // point is on wing
				int dy = wingHeight / 2 - disti;
				dx = Math.sqrt(wingHeight * wingHeight - dy * dy) - wingCenterX;
			} else { // point is on main shield
				int dy = totalHeight / 2 - disti;
				dx = Math.sqrt(mainHeight * mainHeight - dy * dy) - mainCenterX;
			}
			lengths[i] = (int) (dx - 0.5);
		}
		return lengths;
	}
}
