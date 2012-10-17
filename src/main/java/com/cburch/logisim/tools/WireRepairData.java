/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;

import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.data.Location;

public class WireRepairData {
	private Wire wire;
	private Location point;
	
	public WireRepairData(Wire wire, Location point) {
		this.wire = wire;
		this.point = point;
	}
	
	public Location getPoint() {
		return point;
	}
	
	public Wire getWire() {
		return wire;
	}
}
