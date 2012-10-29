/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.awt.Graphics;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentListener;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.tools.CustomHandles;
import com.cburch.logisim.util.Cache;
import com.cburch.logisim.util.GraphicsUtil;
import static com.cburch.logisim.util.LocaleString.*;

public final class Wire implements Component, AttributeSet, CustomHandles,
		Iterable<Location> {
	/** Stroke width when drawing wires. */
	public static final int WIDTH = 3;

	public static final AttributeOption VALUE_HORZ
		= new AttributeOption("horz", __("wireDirectionHorzOption"));
	public static final AttributeOption VALUE_VERT
		= new AttributeOption("vert", __("wireDirectionVertOption"));
	public static final Attribute<AttributeOption> dir_attr
		= Attributes.forOption("direction", __("wireDirectionAttr"),
			new AttributeOption[] { VALUE_HORZ, VALUE_VERT });
	public static final Attribute<Integer> len_attr
		= Attributes.forInteger("length", __("wireLengthAttr"));

	private static final List<Attribute<?>> ATTRIBUTES
		= Arrays.asList(new Attribute<?>[] { dir_attr, len_attr });
	private static final Cache cache = new Cache();

	public static Wire create(Location e0, Location e1) {
		return (Wire) cache.get(new Wire(e0, e1));
	}
	
	private class EndList extends AbstractList<EndData> {
		@Override
		public EndData get(int i) {
			return getEnd(i);
		}
		@Override
		public int size() { return 2; }
	}

	final Location e0;
	final Location e1;
	final boolean is_x_equal;

	private Wire(Location e0, Location e1) {
		this.is_x_equal = e0.getX() == e1.getX();
		if (is_x_equal) {
			if (e0.getY() > e1.getY()) {
				this.e0 = e1;
				this.e1 = e0;
			} else {
				this.e0 = e0;
				this.e1 = e1;
			}
		} else {
			if (e0.getX() > e1.getX()) {
				this.e0 = e1;
				this.e1 = e0;
			} else {
				this.e0 = e0;
				this.e1 = e1;
			}
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Wire)) return false;
		Wire w = (Wire) other;
		return w.e0.equals(this.e0) && w.e1.equals(this.e1);
	}
	
	@Override
	public int hashCode() {
		return e0.hashCode() * 31 + e1.hashCode();
	}

	public int getLength() {
		return (e1.getY() - e0.getY()) + (e1.getX() - e0.getX());
	}

	@Override
	public String toString() {
		return "Wire[" + e0 + "-" + e1 + "]";
	}

	//
	// Component methods
	//
	// (Wire never issues ComponentEvents, so we don't need to track listeners)
	public void addComponentListener(ComponentListener e) { }
	public void removeComponentListener(ComponentListener e) { }

	public ComponentFactory getFactory() {
		return WireFactory.instance;
	}

	public AttributeSet getAttributeSet() {
		return this;
	}

	// location/extent methods
	public Location getLocation() {
		return e0;
	}

	public Bounds getBounds() {
		int x0 = e0.getX();
		int y0 = e0.getY();
		return Bounds.create(x0 - 2, y0 - 2,
			e1.getX() - x0 + 5, e1.getY() - y0 + 5);
	}

	public Bounds getBounds(Graphics g) {
		return getBounds();
	}

	public boolean contains(Location q) {
		int qx = q.getX();
		int qy = q.getY();
		if (is_x_equal) {
			int wx = e0.getX();
			return qx >= wx - 2 && qx <= wx + 2
				&& e0.getY() <= qy && qy <= e1.getY();
		} else {
			int wy = e0.getY();
			return qy >= wy - 2 && qy <= wy + 2
				&& e0.getX() <= qx && qx <= e1.getX();
		}
	}

	public boolean contains(Location pt, Graphics g) {
		return contains(pt);
	}

	//
	// propagation methods
	//
	public List<EndData> getEnds() {
		return new EndList();
	}

	public EndData getEnd(int index) {
		Location loc = getEndLocation(index);
		return new EndData(loc, BitWidth.UNKNOWN,
				EndData.INPUT_OUTPUT);
	}

	public boolean endsAt(Location pt) {
		return e0.equals(pt) || e1.equals(pt);
	}

	public void propagate(CircuitState state) {
		// Normally this is handled by CircuitWires, and so it won't get
		// called. The exception is when a wire is added or removed
		state.markPointAsDirty(e0);
		state.markPointAsDirty(e1);
	}

	//
	// user interface methods
	//
	public void expose(ComponentDrawContext context) {
		java.awt.Component dest = context.getDestination();
		int x0 = e0.getX();
		int y0 = e0.getY();
		dest.repaint(x0 - 5, y0 - 5,
				e1.getX() - x0 + 10, e1.getY() - y0 + 10);
	}

	public void draw(ComponentDrawContext context) {
		CircuitState state = context.getCircuitState();
		Graphics g = context.getGraphics();

		GraphicsUtil.switchToWidth(g, WIDTH);
		g.setColor(state.getValue(e0).getColor());
		g.drawLine(e0.getX(), e0.getY(),
			e1.getX(), e1.getY());
	}
	
	public Object getFeature(Object key) {
		if (key == CustomHandles.class) return this;
		return null;
	}

	
	//
	// AttributeSet methods
	//
	// It makes some sense for a wire to be its own attribute, since
	// after all it is immutable.
	//
	@Override
	public Object clone() { return this; }
	public void addAttributeListener(AttributeListener l) { }
	public void removeAttributeListener(AttributeListener l) { }
	public List<Attribute<?>> getAttributes() { return ATTRIBUTES; }
	public boolean containsAttribute(Attribute<?> attr) { return ATTRIBUTES.contains(attr); }
	public Attribute<?> getAttribute(String name) {
		for (Attribute<?> attr : ATTRIBUTES) {
			if (name.equals(attr.getName())) return attr;
		}
		return null;
	}
	public boolean isReadOnly(Attribute<?> attr) { return true; }
	public void setReadOnly(Attribute<?> attr, boolean value) {
		throw new UnsupportedOperationException();
	}
	public boolean isToSave(Attribute<?> attr) {
		return false;
	}

	@SuppressWarnings("unchecked")
	public <V> V getValue(Attribute<V> attr) {
		if (attr == dir_attr) {
			return (V) (is_x_equal ? VALUE_VERT : VALUE_HORZ);
		} else if (attr == len_attr) {
			return (V) Integer.valueOf(getLength());
		} else {
			return null;
		}
	}

	public <V> void setValue(Attribute<V> attr, V value) {
		throw new IllegalArgumentException("read only attribute");
	}

	//
	// other methods
	//
	public boolean isVertical() { return is_x_equal; }

	public Location getEndLocation(int index) { return index == 0 ? e0 : e1; }

	public Location getEnd0() { return e0; }

	public Location getEnd1() { return e1; }

	public Location getOtherEnd(Location loc) {
		return (loc.equals(e0) ? e1 : e0);
	}
	
	public boolean sharesEnd(Wire other) {
		return this.e0.equals(other.e0) || this.e1.equals(other.e0)
			|| this.e0.equals(other.e1) || this.e1.equals(other.e1);
	}

	public boolean overlaps(Wire other, boolean includeEnds) {
		return overlaps(other.e0, other.e1, includeEnds);
	}

	private boolean overlaps(Location q0, Location q1, boolean includeEnds) {
		if (is_x_equal) {
			int x0 = q0.getX();
			if (x0 != q1.getX() || x0 != e0.getX()) return false;
			if (includeEnds) {
				return e1.getY() >= q0.getY() && e0.getY() <= q1.getY();
			} else {
				return e1.getY() > q0.getY() && e0.getY() < q1.getY();
			}
		} else {
			int y0 = q0.getY();
			if (y0 != q1.getY() || y0 != e0.getY()) return false;
			if (includeEnds) {
				return e1.getX() >= q0.getX() && e0.getX() <= q1.getX();
			} else {
				return e1.getX() > q0.getX() && e0.getX() < q1.getX();
			}
		}
	}
	
	public boolean isParallel(Wire other) {
		return this.is_x_equal == other.is_x_equal;
	}
	
	public Iterator<Location> iterator() {
		return new WireIterator(e0, e1);
	}
	
	public void drawHandles(ComponentDrawContext context) {
		context.drawHandle(e0);
		context.drawHandle(e1);
	}
}
