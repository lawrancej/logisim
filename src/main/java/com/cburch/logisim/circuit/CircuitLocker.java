/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class CircuitLocker {
	private static AtomicInteger NEXT_SERIAL_NUMBER = new AtomicInteger(0);

	private int serialNumber;
	private ReadWriteLock circuitLock;
	private transient Thread mutatingThread;
	private CircuitMutatorImpl mutatingMutator;
	
	CircuitLocker() {
		serialNumber = NEXT_SERIAL_NUMBER.getAndIncrement();
		circuitLock = new ReentrantReadWriteLock();
		mutatingThread = null;
		mutatingMutator = null;
	}
	
	public boolean hasWriteLock() {
		return mutatingThread == Thread.currentThread();
	}
	
	CircuitMutatorImpl getMutator() {
		return mutatingMutator;
	}
	
	void checkForWritePermission(String operationName) {
		if (mutatingThread != Thread.currentThread()) {
			throw new IllegalStateException(operationName + " outside transaction");
		}
	}
	
	void execute(CircuitTransaction xn) {
		if (mutatingThread == Thread.currentThread()) {
			xn.run(mutatingMutator);
		} else {
			xn.execute();
		}
	}
	
	private static class CircuitComparator implements Comparator<Circuit> {
		public int compare(Circuit a, Circuit b) {
			int an = a.getLocker().serialNumber;
			int bn = b.getLocker().serialNumber;
			return an - bn;
		}
	}
	
	static Map<Circuit,Lock> acquireLocks(CircuitTransaction xn,
			CircuitMutatorImpl mutator) {
		Map<Circuit,Integer> requests = xn.getAccessedCircuits();
		Map<Circuit,Lock> circuitLocks = new HashMap<Circuit,Lock>();
		// Acquire locks in serial-number order to avoid deadlock
		Circuit[] lockOrder = requests.keySet().toArray(new Circuit[0]);
		Arrays.sort(lockOrder, new CircuitComparator());
		try {
			for (Circuit circ : lockOrder) {
				Integer access = requests.get(circ);
				CircuitLocker locker = circ.getLocker();
				if (access == CircuitTransaction.READ_ONLY) {
					Lock lock = locker.circuitLock.readLock();
					lock.lock();
					circuitLocks.put(circ, lock);
				} else if (access == CircuitTransaction.READ_WRITE) {
					Thread curThread = Thread.currentThread();
					if (locker.mutatingThread == curThread) {
						; // nothing to do - thread already has lock
					} else {
						Lock lock = locker.circuitLock.writeLock();
						lock.lock();
						circuitLocks.put(circ, lock);
						locker.mutatingThread = Thread.currentThread();
						if (mutator == null) {
							mutator = new CircuitMutatorImpl();
						}
						locker.mutatingMutator = mutator;
					}
				}
			}
		} catch (RuntimeException t) {
			releaseLocks(circuitLocks);
			throw t;
		}
		return circuitLocks;
	}
	
	static void releaseLocks(Map<Circuit,Lock> locks) {
		Thread curThread = Thread.currentThread();
		for (Map.Entry<Circuit,Lock> entry : locks.entrySet()) {
			Circuit circ = entry.getKey();
			Lock lock = entry.getValue();
			CircuitLocker locker = circ.getLocker();
			if (locker.mutatingThread == curThread) {
				locker.mutatingThread = null;
				locker.mutatingMutator = null;
			}
			lock.unlock();
		}
	}
}
