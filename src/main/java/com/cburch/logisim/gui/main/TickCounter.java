/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import com.cburch.logisim.circuit.Simulator;
import com.cburch.logisim.circuit.SimulatorEvent;
import com.cburch.logisim.circuit.SimulatorListener;

import static com.cburch.logisim.util.LocaleString.getFromLocale;

class TickCounter implements SimulatorListener {
    private static final int QUEUE_LENGTH = 1000;

    private final long[] queueTimes;
    private final double[] queueRates;
    private int queueStart;
    private int queueSize;
    private double tickFrequency;

    public TickCounter() {
        queueTimes = new long[QUEUE_LENGTH];
        queueRates = new double[QUEUE_LENGTH];
        queueSize = 0;
    }

    public void clear() {
        queueSize = 0;
    }

    @Override
    public void propagationCompleted(SimulatorEvent e) {
        Simulator sim = e.getSource();
        if (!sim.isTicking()) {
            queueSize = 0;
        }
    }

    @Override
    public void simulatorStateChanged(SimulatorEvent e) {
        propagationCompleted(e);
    }

    @Override
    public void tickCompleted(SimulatorEvent e) {
        Simulator sim = e.getSource();
        if (!sim.isTicking()) {
            queueSize = 0;
        } else {
            double freq = sim.getTickFrequency();
            if (freq != tickFrequency) {
                queueSize = 0;
                tickFrequency = freq;
            }

            int curSize = queueSize;
            int maxSize = queueTimes.length;
            int start = queueStart;
            int end;
            // new sample is added into queue
            if (curSize < maxSize) {
                end = start + curSize;
                if (end >= maxSize) {
                    end -= maxSize;
                }
                curSize++;
                queueSize = curSize;
            // new sample replaces oldest value in queue
            } else {
                end = queueStart;
                if (end + 1 >= maxSize) {
                    queueStart = 0;
                } else {
                    queueStart = end + 1;
                }
            }
            long startTime = queueTimes[start];
            long endTime = System.currentTimeMillis();
            double rate;
            if (startTime == endTime || curSize <= 1) {
                rate = Double.MAX_VALUE;
            } else {
                rate = 1000.0 * (double) (curSize - 1) / (double) (endTime - startTime);
            }
            queueTimes[end] = endTime;
            queueRates[end] = rate;
        }
    }

    public String getTickRate() {
        int size = queueSize;
        if (size <= 1) {
            return "";
        } else {
            int maxSize = queueTimes.length;
            int start = queueStart;
            int end = start + size - 1;
            if (end >= maxSize) {
                end -= maxSize;
            }
            double rate = queueRates[end];
            if (rate <= 0.0 || rate == Double.MAX_VALUE) {
                return "";
            } else {
                // Figure out the minimum over the previous 100 readings, and
                // base our rounding off of that. This is meant to provide some
                // stability in the rounding - we don't want the result to
                // oscillate rapidly between 990 Hz and 1 KHz - it's better for
                // it to oscillate between 990 Hz and 1005 Hz.
                int baseLen = size;
                if (baseLen > 100) {
                    baseLen = 100;
                }

                int baseStart = end - baseLen + 1;
                double min = rate;
                if (baseStart < 0) {
                    baseStart += maxSize;
                    for (int i = baseStart + maxSize; i < maxSize; i++) {
                        double x = queueRates[i];
                        if (x < min) {
                            min = x;
                        }

                    }
                    for (int i = 0; i < end; i++) {
                        double x = queueRates[i];
                        if (x < min) {
                            min = x;
                        }

                    }
                } else {
                    for (int i = baseStart; i < end; i++) {
                        double x = queueRates[i];
                        if (x < min) {
                            min = x;
                        }

                    }
                }
                if (min < 0.9 * rate) {
                    min = rate;
                }


                if (min >= 1000.0) {
                    return getFromLocale("tickRateKHz",
                            roundString(rate / 1000.0, min / 1000.0));
                } else {
                    return getFromLocale("tickRateHz", roundString(rate, min));
                }
            }
        }
    }

    private static String roundString(double val, double min) {
        // round so we have only three significant digits
        // invariant: a = 10^i
        int i = 0;
        // invariant: a * bm == min, a is power of 10
        double a = 1.0;
        double bm = min;
        double bv = val;
        if (bm >= 1000.0) {
            while (bm >= 1000.0) {
                i++;
                a *= 10.0;
                bm /= 10.0;
                bv /= 10.0;
            }
        } else {
            while (bm < 100.0) {
                i--;
                a /= 10.0;
                bm *= 10.0;
                bv *= 10.0;
            }
        }

        // Examples:
        // 2.34: i = -2, a = .2, b = 234
        // 20.1: i = -1, a = .1, b = 201

        // nothing after decimal point
        if (i >= 0) {
            return String.valueOf((int) Math.round(a * (double) Math.round(bv)));
        // keep some after decimal point
        } else {
            return String.format("%." + (-i) + 'f', a * bv);
        }
    }
}
