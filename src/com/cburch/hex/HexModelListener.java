/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.hex;

public interface HexModelListener {
	public void metainfoChanged(HexModel source);
	public void bytesChanged(HexModel source, long start, long numBytes,
			int[] oldValues);
}
