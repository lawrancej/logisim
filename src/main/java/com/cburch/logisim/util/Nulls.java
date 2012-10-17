/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

public final class Nulls {
	private Nulls() { }
	
	public static boolean equal(Object a, Object b) {
		if(a == null) {
			return b == null;
		} else if(b == null) {
			return false;
		} else {
			return a.equals(b);
		}
	}
	
	public static boolean unequal(Object a, Object b) {
		if(a == null) {
			return b != null;
		} else if(b == null) {
			return true;
		} else {
			return !a.equals(b);
		}
	}
}
