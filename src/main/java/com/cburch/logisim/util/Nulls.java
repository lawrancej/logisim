/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

/**
 * Null safe equality testing class.
 * Obsolete in Java 7, thanks to static method java.util.Objects.equal(a,b).
 * Until Java 7 becomes popular, use Commons Lang ObjectUtils.equal instead.
 * @author Joey Lawrance
 *
 */
public final class Nulls {
	
	/**
	 * Null safe equality test equivalent to java.util.Objects.equal or ObjectUtils.equal
	 * @param a
	 * @param b
	 * @return true if a 
	 */
	@Deprecated
	public static boolean equal(Object a, Object b) {
		if(a == null || b == null) {
			return a == b;
		} else {
			return a.equals(b);
		}
	}
	
	@Deprecated
	public static boolean unequal(Object a, Object b) {
		return !equal(a,b);
	}
}
