/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.proj;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.tools.Tool;


public class ProjectEvent {
    // change file
    public final static int ACTION_SET_FILE     = 0;
    // change current
    public final static int ACTION_SET_CURRENT  = 1;
    // change tool
    public final static int ACTION_SET_TOOL     = 2;
    // selection alterd
    public final static int ACTION_SELECTION    = 3;
    // circuit state changed
    public final static int ACTION_SET_STATE    = 4;
    // action about to start
	public static final int REDO_START			= 11;
	public static final int REDO_COMPLETE		= 12;
    public static final int ACTION_START        = 5;
    // action has completed
    public static final int ACTION_COMPLETE     = 6;
    // one action has been appended to another
    public static final int ACTION_MERGE        = 7;
    // undo about to start
    public static final int UNDO_START          = 8;
    // undo has completed
    public static final int UNDO_COMPLETE       = 9;
    // canvas should be repainted
    public static final int REPAINT_REQUEST     = 10;

    private int action;
    private Project proj;
    private Object old_data;
    private Object data;

    ProjectEvent(int action, Project proj, Object old, Object data) {
        this.action = action;
        this.proj = proj;
        this.old_data = old;
        this.data = data;
    }

    ProjectEvent(int action, Project proj, Object data) {
        this.action = action;
        this.proj = proj;
        this.data = data;
    }

    ProjectEvent(int action, Project proj) {
        this.action = action;
        this.proj = proj;
        this.data = null;
    }

    // access methods
    public int getAction() {
        return action;
    }

    public Project getProject() {
        return proj;
    }

    public Object getOldData() {
        return old_data;
    }

    public Object getData() {
        return data;
    }

    // convenience methods
    public LogisimFile getLogisimFile() {
        return proj.getLogisimFile();
    }

    public Circuit getCircuit() {
        return proj.getCurrentCircuit();
    }

    public Tool getTool() {
        return proj.getTool();
    }

}
