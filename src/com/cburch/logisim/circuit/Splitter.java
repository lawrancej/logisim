/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.CircuitWires;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentUserEvent;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.comp.ManagedComponent;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.ToolTipMaker;
import com.cburch.logisim.tools.WireRepair;
import com.cburch.logisim.tools.WireRepairData;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringUtil;

public class Splitter extends ManagedComponent
		implements WireRepair, ToolTipMaker {
	private static final int SPINE_WIDTH = Wire.WIDTH + 1;
	private static final int SPINE_DOT = Wire.WIDTH + 1;
	
	private class MyAttributeListener implements AttributeListener {
		public void attributeListChanged(AttributeEvent e) { }
		public void attributeValueChanged(AttributeEvent e) {
			configureComponent();
		}
	}

	// basic data
	byte[] bit_thread; // how each bit maps to thread within end

	// derived data
	private MyAttributeListener myAttributeListener = new MyAttributeListener();
	CircuitWires.SplitterData wire_data;

	public Splitter(Location loc, AttributeSet attrs) {
		super(loc, attrs, 3);
		configureComponent();
		attrs.addAttributeListener(myAttributeListener);
	}

	//
	// abstract ManagedComponent methods
	//
	@Override
	public ComponentFactory getFactory() {
		return SplitterFactory.instance;
	}

	@Override
	public void propagate(CircuitState state) {
		; // handled by CircuitWires, nothing to do
	}
	
	@Override
	public boolean contains(Location loc) {
		if (super.contains(loc)) {
			Location myLoc = getLocation();
			Direction facing = getAttributeSet().getValue(StdAttr.FACING);
			if (facing == Direction.EAST || facing == Direction.WEST) {
				return Math.abs(loc.getX() - myLoc.getX()) > 5
					|| loc.manhattanDistanceTo(myLoc) <= 5; 
			} else {                
				return Math.abs(loc.getY() - myLoc.getY()) > 5
					|| loc.manhattanDistanceTo(myLoc) <= 5; 
			}
		} else {
			return false;
		}
	}

	private synchronized void configureComponent() {
		clearManager();
		recomputeBounds();

		SplitterAttributes attrs = (SplitterAttributes) getAttributeSet();
		Direction facing = attrs.facing;
		int fanout = attrs.fanout;
		byte[] bit_end = attrs.bit_end;

		// compute width of each end
		bit_thread = new byte[bit_end.length];
		byte[] end_width = new byte[fanout + 1];
		end_width[0] = (byte) bit_end.length;
		for (int i = 0; i < bit_end.length; i++) {
			byte thr = bit_end[i];
			if (thr > 0) {
				bit_thread[i] = end_width[thr];
				end_width[thr]++;
			} else {
				bit_thread[i] = -1;
			}
		}

		// compute end positions
		int offs = -(fanout / 2) * 10;
		int wid = attrs.appear == SplitterAttributes.APPEAR_FAT_TRAPEZOID ? 40 : 20;
		int dx, ddx;
		int dy, ddy;
		if (facing == Direction.EAST) {
			dx =  wid;  ddx =   0;
			dy = offs;  ddy =  10;
		} else if (facing == Direction.WEST) {
			dx = -wid;  ddx =   0;
			dy = offs;  ddy =  10;
		} else if (facing == Direction.NORTH) {
			dx = offs + (fanout - 1) * 10;  ddx = -10;
			dy = -wid;                      ddy =   0;
		} else if (facing == Direction.SOUTH) {
			dx = offs + (fanout - 1) * 10;  ddx = -10;
			dy =  wid;                      ddy =   0;
		} else {
			throw new IllegalArgumentException("unrecognized direction");
		}

		// now we can configure the ends
		Location loc = getLocation();
		EndData[] ends = new EndData[fanout + 1];
		ends[0] = new EndData(loc, BitWidth.create(bit_end.length), EndData.INPUT_OUTPUT);
		for (int i = 1; i <= fanout; i++) {
			Location p = loc.translate(dx, dy);
			ends[i] = new EndData(p, BitWidth.create(end_width[i]), EndData.INPUT_OUTPUT);
			dx += ddx;
			dy += ddy;
		}
		setEnds(ends);
		wire_data = new CircuitWires.SplitterData(fanout);
	}
	
	//
	// user interface methods
	//
	public void draw(ComponentDrawContext context) {
		SplitterAttributes attrs = (SplitterAttributes) getAttributeSet();
		if (attrs.appear == SplitterAttributes.APPEAR_LINES) {
			drawLines(context, attrs);
		} else if (attrs.appear == SplitterAttributes.APPEAR_FAT_TRAPEZOID) {
			drawTrapezoid(context, attrs, 40);
		} else {
			drawTrapezoid(context, attrs, 20);
		}
	}
	
	static void drawTrapezoidShape(ComponentDrawContext context,
			SplitterAttributes attrs, Location origin,
			Bounds bds) {
		Direction facing = attrs.facing;
		Location ein = origin;
		int[] trapx;
		int[] trapy;
		int[] trix;
		int[] triy;
		int x0 = bds.getX();
		int x1 = x0 + bds.getWidth();
		int y0 = bds.getY();
		int y1 = y0 + bds.getHeight();
		if (facing == Direction.NORTH || facing == Direction.SOUTH) {
			int ylong = facing == Direction.NORTH ? y0 : y1;
			int yshort = facing == Direction.NORTH ? y1 - 4 : y0 + 4;
			trapx = new int[] { x0 + 4, x0, x1, x1 - 4 };
			trapy = new int[] { yshort, ylong, ylong, yshort };

			int xin = ein.getX();
			int yin = ein.getY();
			trix = new int[] { xin, xin - 4, xin + 4 };
			triy = new int[] { yin, yshort, yshort };
		} else {
			int xlong = facing == Direction.WEST ? x0 : x1;
			int xshort = facing == Direction.WEST ? x1 - 4 : x0 + 4;
			trapy = new int[] { y0 + 4, y0, y1, y1 - 4 };
			trapx = new int[] { xshort, xlong, xlong, xshort };

			int xin = ein.getX();
			int yin = ein.getY();
			trix = new int[] { xin, xshort, xshort };
			triy = new int[] { yin, yin - 4, yin + 4 };
		}
		
		Graphics g = context.getGraphics();
		g.fillPolygon(trix, triy, 3);
		GraphicsUtil.switchToWidth(g, 2);
		g.drawPolygon(trapx, trapy, 4);
	}
	
	private void drawTrapezoid(ComponentDrawContext context, SplitterAttributes attrs,
			int width) {
		Graphics g = context.getGraphics();
		Direction facing = attrs.facing;
		drawTrapezoidShape(context, attrs, getLocation(), getBounds());
		
		Font font = g.getFont();
		g.setFont(font.deriveFont(7.0f));
		String[] ends = new String[attrs.fanout + 1];
		int curEnd = -1;
		int cur0 = 0;
		for (int i = 0, n = attrs.bit_end.length; i <= n; i++) {
			int bit = i == n ? -1 : attrs.bit_end[i];
			if (bit != curEnd) {
				int cur1 = i - 1;
				String toAdd;
				if (curEnd <= 0) {
					toAdd = null;
				} else if (cur0 == cur1) {
					toAdd = "" + cur0;
				} else {
					toAdd = cur0 + "-" + cur1;
				}
				if (toAdd != null) {
					String old = ends[curEnd];
					if (old == null) {
						ends[curEnd] = toAdd;
					} else {
						ends[curEnd] = old + "," + toAdd;
					}
				}
				curEnd = bit;
				cur0 = i;
			}
		}
		int dx = 0;
		int dy = 0;
		if (facing == Direction.NORTH) {
			dy = 2;
		} else if (facing == Direction.SOUTH) {
			dy = -2;
		} else if (facing == Direction.WEST) {
			dx = 2;
		} else {
			dx = -2;
		}
		int halign = dx + dy < 0 ? GraphicsUtil.H_RIGHT : GraphicsUtil.H_LEFT;
		double radians = dx == 0 ? Math.PI / 2.0 : 0;
		for (int i = 1, n = ends.length; i < n; i++) {
			String text = ends[i];
			if (text != null) {
				Location loc = getEndLocation(i);
				Graphics g2 = g.create();
				g2.translate(loc.getX() + dx, loc.getY() + dy);
				((Graphics2D) g2).rotate(radians);
				GraphicsUtil.drawText(g2, ends[i],
						0, 0, halign, GraphicsUtil.V_CENTER);
				g2.dispose();
			}
		}
		context.drawPins(this);
	}
	
	private void drawLines(ComponentDrawContext context, SplitterAttributes attrs) {
		Graphics g = context.getGraphics();
		CircuitState state = context.getCircuitState();
		Direction facing = attrs.facing;
		int fanout = attrs.fanout;
		
		g.setColor(Color.BLACK);
		Location s = getEndLocation(0);
		if (facing == Direction.NORTH
				|| facing == Direction.SOUTH) {
			Location t = getEndLocation(1);
			int mx = s.getX();
			int my = (s.getY() + t.getY()) / 2;
			GraphicsUtil.switchToWidth(g, Wire.WIDTH);
			g.drawLine(mx, s.getY(), mx, my);
			for (int i = 1; i <= fanout; i++) {
				t = getEndLocation(i);
				if (context.getShowState()) {
					g.setColor(state.getValue(t).getColor());
				}
				int tx = t.getX();
				g.drawLine(tx, t.getY(),
						tx < mx ? tx + 10 : (tx > mx ? tx - 10 : tx), my);
			}
			if (fanout > 3) {
				GraphicsUtil.switchToWidth(g, SPINE_WIDTH);
				g.setColor(Color.BLACK);
				t = getEndLocation(1);
				Location last = getEndLocation(fanout);
				g.drawLine(t.getX() - 10, my, last.getX() + 10, my);
			} else {
				g.setColor(Color.BLACK);
				g.fillOval(mx - SPINE_DOT / 2, my - SPINE_DOT / 2,
						SPINE_DOT, SPINE_DOT);
			}
		} else {
			Location t = getEndLocation(1);
			int mx = (s.getX() + t.getX()) / 2;
			int my = s.getY();
			GraphicsUtil.switchToWidth(g, Wire.WIDTH);
			g.drawLine(s.getX(), my, mx, my);
			for (int i = 1; i <= fanout; i++) {
				t = getEndLocation(i);
				if (context.getShowState()) {
					g.setColor(state.getValue(t).getColor());
				}
				int ty = t.getY();
				g.drawLine(t.getX(), ty,
						mx, ty < my ? ty + 10 : (ty > my ? ty - 10 : ty));
			}
			if (fanout > 3) {
				GraphicsUtil.switchToWidth(g, SPINE_WIDTH);
				g.setColor(Color.BLACK);
				t = getEndLocation(1);
				Location last = getEndLocation(fanout);
				g.drawLine(mx, t.getY() + 10, mx, last.getY() - 10);
			} else {
				g.setColor(Color.BLACK);
				g.fillOval(mx - SPINE_DOT / 2, my - SPINE_DOT / 2,
						SPINE_DOT, SPINE_DOT);
			}
		}
		GraphicsUtil.switchToWidth(g, 1);
	}
	
	@Override
	public Object getFeature(Object key) {
		if (key == WireRepair.class) return this;
		if (key == ToolTipMaker.class) return this;
		else return super.getFeature(key);
	}

	public boolean shouldRepairWire(WireRepairData data) {
		return true;
	}
	
	public String getToolTip(ComponentUserEvent e) {
		int end = -1;
		for (int i = getEnds().size() - 1; i >= 0; i--) {
			if (getEndLocation(i).manhattanDistanceTo(e.getX(), e.getY()) < 10) {
				end = i;
				break;
			}
		}
		
		if (end == 0) {
			return Strings.get("splitterCombinedTip");
		} else if (end > 0){
			int bits = 0;
			StringBuilder buf = new StringBuilder();
			SplitterAttributes attrs = (SplitterAttributes) getAttributeSet();
			byte[] bit_end = attrs.bit_end;
			boolean inString = false;
			int beginString = 0;
			for (int i = 0; i < bit_end.length; i++) {
				if (bit_end[i] == end) {
					bits++;
					if (!inString) {
						inString = true;
						beginString = i;
					}
				} else {
					if (inString) {
						appendBuf(buf, beginString, i - 1);
						inString = false;
					}
				}
			}
			if (inString) appendBuf(buf, beginString, bit_end.length - 1);
			String base;
			switch (bits) {
			case 0:  base = Strings.get("splitterSplit0Tip"); break;
			case 1:  base = Strings.get("splitterSplit1Tip"); break;
			default: base = Strings.get("splitterSplitManyTip"); break;
			}
			return StringUtil.format(base, buf.toString());
		} else {
			return null;
		}
	}
	private static void appendBuf(StringBuilder buf, int start, int end) {
		if (buf.length() > 0) buf.append(",");
		if (start == end) {
			buf.append(start);
		} else {
			buf.append(start + "-" + end);
		}
	}

}
