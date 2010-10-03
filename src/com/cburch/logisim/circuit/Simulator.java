/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.util.ArrayList;

import com.cburch.logisim.comp.ComponentDrawContext;

public class Simulator {
	/** If PRINT_TICK_RATE is true, then the number of ticks per second is
	 * displayed once per TICK_RATE_QUANTUM ticks. */
	private static final boolean PRINT_TICK_RATE = false;
	private static final int TICK_RATE_QUANTUM = 512;

	/*begin DEBUGGING
	private static PrintWriter debug_log;
	
	static {
		try {
			debug_log = new PrintWriter(new BufferedWriter(new FileWriter("DEBUG")));
		} catch (IOException e) {
			System.err.println("Could not open debug log"); //OK
		}
	}
	
	public static void log(String msg) {
		debug_log.println(msg);
	}
	
	public static void flushLog() {
		debug_log.flush();
	}
	//end DEBUGGING*/
	
	class PropagationManager extends Thread {
		private Propagator propagator = null;
		private PropagationPoints stepPoints = new PropagationPoints();
		private volatile int ticksRequested = 0;
		private volatile int stepsRequested = 0;
		private volatile boolean resetRequested = false;
		private volatile boolean propagateRequested = false;
		private volatile boolean complete = false;

		// These variables apply only if PRINT_TICK_RATE is set
		int tickRateTicks = 0;
		long tickRateStart = System.currentTimeMillis();

		public Propagator getPropagator() {
			return propagator;
		}
		
		public void setPropagator(Propagator value) {
			propagator = value;
		}
		
		public synchronized void requestPropagate() {
			if (!propagateRequested) {
				propagateRequested = true;
				notifyAll();
			}
		}
		
		public synchronized void requestReset() {
			if (!resetRequested) {
				resetRequested = true;
				notifyAll();
			}
		}
		
		public synchronized void requestTick()  {
			if (ticksRequested < 16) {
				ticksRequested++;
			}
			notifyAll();
		}
		
		public synchronized void shutDown() {
			complete = true;
			notifyAll();
		}
		
		@Override
		public void run() {
			while (!complete) {
				synchronized(this) {
					while (!complete && !propagateRequested
							&& !resetRequested && ticksRequested == 0
							&& stepsRequested == 0) {
						try {
							wait();
						} catch (InterruptedException e) { }
					}
				}

				if (resetRequested) {
					resetRequested = false;
					if (propagator != null) propagator.reset();
					firePropagationCompleted();
				}
				
				if (propagateRequested || ticksRequested > 0 || stepsRequested > 0) {
					boolean ticked = false;
					propagateRequested = false;
					if (isRunning) {
						stepPoints.clear();
						stepsRequested = 0;
						if (propagator == null) {
							ticksRequested = 0;
						} else {
							ticked = ticksRequested > 0;
							if (ticked) doTick();
							do {
								propagateRequested = false;
								try {
									exceptionEncountered = false;
									propagator.propagate();
								} catch (Throwable thr) {
									thr.printStackTrace();
									exceptionEncountered = true;
									setIsRunning(false);
								}
							} while (propagateRequested);
							if (isOscillating()) {
								setIsRunning(false);
								ticksRequested = 0;
								propagateRequested = false;
							}
						}
					} else {
						if (stepsRequested > 0) {
							synchronized(this) {
								stepsRequested--;
							}
							exceptionEncountered = false;
							try {
								stepPoints.clear();
								propagator.step(stepPoints);
							} catch (Throwable thr) {
								thr.printStackTrace();
								exceptionEncountered = true;
							}
						}
					}
					if (ticked) fireTickCompleted();
					firePropagationCompleted();
				}
			}
		}
		
		private void doTick() {
			synchronized(this) {
				ticksRequested--;
			}
			propagator.tick();
			if (PRINT_TICK_RATE) {
				tickRateTicks++;
				if (tickRateTicks >= TICK_RATE_QUANTUM) {
					long a = tickRateStart;
					long b = System.currentTimeMillis();
					double t = 1000 * TICK_RATE_QUANTUM / (b - a);
					if (t >= 100.0) {
						t += 0.5;
						System.out.println((int) t + " Hz"); //OK
					} else {
						// This is so we display only three significant digits
						double fact = 0.1;
						while (t < 1 / fact) fact *= 10.0;
						fact *= 100.0;
						t = (int) (t * fact + 0.5) / fact;
						System.out.println(t + " Hz"); //OK
					}
					tickRateTicks = 0;
					tickRateStart = b;
				}
			}
		}
	}

