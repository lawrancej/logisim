/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

import com.cburch.logisim.tools.Library;

interface LibraryLoader {
	public Library loadLibrary(String desc);
	public String getDescriptor(Library lib);
	public void showError(String description);
}
