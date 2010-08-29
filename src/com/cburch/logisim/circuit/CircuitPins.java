/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentEvent;
import com.cburch.logisim.comp.ComponentListener;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.EventSourceWeakSupport;

class CircuitPins {
	private static class PinData implements Comparable<PinData> {
		Component pin;
		Location offset = null; // relative to top left corner of box

		PinData(Component p) { this.pin = p; }

		public int compareTo(PinData other) {
			Direction d0 = pin.getAttributeSet().getValue(StdAttr.FACING);
			Location p0 = this.pin.getLocation();
			int px0, py0;
			if (d0 == Direction.EAST || d0 == Direction.WEST) {
				px0 = p0.getX(); py0 = p0.getY();
			} else {
				py0 = p0.getX(); px0 = p0.getY();
			}

			Location p1 = other.pin.getLocation();
			Direction d1 = other.pin.getAttributeSet().getValue(StdAttr.FACING);
			int px1, py1;
			if (d1 == Direction.EAST || d1 == Direction.WEST) {
				px1 = p1.getX(); py1 = p1.getY();
			} else {
				py1 = p1.getX(); px1 = p1.getY();
			}

			if (py0 != py1) return py0 - py1;
			else           return px0 - px1;
		}

	}

	private class MyComponentListener
			implements ComponentListener, AttributeListener {
		public void endChanged(ComponentEvent e) {
			for (CircuitPinListener l : listeners) {
				l.pinChanged();
			}
		}
		public void componentInvalidated(ComponentEvent e) { }

		public void attributeListChanged(AttributeEvent e) { }
		public void attributeValueChanged(AttributeEvent e) {
			Attribute<?> attr = e.getAttribute();
			if (attr == StdAttr.FACING) {
				bounds = null;
				for (CircuitPinListener l : listeners) {
					l.pinChanged();
				}
			}
		}
	}

	private EventSourceWeakSupport<CircuitPinListener> listeners
		= new EventSourceWeakSupport<CircuitPinListener>();
	private MyComponentListener myComponentListener = new MyComponentListener();
	private ArrayList<PinData> pins = new ArrayList<PinData>();
	private Bounds bounds = null;

	CircuitPins() { }

	void addPinListener(CircuitPinListener l) { listeners.add(l); }
	void removePinListener(CircuitPinListener l) { listeners.remove(l); }

	void addPin(Component added) {
		// if it's already in there, don't add it again
		for (PinData pd : pins) {
			if (pd.pin == added) return;
		}

		// add it
		pins.add(new PinData(added));
		added.getAttributeSet().addAttributeListener(myComponentListener);
		added.addComponentListener(myComponentListener);
		Collections.sort(pins);
		bounds = null;

		for (CircuitPinListener l : listeners) {
			l.pinAdded();
		}
	}

	void removePin(Component removed) {
		boolean success = false;
		for (Iterator<PinData> it = pins.iterator(); it.hasNext(); ) {
			PinData pd = it.next();
			if (pd.pin == removed) {
				// found it to remove
				it.remove();
				success = true;
				break;
			}
		}
		if (success) {
			removed.removeComponentListener(myComponentListener);
			bounds = null;
			for (CircuitPinListener l : listeners) {
				l.pinRemoved();
			}
			removed.getAttributeSet().addAttributeListener(myComponentListener);
		} else {
			throw new NoSuchElementException();
		}
	}

	Component getSubcircuitPin(int which) {
		if (bounds == null) recomputeBounds();
		PinData pd = pins.get(which);
		if (pd == null) throw new NoSuchElementException("null element");
		return pd.pin;
	}

	Bounds getOffsetBounds(AttributeSet attrs) {
		if (bounds == null) recomputeBounds();
		Direction facing = attrs.getValue(StdAttr.FACING);
		if (facing == Direction.EAST) return bounds;
		if (facing == Direction.WEST) {
			int dx = bounds.getX() == 0 ? -bounds.getWidth() : 0;
			if (bounds.getY() == 0 || bounds.getY() == -bounds.getHeight()) dx = -(bounds.getX() + bounds.getWidth());
			return Bounds.create(dx, -(bounds.getY() + bounds.getHeight()),
					bounds.getWidth(), bounds.getHeight());
		} else if (facing == Direction.SOUTH) {
			return Bounds.create(-(bounds.getY() + bounds.getHeight()), bounds.getX(),
					bounds.getHeight(), bounds.getWidth());
		} else if (facing == Direction.NORTH) {
			int dx = bounds.getX() == 0 ? -bounds.getWidth() : 0;
			if (bounds.getY() == 0 || bounds.getY() == -bounds.getHeight()) dx = -(bounds.getX() + bounds.getWidth());
			return Bounds.create(bounds.getY(), dx,
					bounds.getHeight(), bounds.getWidth());
		}
		return bounds;
	}
	
