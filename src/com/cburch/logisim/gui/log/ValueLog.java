/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.log;

import com.cburch.logisim.data.Value;

class ValueLog {
	private static final int LOG_SIZE = 400;

	private Value[] log;
	private short curSize;
	private short firstIndex;
	
	public ValueLog() {
		log = new Value[LOG_SIZE];
		curSize = 0;
		firstIndex = 0;
	}
	
	public int size() {
		return curSize;
	}
	
	public Value get(int index) {
		int i = firstIndex + index;
		if (i >= LOG_SIZE) i -= LOG_SIZE;
		return log[i];
	}
	
	public Value getLast() {
		return curSize < LOG_SIZE ? (curSize == 0 ? null : log[curSize - 1])
				: (firstIndex == 0 ? log[curSize - 1] : log[firstIndex - 1]);
	}
	
	public void append(Value val) {
		if (curSize < LOG_SIZE) {
			log[curSize] = val;
			curSize++;
		} else {
			log[firstIndex] = val;
			firstIndex++;
			if (firstIndex >= LOG_SIZE) firstIndex = 0;
		}
	}
}
