/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

public interface SimulatorListener {
	public void propagationCompleted(SimulatorEvent e);
	public void tickCompleted(SimulatorEvent e);
	public void simulatorStateChanged(SimulatorEvent e);
}
