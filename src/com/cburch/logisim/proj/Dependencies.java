/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.proj;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitEvent;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.circuit.Subcircuit;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.util.Dag;

public class Dependencies {
    private class MyListener
            implements LibraryListener, CircuitListener {
        public void libraryChanged(LibraryEvent e) {
            switch (e.getAction()) {
            case LibraryEvent.ADD_TOOL:
                if (e.getData() instanceof AddTool) {
                    ComponentFactory factory = ((AddTool) e.getData()).getFactory();
                    if (factory instanceof Circuit) processCircuit((Circuit) factory);
                }
                break;
            case LibraryEvent.REMOVE_TOOL:
                if (e.getData() instanceof AddTool) {
                    ComponentFactory factory = ((AddTool) e.getData()).getFactory();
                    if (factory instanceof Circuit) {
                        Circuit circ = (Circuit) factory;
                        depends.removeNode(circ);
                        circ.removeCircuitListener(this);
                    }
                }
                break;
            }
        }

        public void circuitChanged(CircuitEvent e) {
            Component comp;
            switch (e.getAction()) {
            case CircuitEvent.ACTION_ADD:
                comp = (Component) e.getData();
                if (comp instanceof Subcircuit) {
                    depends.addEdge(e.getCircuit(),
                        comp.getFactory());
                }
                break;
            case CircuitEvent.ACTION_REMOVE:
                comp = (Component) e.getData();
                if (comp instanceof Subcircuit) {
                    Circuit sub = (Circuit) comp.getFactory();
                    boolean found = false;
                    for (Component o : e.getCircuit().getNonWires()) { 
                        if (o instanceof Subcircuit && o.getFactory() == sub) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) depends.removeEdge(e.getCircuit(), sub);
                }
                break;
            case CircuitEvent.ACTION_CLEAR:
                depends.removeNode(e.getCircuit());
                break;
            }
        }
    }

    private MyListener myListener = new MyListener();
    private Dag depends = new Dag();

    Dependencies(LogisimFile file) {
        addDependencies(file);
    }

    public boolean canRemove(Circuit circ) {
        return !depends.hasPredecessors(circ);
    }

    public boolean canAdd(Circuit circ, Circuit sub) {
        return depends.canFollow(sub, circ);
    }

    private void addDependencies(LogisimFile file) {
        file.addLibraryListener(myListener);
        for (AddTool tool : file.getTools()) {
            ComponentFactory src = tool.getFactory();
            if (src instanceof Circuit) {
                processCircuit((Circuit) src);
            }
        }
    }

    private void processCircuit(Circuit circ) {
        circ.addCircuitListener(myListener);
        for (Component comp : circ.getNonWires()) {
            if (comp instanceof Subcircuit) {
                ComponentFactory sub = comp.getFactory();
                depends.addEdge(circ, sub);
            }
        }
    }

}
