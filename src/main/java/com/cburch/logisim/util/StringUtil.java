/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

public class StringUtil {
    public static String formatter(final String base, final String arg) {
    	return String.format(base, arg);
    }

    public static String constantGetter(final String value) {
    	return value;
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
