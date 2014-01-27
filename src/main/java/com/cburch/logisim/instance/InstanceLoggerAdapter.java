/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.instance;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.log.Loggable;

class InstanceLoggerAdapter implements Loggable {
    private InstanceComponent comp;
    private InstanceLogger logger;
    private InstanceStateImpl state;

    public InstanceLoggerAdapter(InstanceComponent comp, Class<? extends InstanceLogger> loggerClass) {
        try {
            this.comp = comp;
            this.logger = loggerClass.newInstance();
            this.state = new InstanceStateImpl(null, comp);
        } catch (Exception t) {
            handleError(t, loggerClass);
            logger = null;
        }
    }

    private void handleError(Throwable t, Class<? extends InstanceLogger> loggerClass) {
        String className = loggerClass.getName();
        //OK
        System.err.println("error while instantiating logger " + className
                + ": " + t.getClass().getName());
        String msg = t.getMessage();
        //OK
        if (msg != null) {
            System.err.println("  (" + msg + ")");
        }

    }

    @Override
    public Object[] getLogOptions(CircuitState circState) {
        if (logger != null) {
            updateState(circState);
            return logger.getLogOptions(state);
        } else {
            return null;
        }
    }

    @Override
    public String getLogName(Object option) {
        if (logger != null) {
            return logger.getLogName(state, option);
        } else {
            return null;
        }
    }

    @Override
    public Value getLogValue(CircuitState circuitState, Object option) {
        if (logger != null) {
            updateState(circuitState);
            return logger.getLogValue(state, option);
        } else {
            return Value.UNKNOWN;
        }
    }

    private void updateState(CircuitState circuitState) {
        if (state.getCircuitState() != circuitState) {
            state.repurpose(circuitState, comp);
        }
    }
}
