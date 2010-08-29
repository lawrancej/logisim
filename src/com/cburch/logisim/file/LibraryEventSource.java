/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

public interface LibraryEventSource {
	public void addLibraryListener(LibraryListener listener);
	public void removeLibraryListener(LibraryListener listener);
}
