/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.hex;

public interface HexModel {
	/** Registers a listener for changes to the values. */
	public void addHexModelListener(HexModelListener l);
	
	/** Unregisters a listener for changes to the values. */
	public void removeHexModelListener(HexModelListener l);
	
	/** Returns the offset of the initial value to be displayed. */
	public long getFirstOffset();
	
	/** Returns the number of values to be displayed. */
	public long getLastOffset();
	
	/** Returns number of bits in each value. */
	public int getValueWidth();
	
	/** Returns the value at the given address. */
	public int get(long address);
	
	/** Changes the value at the given address. */
	public void set(long address, int value);
	
	/** Changes a series of values at the given addresses. */
	public void set(long start, int[] values);
	
	/** Fills a series of values with the same value. */
	public void fill(long start, long length, int value);
}
