/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;


public class CircuitEvent {
	public final static int ACTION_SET_NAME = 0; // name changed
	public final static int ACTION_ADD      = 1; // component added
	public final static int ACTION_REMOVE   = 2; // component removed
	public final static int ACTION_CHANGE   = 3; // component changed
	public final static int ACTION_INVALIDATE   = 4; // component invalidated (pin types changed)
	public final static int ACTION_CLEAR    = 5; // entire circuit cleared
	public final static int TRANSACTION_DONE = 6;

	private int action;
	private Circuit circuit;
	private Object data;

	CircuitEvent(int action, Circuit circuit, Object data) {
		this.action = action;
		this.circuit = circuit;
		this.data = data;
	}

	// access methods
	public int getAction() {
		return action;
	}

	public Circuit getCircuit() {
		return circuit;
	}

	public Object getData() {
		return data;
	}

	public CircuitTransactionResult getResult() {
		return (CircuitTransactionResult) data;
	}
}
