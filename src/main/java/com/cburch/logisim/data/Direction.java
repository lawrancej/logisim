/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.data;

import com.cburch.logisim.util.StringGetter;
import static com.cburch.logisim.util.LocaleString.*;

public class Direction implements AttributeOptionInterface {
	public static final Direction EAST
		= new Direction("east", __("directionEastOption"),
				__("directionEastVertical"), 0);
	public static final Direction WEST
		= new Direction("west", __("directionWestOption"),
				__("directionWestVertical"), 1);
	public static final Direction NORTH
		= new Direction("north", __("directionNorthOption"),
				__("directionNorthVertical"), 2);
	public static final Direction SOUTH
		= new Direction("south", __("directionSouthOption"),
				__("directionSouthVertical"), 3);
	public static final Direction[] cardinals
		= { NORTH, EAST, SOUTH, WEST };

	public static Direction parse(String str) {
		if (str.equals(EAST.name))  return EAST;
		if (str.equals(WEST.name))  return WEST;
		if (str.equals(NORTH.name)) return NORTH;
		if (str.equals(SOUTH.name)) return SOUTH;
		throw new NumberFormatException("illegal direction '" + str + "'");
	}

	private String name;
	private StringGetter disp;
	private StringGetter vert;
	private int id;

	private Direction(String name, StringGetter disp, StringGetter vert, int id) {
		this.name = name;
		this.disp = disp;
		this.vert = vert;
		this.id = id;
	}

	@Override
	public String toString() {
		return name;
	}

	public String toDisplayString() {
		return disp.toString();
	}
	
	public StringGetter getDisplayGetter() {
		return disp;
	}
	
	public String toVerticalDisplayString() {
		return vert.toString();
	}

	@Override
	public int hashCode() {
		return id;
	}
	
	public double toRadians() {
		if (this == Direction.EAST) return 0.0;
		if (this == Direction.WEST) return Math.PI;
		if (this == Direction.NORTH) return Math.PI / 2.0;
		if (this == Direction.SOUTH) return -Math.PI / 2.0;
		return 0.0;
	}
	
	public int toDegrees() {
		if (this == Direction.EAST) return 0;
		if (this == Direction.WEST) return 180;
		if (this == Direction.NORTH) return 90;
		if (this == Direction.SOUTH) return 270;
		return 0;
	}
	
	public Direction reverse() {
		if (this == Direction.EAST) return Direction.WEST;
		if (this == Direction.WEST) return Direction.EAST;
		if (this == Direction.NORTH) return Direction.SOUTH;
		if (this == Direction.SOUTH) return Direction.NORTH;
		return Direction.WEST;
	}
	
	public Direction getRight() {
		if (this == Direction.EAST) return Direction.SOUTH;
		if (this == Direction.WEST) return Direction.NORTH;
		if (this == Direction.NORTH) return Direction.EAST;
		if (this == Direction.SOUTH) return Direction.WEST;
		return Direction.WEST;
	}
	
	public Direction getLeft() {
		if (this == Direction.EAST) return Direction.NORTH;
		if (this == Direction.WEST) return Direction.SOUTH;
		if (this == Direction.NORTH) return Direction.WEST;
		if (this == Direction.SOUTH) return Direction.EAST;
		return Direction.WEST;
	}

	// for AttributeOptionInterface
	public Object getValue() {
		return this;
	}
}
