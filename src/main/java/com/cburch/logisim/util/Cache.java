/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

/**
 * Allows immutable objects to be cached in memory in order to reduce
 * the creation of duplicate objects.
 */
public class Cache {
	private int mask;
	private Object[] data;
	
	public Cache() {
		this(8);
	}
	public Cache(int logSize) {
		if (logSize > 12) logSize = 12;
		
		data = new Object[1 << logSize];
		mask = data.length - 1;
	}
	
	public Object get(int hashCode) {
		return data[hashCode & mask];
	}
	
	public void put(int hashCode, Object value) {
		if (value != null) {
			data[hashCode & mask] = value;
		}
	}
	
	public Object get(Object value) {
		if (value == null) return null;
		int code = value.hashCode() & mask;
		Object ret = data[code];
		if (ret != null && ret.equals(value)) {
			return ret;
		} else {
			data[code] = value;
			return value;
		}
	}
}
