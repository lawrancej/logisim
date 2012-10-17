/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.data;

/**
 * Represents the dimensions of a rectangle. This is analogous to
 * java.awt's <code>Dimension</code> class, except that objects of this type
 * are immutable.
 */
public class Size {
	public static Size create(int wid, int ht) {
		return new Size(wid, ht);
	}

	private final int wid;
	private final int ht;

	private Size(int wid, int ht) {
		this.wid = wid;
		this.ht = ht;
	}

	@Override
	public boolean equals(Object other_obj) {
		if (!(other_obj instanceof Size)) return false;
		Size other = (Size) other_obj;
		return wid == other.wid && ht == other.ht;
	}

	@Override
	public String toString() {
		return wid + "x" + ht;
	}

	public int getWidth() {
		return wid;
	}

	public int getHeight() {
		return ht;
	}

	public java.awt.Dimension toAwtDimension() {
		return new java.awt.Dimension(wid, ht);
	}


	public boolean contains(Location p) {
		return contains(p.getX(), p.getY());
	}

	public boolean contains(int x, int y) {
		return x >= 0 && y >= 0 && x < this.wid && y < this.ht;
	}

	public boolean contains(int x, int y, int wid, int ht) {
		int oth_x = (wid <= 0 ? x : x + wid - 1);
		int oth_y = (ht <= 0 ? y : y + wid - 1);
		return contains(x, y) && contains(oth_x, oth_y);
	}

	public boolean contains(Size bd) {
		return contains(bd.wid, bd.ht);
	}

}
