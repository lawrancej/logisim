/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

public class SimulatorPrototype extends Thread {
    /*   TODO this is a development version of a new implementation of the
//   simulator class, which should allow for simulation cycles to be
//   controlled by the bus.
    /** Simulation will be slowed THROTTLE_FREQ times per second.
    private static final int THROTTLE_FREQ = 100;
    
    /** Record snapshots of time every SNAPSHOT_COUNT throttles.
    private static final int SNAPSHOT_COUNT = THROTTLE_FREQ;

    // TODO monitor simulation speed
    // TODO identify oscillation
    
    private Propagator prop;
    private ArrayList listeners = new ArrayList();
    private boolean keepAlive = true;
    private boolean released = false;
    private boolean isRunning = true;
    private boolean isExceptionEncountered = false;
    private long nanoCount = Long.MIN_VALUE;
    private Object lock = new Object();

    private int nanosPerSecond = 1000000;
    private int nanosPerThrottle = 10000;
    private long lastThrottle = 0; // in nanos
    private long nextThrottle = 1; // in nanos
    private long lastThrottleTime = 0;

    private boolean isTicking = false;
    private boolean tickOnce = false;
    private int nanosPerTick = 10000;
    private long lastTick = 0; // in nanos
    private long nextTick = 10000;
    
    private long[] snapshotNano = new long[SNAPSHOT_COUNT];
    private long[] snapshotTime = new long[SNAPSHOT_COUNT];
    private int snapshotIndex;
    
    public SimulatorPrototype() {
        try {
            setPriority(getPriority() - 1);
        } catch (SecurityException e) {
        } catch (IllegalArgumentException e) { }
        start();
    }

    public void addSimulatorListener(SimulatorListener l) {
        listeners.add(l);
    }
    
    public void removeSimulatorListener(SimulatorListener l) {
        listeners.remove(l);
    }
    
    private void firePropagationCompleted() {
        SimulatorEvent e = new SimulatorEvent(this);
        ArrayList listeners = new ArrayList(this.listeners);
        for (Iterator it = listeners.iterator(); it.hasNext(); ) {
            SimulatorListener l = (SimulatorListener) it.next();
            l.propagationCompleted(e);
        }
    }
    
    private void fireTickCompleted() {
        SimulatorEvent e = new SimulatorEvent(this);
        ArrayList listeners = new ArrayList(this.listeners);
        for (Iterator it = listeners.iterator(); it.hasNext(); ) {
            ((SimulatorListener) it.next()).tickCompleted(e);
        }
    }
    
    private void fireSimulatorStateChanged() {
        SimulatorEvent e = new SimulatorEvent(this);
        ArrayList listeners = new ArrayList(this.listeners);
        for (Iterator it = listeners.iterator(); it.hasNext(); ) {
            ((SimulatorListener) it.next()).simulatorStateChanged(e);
        }
    }

    public CircuitState getCircuitState() {
        return prop.getRootState();
    }
    
    public void setCircuitState(CircuitState state) {
        synchronized(lock) {
            prop = state.getPropagator();
            lock.notifyAll();
        }
    }
    
    public boolean isExceptionEncountered() {
        return isExceptionEncountered;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public void setIsRunning(boolean value) {
        synchronized(lock) {
            isRunning = value;
            tickOnce = false;
            lock.notifyAll();
        }
    }
    
    public boolean isTicking() {
        return isTicking;
    }
    
    public void tickOnce() {
        System.err.println("tickOnce");
        synchronized(lock) {
            if (tickOnce) return;
            if (!isTicking) {
                lastTick = nanoCount;
            } else {
                lastTick = nanoCount - nanosPerTick;
            }
            nextTick = nanoCount + nanosPerTick;
            tickOnce = true;
            isRunning = true;
            isTicking = true;
            lock.notifyAll();
        }
    }
    
    public void setIsTicking(boolean value) {
        synchronized(lock) {
            if (isTicking == value) return;
            isTicking = value;
            if (value) {
                lastTick = nanoCount;
                nextTick = nanoCount + nanosPerTick;
                tickOnce = false;
            }
            lock.notifyAll();
        }
    }
    
    public void releaseUserEvents() {
        synchronized(lock) {
            released = true;
            lock.notifyAll();
        }
    }
    
    public void shutDown() {
        synchronized(lock) {
            keepAlive = false;
            lock.notifyAll();
        }
    }
    
    public int getNanosPerSecond() {
        return nanosPerSecond;
    }
    
    public void setNanosPerSecond(int value) {
        synchronized(lock) {
            nanosPerSecond = value;
            if (value > 0) {
                nanosPerThrottle = value <= THROTTLE_FREQ ? 1 : value / THROTTLE_FREQ;
                nextThrottle = lastThrottle + nanosPerThrottle;
            } else {
                nanosPerThrottle = Integer.MAX_VALUE;
                nextThrottle = Long.MAX_VALUE;
            }
            lock.notifyAll();
        }
    }
    
    public int getNanosPerTick() {
        return nanosPerTick;
    }
    
    public void setNanosPerTick(int value) {
        synchronized(lock) {
            nanosPerTick = value;
            nextTick = lastTick + nanosPerTick;
            lock.notifyAll();
        }
    }
    
    public int getSimulationSpeed() {
        long nano0;
        long nano1;
        long time0;
        long time1;
        synchronized(lock) {
            nano0 = snapshotNano[snapshotIndex];
            time0 = snapshotTime[snapshotIndex];
            nano1 = nanoCount;
        }
        time1 = System.currentTimeMillis();
        return (int) ((2 * (nano1 - nano0) + 1) / (2 * (time1 - time0)));
    }
    
    // TODO user interaction interface changes should be batched
    // until action is completed
    
    public void run() {
        synchronized(lock) {
            while (keepAlive && prop == null) {
                try {
                    lock.wait();
                } catch (InterruptedException e) { }
            }
        }
        
        long now = System.currentTimeMillis();
        for (int i = 0; i < SNAPSHOT_COUNT; i++) {
            snapshotNano[i] = Long.MIN_VALUE;
            snapshotTime[i] = now;
        }
        
        while (keepAlive) {
            step();
        }
    }
    
    private void step() {
        synchronized(lock) {
            waitForRelease();
            if (!keepAlive) return;
            
            nanoCount++;
            try {
                released = false;
                isExceptionEncountered = false;
                System.err.println("step simulation " + prop.isPending());
                prop.step();
            } catch (Throwable t) {
                isExceptionEncountered = true;
                isRunning = false;
                return;
            }
            
            if (isTicking && nanosPerTick != 0 && nanoCount >= nextTick) {
                try {
                    System.err.println("tick simulation");
                    prop.tick();
                    if (tickOnce) {
                        tickOnce = false;
                        isTicking = false;
                        isRunning = false;
                    }
                    fireTickCompleted();
                } catch (Throwable t) {
                    isExceptionEncountered = true;
                    isRunning = false;
                    return;
                }
                lastTick = nanoCount;
                nextTick = nanoCount + nanosPerTick;
            }
            
            if (nanoCount >= nextThrottle) {
                fireSimulatorStateChanged(); // TODO differentiate or remove
                firePropagationCompleted();
                addSnapshot();
                throttleNanoRate();
            }
        }
    }
    
    private void waitForRelease() {
        if (isRunning && (released || prop.isPending())) return;
        
        // wait until either something pops up as pending
        // simulation, or until the next tick
        fireSimulatorStateChanged(); // TODO differentiate or remove
        firePropagationCompleted();
        long now = System.currentTimeMillis();
        do {
            boolean wasRunning = isRunning;
            try {
                if (wasRunning && isTicking && nanosPerTick > 0) {
                    long wait = 1000 * (nextTick - nanoCount) / nanosPerSecond;
                    if (wait <= 0) return;
                    System.err.println(nanoCount + ": begin wait " + wait);
                    lock.wait(wait);
                } else {
                    System.err.println(nanoCount + ": begin wait indefinite");
                    lock.wait();
                }
            } catch (InterruptedException e) { }

            if (!keepAlive) return;
            long next = System.currentTimeMillis();
            if (wasRunning) {
                long nanosElapsed = nanosPerSecond * (next - now) / 1000;
                System.err.println("elapsed: " + nanosElapsed);
                nanoCount += nanosElapsed;
                if (nanoCount > nextTick) nanoCount = nextTick;
            }
            now = next;
        } while (!isRunning || (!released && !prop.isPending()));
    }
    
    private void addSnapshot() {
        snapshotNano[snapshotIndex] = nanoCount;
        snapshotTime[snapshotIndex] = System.currentTimeMillis();
        ++snapshotIndex;
        if (snapshotIndex == SNAPSHOT_COUNT) snapshotIndex = 0;
    }
    
    private void throttleNanoRate() {
        System.err.println("throttle simulation");
        long actualTime;
        while (true) {
            if (nanosPerThrottle == 0) {
                nextThrottle = Long.MAX_VALUE;
                return;
            }
            if (nanoCount < nextThrottle) return;
            // compute how long to throttle by
            actualTime = System.currentTimeMillis();
            long wantedTime = lastThrottleTime
                + 1000 * (nanoCount - lastThrottle) / nanosPerSecond;
            if (actualTime >= wantedTime) break;
            try { lock.wait(wantedTime - actualTime); }
            catch (InterruptedException e) { }
        }
        lastThrottle = nanoCount;
        lastThrottleTime = actualTime;
        nextThrottle = lastThrottle + nanosPerThrottle;
    }
    */
}