	private boolean isRunning = true;
	private boolean isTicking = false;
	private boolean exceptionEncountered = false;
	private double tickFrequency = 1.0;

	private PropagationManager manager;
	private SimulatorTicker ticker;
	private ArrayList<SimulatorListener> listeners
		= new ArrayList<SimulatorListener>();

	public Simulator() {
		manager = new PropagationManager();
		ticker = new SimulatorTicker(manager);
		try {
			manager.setPriority(manager.getPriority() - 1);
			ticker.setPriority(ticker.getPriority() - 1);
		} catch (SecurityException e) {
		} catch (IllegalArgumentException e) { }
		manager.start();
		ticker.start();
	}
	
	public void shutDown() {
		ticker.shutDown();
		manager.shutDown();
	}

	public void setCircuitState(CircuitState state) {
		manager.setPropagator(state.getPropagator());
		renewTickerAwake();
	}
	
	public CircuitState getCircuitState() {
		Propagator prop = manager.getPropagator();
		return prop == null ? null : prop.getRootState();
	}
	
	public void requestReset() {
		manager.requestReset();
	}
	
	public void tick() {
		ticker.tickOnce();
	}
	
	public void step() {
		synchronized(manager) {
			manager.stepsRequested++;
			manager.notifyAll();
		}
	}
	
	public void drawStepPoints(ComponentDrawContext context) {
		manager.stepPoints.draw(context);
	}
	
	public boolean isExceptionEncountered() {
		return exceptionEncountered;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setIsRunning(boolean value) {
		if (isRunning != value) {
			isRunning = value;
			renewTickerAwake();
			/*DEBUGGING - comment out: 
			if (!value) flushLog(); //*/
			fireSimulatorStateChanged();
		}
	}

	public boolean isTicking() {
		return isTicking;
	}

	public void setIsTicking(boolean value) {
		if (isTicking != value) {
			isTicking = value;
			renewTickerAwake();
			fireSimulatorStateChanged();
		}
	}
	
	private void renewTickerAwake() {
		ticker.setAwake(isRunning && isTicking && tickFrequency > 0);
	}

	public double getTickFrequency() {
		return tickFrequency;
	}

	public void setTickFrequency(double freq) {
		if (tickFrequency != freq) {
			int millis = (int) Math.round(1000 / freq);
			int ticks;
			if (millis > 0) {
				ticks = 1;
			} else {
				millis = 1;
				ticks = (int) Math.round(freq / 1000);
			}
			
			tickFrequency = freq;
			ticker.setTickFrequency(millis, ticks);
			renewTickerAwake();
			fireSimulatorStateChanged();
		}
	}

	public void requestPropagate() {
		manager.requestPropagate();
	}

	public boolean isOscillating() {
		Propagator prop = manager.getPropagator();
		return prop != null && prop.isOscillating();
	}

	public void addSimulatorListener(SimulatorListener l) { listeners.add(l); }
	public void removeSimulatorListener(SimulatorListener l) { listeners.remove(l); }
	void firePropagationCompleted() {
		SimulatorEvent e = new SimulatorEvent(this);
		for (SimulatorListener l : new ArrayList<SimulatorListener>(listeners)) {
			l.propagationCompleted(e);
		}
	}
	void fireTickCompleted() {
		SimulatorEvent e = new SimulatorEvent(this);
		for (SimulatorListener l : new ArrayList<SimulatorListener>(listeners)) {
			l.tickCompleted(e);
		}
	}
	void fireSimulatorStateChanged() {
		SimulatorEvent e = new SimulatorEvent(this);
		for (SimulatorListener l : new ArrayList<SimulatorListener>(listeners)) {
			l.simulatorStateChanged(e);
		}
	}
}