	List<Component> getPins() {
		List<Component> ret = new ArrayList<Component>();
		for (PinData pd : pins) {
			ret.add(pd.pin);
		}
		return ret;
	}

	void configureComponent(Subcircuit comp) {
		if (bounds == null) recomputeBounds();
		CircuitAttributes attrs = (CircuitAttributes) comp.getAttributeSet();
		Direction facing = attrs.getFacing();
		Location loc = comp.getLocation();
		comp.clearManager();
		comp.setBounds(getOffsetBounds(attrs).translate(loc.getX(), loc.getY()));
		Location base = loc.translate(facing, bounds.getX(), bounds.getY());

		int i = 0;
		EndData[] ends = new EndData[pins.size()];
		for (PinData pd : pins) {
			int type = Circuit.isInput(pd.pin) ? EndData.INPUT_ONLY : EndData.OUTPUT_ONLY;
			BitWidth width = pd.pin.getAttributeSet().getValue(StdAttr.WIDTH);

			Location ploc = base.translate(facing, pd.offset.getX(), pd.offset.getY());
			ends[i] = new EndData(ploc, width, type);
			i++;
		}
		comp.setEnds(ends);
	}

	private void recomputeBounds() {
		int[] n = { 0, 0, 0, 0 };
		int east = Direction.EAST.hashCode();
		int west = Direction.WEST.hashCode();
		int north = Direction.NORTH.hashCode();
		int south = Direction.SOUTH.hashCode();

		// count pins in each direction
		for (PinData pd : pins) {
			Direction facing = pd.pin.getAttributeSet().getValue(StdAttr.FACING);
			int di = facing.hashCode();
			n[di]++;
		}

		// compute dimension, offset from beginning of each side
		int[] start = { 0, 0, 0, 0 }; // offset for each side
		int ht  = computeAxis(start, n, east,  west);
		int wid = computeAxis(start, n, north, south);

		// compute box offset relative to top left corner
		int x;
		int y;
		if (n[west] > 0) { // anchor is on east side
			x = wid;
			y = start[west];
		} else if (n[south] > 0) { // anchor is on top side
			x = start[south];
			y = 0;
		} else if (n[east] > 0) { // anchor is on west side
			x = 0;
			y = start[east];
		} else if (n[north] > 0) { // anchor is on bottom side
			x = start[north];
			y = ht;
		} else { // anchor is top left corner
			x = 0;
			y = 0;
		}
		bounds = Bounds.create(-x, -y, wid, ht);

		// set offset for each pin relative to top left corner
		Arrays.fill(n, 0);
		for (PinData pd : pins) {
			Direction facing = pd.pin.getAttributeSet().getValue(StdAttr.FACING);
			int di = facing.hashCode();
			if (di == east) { // on west side
				pd.offset = Location.create(0, start[east] + n[di]);
			} else if (di == west) { // on east side
				pd.offset = Location.create(wid, start[west] + n[di]);
			} else if (di == north) { // on south side
				pd.offset = Location.create(start[north] + n[di], ht);
			} else if (di == south) { // on north side
				pd.offset = Location.create(start[south] + n[di], 0);
			}
			n[di] += 10;
		}
	}

	private int computeAxis(int[] start, int[] n, int i, int j) {
		int others = n[0]+n[1]+n[2]+n[3]-n[i]-n[j]; // # pins on other axis
		int max = Math.max(n[i], n[j]); // maximum pins on this axis

		// compute length of axis, offset for maximum
		int dim;
		int maxOffs;
		switch (max) {
		case 0:
			dim = 30;
			maxOffs = (others == 0 ? 15 : 10);
			break;
		case 1:
			dim = 30;
			maxOffs = (others == 0 ? 15 : 10);
			break;
		case 2:
			dim = 30;
			maxOffs = 10;
			break;
		default:
			if (others == 0) {
				dim = 10 * max;
				maxOffs =  5;
			} else {
				dim = 10 * max + 10;
				maxOffs = 10;
			}
		}

		// compute offset for each side
		start[i] = maxOffs + 10 * ((max - n[i]) / 2);
		start[j] = maxOffs + 10 * ((max - n[j]) / 2);

		return dim;
	}
}
