/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.Lock;

public abstract class CircuitTransaction {
    public static final Integer READ_ONLY = Integer.valueOf(1);
    public static final Integer READ_WRITE = Integer.valueOf(2);
    
    protected abstract Map<Circuit,Integer> getAccessedCircuits();
    
    protected abstract void run(CircuitMutator mutator);
    
    public final CircuitTransactionResult execute() {
        CircuitMutatorImpl mutator = new CircuitMutatorImpl();
        Map<Circuit,Lock> locks = CircuitLocker.acquireLocks(this, mutator);
        CircuitTransactionResult result;
        try {
            this.run(mutator);
            
            Collection<Circuit> modified = mutator.getModifiedCircuits();
            for (Circuit circuit : modified) {
                WireRepair repair = new WireRepair(circuit);
                repair.run(mutator);
            }
            result = new CircuitTransactionResult(mutator);
            for (Circuit circuit : result.getModifiedCircuits()) {
                circuit.fireEvent(CircuitEvent.TRANSACTION_DONE, result);
            }
        } finally {
            CircuitLocker.releaseLocks(locks);
        }
        return result;
    }

}
