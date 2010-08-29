/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

public class LoadFailedException extends Exception {
    LoadFailedException(String desc) {
        super(desc);
    }
}