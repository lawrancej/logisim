/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

class SimulatorTicker extends Thread {
	private Simulator.PropagationManager manager;
	private int ticksPerTickPhase;
	private int millisPerTickPhase;

	private boolean shouldTick;
	private int ticksPending;
	private boolean complete;
	
	public SimulatorTicker(Simulator.PropagationManager manager) {
		this.manager = manager;
		ticksPerTickPhase = 1;
		millisPerTickPhase = 1000;
		shouldTick = false;
		ticksPending = 0;
		complete = false;
	}
	
	public synchronized void setTickFrequency(int millis, int ticks) {
		millisPerTickPhase = millis;
		ticksPerTickPhase = ticks;
	}

	synchronized void setAwake(boolean value) {
		shouldTick = value;
		if (shouldTick) notifyAll();
	}
	
	public synchronized void shutDown() {
		complete = true;
		notifyAll();
	}
	
	public synchronized void tickOnce() {
		ticksPending++;
		notifyAll();
	}

	@Override
	public void run() {
		long lastTick = System.currentTimeMillis();
		while (true) {
			boolean curShouldTick = shouldTick;
			int millis = millisPerTickPhase;
			int ticks = ticksPerTickPhase;
			try {
				synchronized(this) {
					curShouldTick = shouldTick;
					millis = millisPerTickPhase;
					ticks = ticksPerTickPhase;
					while (!curShouldTick && ticksPending == 0
							&& !complete) {
						wait();
						curShouldTick = shouldTick;
						millis = millisPerTickPhase;
						ticks = ticksPerTickPhase;
					}
				}
			} catch (InterruptedException e) { }
			
			if (complete) break;
			
			int toTick;
			long now = System.currentTimeMillis();
			if (curShouldTick && now - lastTick >= millis) {
				toTick = ticks;
			} else {
				toTick = ticksPending;
			}

			if (toTick > 0) {
				lastTick = now;
				for (int i = 0; i < toTick; i++) {
					manager.requestTick();
				}
				synchronized(this) {
					if (ticksPending > toTick) ticksPending -= toTick;
					else ticksPending = 0;
				}
				// we fire tickCompleted in this thread so that other
				// objects (in particular the repaint process) can slow
				// the thread down.
			}

			try {
				long nextTick = lastTick + millis;
				int wait = (int) (nextTick - System.currentTimeMillis());
				if (wait < 1) wait = 1;
				if (wait > 100) wait = 100;
				Thread.sleep(wait);
			} catch (InterruptedException e) { }
		}
	}
}
