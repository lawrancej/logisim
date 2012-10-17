/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.util.Collection;

public class CircuitTransactionResult {
	private CircuitMutatorImpl mutator;
	
	CircuitTransactionResult(CircuitMutatorImpl mutator) {
		this.mutator = mutator;
	}
	
	public CircuitTransaction getReverseTransaction() {
		return mutator.getReverseTransaction();
	}
	
	public ReplacementMap getReplacementMap(Circuit circuit) {
		ReplacementMap ret = mutator.getReplacementMap(circuit);
		return ret == null ? new ReplacementMap() : ret;
	}
	
	public Collection<Circuit> getModifiedCircuits() {
		return mutator.getModifiedCircuits();
	}
}
