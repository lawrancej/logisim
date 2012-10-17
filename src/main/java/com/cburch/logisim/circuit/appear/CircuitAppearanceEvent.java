/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit.appear;

import com.cburch.logisim.circuit.Circuit;

public class CircuitAppearanceEvent {
	public static final int APPEARANCE = 1;
	public static final int BOUNDS = 2;
	public static final int PORTS = 4;
	public static final int ALL_TYPES = 7;
	
	private Circuit circuit;
	private int affects;
	
	CircuitAppearanceEvent(Circuit circuit, int affects) {
		this.circuit = circuit;
		this.affects = affects;
	}
	
	public Circuit getSource() {
		return circuit;
	}
	
	public boolean isConcerning(int type) {
		return (affects & type) != 0;
	}
}
