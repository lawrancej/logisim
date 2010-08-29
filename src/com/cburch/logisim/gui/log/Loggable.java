/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.log;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.Value;

public interface Loggable {
	public Object[] getLogOptions(CircuitState state);
	public String getLogName(Object option);
	public Value getLogValue(CircuitState state, Object option);
}
