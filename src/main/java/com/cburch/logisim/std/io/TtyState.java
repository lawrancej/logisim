/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.io;

import java.util.Arrays;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.start.TtyInterface;
import com.cburch.logisim.instance.InstanceData;

class TtyState implements InstanceData, Cloneable {
    private Value lastClock;
    private String[] rowData;
    private int colCount;
    private StringBuffer lastRow;
    private int row;
    private boolean sendStdout;

    public TtyState(int rows, int cols) {
        lastClock = Value.UNKNOWN;
        rowData = new String[rows - 1];
        colCount = cols;
        lastRow = new StringBuffer(cols);
        sendStdout = false;
        clear();
    }

    @Override
    public TtyState clone() {
        try {
            TtyState ret = (TtyState) super.clone();
            ret.rowData = this.rowData.clone();
            return ret;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public Value setLastClock(Value newClock) {
        Value ret = lastClock;
        lastClock = newClock;
        return ret;
    }

    public void setSendStdout(boolean value) {
        sendStdout = value;
    }

    public void clear() {
        Arrays.fill(rowData, "");
        lastRow.delete(0, lastRow.length());
        row = 0;
    }

    public String getRowString(int index) {
        if (index < row) {
            return rowData[index];
        }

        else if (index == row) {
            return lastRow.toString();
        }

        else {
            return "";
        }

    }

    public int getCursorRow() {
        return row;
    }

    public int getCursorColumn() {
        return lastRow.length();
    }

    public void add(char c) {
        if (sendStdout) {
            TtyInterface.sendFromTty(c);
        }

        int lastLength = lastRow.length();
        switch (c) {
        // control-L
        case 12:
            row = 0;
            lastRow.delete(0, lastLength);
            Arrays.fill(rowData, "");
            break;
        // backspace
        case '\b':
            if (lastLength > 0) {
                lastRow.delete(lastLength - 1, lastLength);
            }

            break;
        // newline
        case '\n': case '\r':
            commit();
            break;
        default:
            if (!Character.isISOControl(c)) {
                if (lastLength == colCount) {
                    commit();
                }

                lastRow.append(c);
            }
        }
    }

    private void commit() {
        if (row >= rowData.length) {
            System.arraycopy(rowData, 1, rowData, 0, rowData.length - 1);
            rowData[row - 1] = lastRow.toString();
        } else {
            rowData[row] = lastRow.toString();
            row++;
        }
        lastRow.delete(0, lastRow.length());
    }

    public void updateSize(int rows, int cols) {
        int oldRows = rowData.length + 1;
        if (rows != oldRows) {
            String[] newData = new String[rows - 1];
            // rows have been added,
            if (rows > oldRows
                    // or rows removed but filled rows fit
                    || row < rows - 1) {
                System.arraycopy(rowData, 0, newData, 0, row);
                Arrays.fill(newData, row, rows - 1, "");
            // rows removed, and some filled rows must go
            } else {
                System.arraycopy(rowData, row - rows + 1, newData, 0, rows - 1);
                row = rows - 1;
            }
            rowData = newData;
        }

        int oldCols = colCount;
        if (cols != oldCols){
            colCount = cols;
            // will need to trim any long rows
            if (cols < oldCols) {
                for (int i = 0; i < rows - 1; i++) {
                    String s = rowData[i];
                    if (s.length() > cols) {
                        rowData[i] = s.substring(0, cols);
                    }

                }
                if (lastRow.length() > cols) {
                    lastRow.delete(cols, lastRow.length());
                }
            }
        }
    }
}
