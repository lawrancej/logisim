/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StringUtil {
	public static String format(String fmt, String... args) {
		StringWriter out = new StringWriter();
		out.flush();
		PrintWriter p = new PrintWriter(out);
		p.format(fmt, (Object[])args);
		return out.toString();
	}
	public static StringGetter formatter(final StringGetter base, final String arg) {
		return new StringGetter() {
			public String toString() {
				return format(base.toString(), arg);
			}
		};
	}
	
	public static StringGetter formatter(final StringGetter base, final StringGetter arg) {
		return new StringGetter() {
			public String toString() {
				return format(base.toString(), arg.toString());
			}
		};
	}
	
	public static StringGetter constantGetter(final String value) {
		return new StringGetter() {
			public String toString() {
				return value;
			}
		};
	}
	
	public static String toHexString(int bits, int value) {
		if (bits < 32) value &= (1 << bits) - 1;
		String ret = Integer.toHexString(value);
		int len = (bits + 3) / 4;
		while (ret.length() < len) ret = "0" + ret;
		if (ret.length() > len) ret = ret.substring(ret.length() - len);
		return ret;
	}
}
