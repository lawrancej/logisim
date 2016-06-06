/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.proj.Project;

import javax.swing.JList;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
class CircuitJList extends JList<Circuit> {
    public CircuitJList(Project proj, boolean includeEmpty) {
        LogisimFile file = proj.getLogisimFile();
        Circuit current = proj.getCurrentCircuit();
        List<Circuit> options = new ArrayList<>();
        boolean currentFound = false;
        for (Circuit circ : file.getCircuits()) {
            if (!includeEmpty || circ.getBounds() != Bounds.EMPTY_BOUNDS) {
                if (circ == current) currentFound = true;
                options.add(circ);
            }
        }
        setListData(options.toArray(new Circuit[options.size()]));
        if (currentFound) setSelectedValue(current, true);
        setVisibleRowCount(Math.min(6, options.size()));
    }
}
