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
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.ToolTipMaker;
import com.cburch.logisim.tools.WireRepair;
import com.cburch.logisim.tools.WireRepairData;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringUtil;

public class Splitter extends ManagedComponent
		implements WireRepair, ToolTipMaker {
	private static final int SPINE_WIDTH = Wire.WIDTH + 2;
	private static final int SPINE_DOT = Wire.WIDTH + 4;
	
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
		SplitterParameters parms = attrs.getParameters();
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
		Location origin = getLocation();
		int x = origin.getX() + parms.getEnd0X();
		int y = origin.getY() + parms.getEnd0Y();
		int dx = parms.getEndToEndDeltaX();
		int dy = parms.getEndToEndDeltaY();
		
		EndData[] ends = new EndData[fanout + 1];
		ends[0] = new EndData(origin, BitWidth.create(bit_end.length), EndData.INPUT_OUTPUT);
		for (int i = 0; i < fanout; i++) {
			ends[i + 1] = new EndData(Location.create(x, y),
					BitWidth.create(end_width[i + 1]), EndData.INPUT_OUTPUT);
			x += dx;
			y += dy;
		}
		setEnds(ends);
		wire_data = new CircuitWires.SplitterData(fanout);
	}
	
	//
	// user interface methods
	//
	public void draw(ComponentDrawContext context) {
		SplitterAttributes attrs = (SplitterAttributes) getAttributeSet();
		if (attrs.appear == SplitterAttributes.APPEAR_LEGACY) {
			drawLegacy(context, attrs);
		} else {
			Location loc = getLocation();
			drawLines(context, attrs, loc, this);
			drawLabels(context, attrs, loc, this);
			context.drawPins(this);
		}
	}
	
	static void drawLines(ComponentDrawContext context,
			SplitterAttributes attrs, Location origin, Splitter comp) {
		boolean showState = comp != null && context.getShowState();
		CircuitState state = showState ? context.getCircuitState() : null;
		if (state == null) showState = false;

		SplitterParameters parms = attrs.getParameters();
		int x0 = origin.getX();
		int y0 = origin.getY();
		int x = x0 + parms.getEnd0X();
		int y = y0 + parms.getEnd0Y();
		int dx = parms.getEndToEndDeltaX();
		int dy = parms.getEndToEndDeltaY();
		int dxEndSpine = parms.getEndToSpineDeltaX();
		int dyEndSpine = parms.getEndToSpineDeltaY();
		
		Graphics g = context.getGraphics();
		Color oldColor = g.getColor();
		GraphicsUtil.switchToWidth(g, Wire.WIDTH);
		for (int i = 0, n = attrs.fanout; i < n; i++) {
			if (showState) {
				Value val = state.getValue(comp.getEndLocation(i + 1));
				g.setColor(val.getColor());
			}
			g.drawLine(x, y, x + dxEndSpine, y + dyEndSpine);
			x += dx;
			y += dy;
		}
		GraphicsUtil.switchToWidth(g, SPINE_WIDTH);
		g.setColor(oldColor);
		int spine0x = x0 + parms.getSpine0X();
		int spine0y = y0 + parms.getSpine0Y();
		int spine1x = x0 + parms.getSpine1X();
		int spine1y = y0 + parms.getSpine1Y();
		if (spine0x == spine1x && spine0y == spine1y) { // centered
			int fanout = attrs.fanout;
			spine0x = x0 + parms.getEnd0X() + parms.getEndToSpineDeltaX();
			spine0y = y0 + parms.getEnd0Y() + parms.getEndToSpineDeltaY();
			spine1x = spine0x + (fanout - 1) * parms.getEndToEndDeltaX();
			spine1y = spine0y + (fanout - 1) * parms.getEndToEndDeltaY();
			if (parms.getEndToEndDeltaX() == 0) { // vertical spine
				if (spine0y < spine1y) {
					spine0y++;
					spine1y--;
				} else {
					spine0y--;
					spine1y++;
				}
				g.drawLine(x0 + parms.getSpine1X() / 4, y0, spine0x, y0);
			} else {
				if (spine0x < spine1x) {
					spine0x++;
					spine1x--;
				} else {
					spine0x--;
					spine1x++;
				}
				g.drawLine(x0, y0 + parms.getSpine1Y() / 4, x0, spine0y);
			}
			if (fanout <= 1) { // spine is empty
				int diam = SPINE_DOT;
				g.fillOval(spine0x - diam / 2, spine0y - diam / 2, diam, diam);
			} else {
				g.drawLine(spine0x, spine0y, spine1x, spine1y);
			}
		} else {
			int[] xSpine = { spine0x, spine1x, x0 + parms.getSpine1X() / 4 };
			int[] ySpine = { spine0y, spine1y, y0 + parms.getSpine1Y() / 4 };
			g.drawPolyline(xSpine, ySpine, 3);
		}
	}

	static void drawLabels(ComponentDrawContext context,
			SplitterAttributes attrs, Location origin, Splitter comp) {
		// compute labels
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

		Graphics g = context.getGraphics().create();
		Font font = g.getFont();
		g.setFont(font.deriveFont(7.0f));
		
		SplitterParameters parms = attrs.getParameters();
		int x = origin.getX() + parms.getEnd0X() + parms.getEndToSpineDeltaX();
		int y = origin.getY() + parms.getEnd0Y() + parms.getEndToSpineDeltaY();
		int dx = parms.getEndToEndDeltaX();
		int dy = parms.getEndToEndDeltaY();
		if (parms.getTextAngle() != 0) {
			((Graphics2D) g).rotate(Math.PI / 2.0);
			int t;
			t = -x; x = y; y = t;
			t = -dx; dx = dy; dy = t;
		}
		int halign = parms.getTextHorzAlign();
		int valign = parms.getTextVertAlign();
		x += (halign == GraphicsUtil.H_RIGHT ? -1 : 1) * (SPINE_WIDTH / 2 + 1);
		y += valign == GraphicsUtil.V_TOP ? 0 : -3;
		for (int i = 0, n = attrs.fanout; i < n; i++) {
			String text = ends[i + 1];
			if (text != null) {
				GraphicsUtil.drawText(g, text, x, y, halign, valign);
			}
			x += dx;
			y += dy;
		}

		g.dispose();
	}
	
	private void drawLegacy(ComponentDrawContext context, SplitterAttributes attrs) {
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
			if (fanout >= 3) {
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
			if (fanout >= 3) {
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
