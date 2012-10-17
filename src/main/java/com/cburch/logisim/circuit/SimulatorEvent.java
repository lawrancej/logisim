/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

public class SimulatorEvent {
	private Simulator source;

	public SimulatorEvent(Simulator source) {
		this.source = source;
	}

	public Simulator getSource() {
		return source;
	}
}
