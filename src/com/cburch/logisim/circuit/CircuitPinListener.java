/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

interface CircuitPinListener {
    public void pinAdded();
    public void pinRemoved();
    public void pinChanged();
}
