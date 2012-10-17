/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.Simulator;

public interface SimulateListener {
	public void stateChangeRequested(Simulator sim, CircuitState state);
}
