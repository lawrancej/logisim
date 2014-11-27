/* Copyright (c) 2010, Carl Burch.
 * Copyright (c) 2014, Spanti Nicola (RyDroid)
 * License information is located in the com.cburch.logisim.
 * Main source code and at https://github.com/lawrancej/logisim/
 */

package com.cburch.logisim.util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Useful functions for manipulating strings
 */
public class StringUtil {
    public static String formatter(final String base, final String arg) {
    	return String.format(base, arg);
    }

    public static String constantGetter(final String value) {
    	return value;
    }

    public static String toHexString(int bits, int value) {
        if (bits < 32) {
        	value &= (1 << bits) - 1;
        }
        String ret = Integer.toHexString(value);
        int len = (bits + 3) / 4;
        while (ret.length() < len) {
        	ret = "0" + ret;
        }
        if (ret.length() > len) {
        	ret = ret.substring(ret.length() - len);
        }
        return ret;
    }
    
    /**
     * Joins all elements of an array into a string with a separator starting where the iterator is
     * It can be useful for not starting at a given element
     * @param iterator of an array of strings
     * @param separator between elements
     * @return String
     */
    public static String join(Iterator<String> it, final String separator) {
    	if (it != null && it.hasNext()) {
            StringBuilder ret = new StringBuilder();
            ret.append(it.next());
            while (it.hasNext()) {
                ret.append(separator);
                ret.append(it.next());
            }
            return ret.toString();
        }
        return "";
    }
    
    /**
     * Joins all elements of an array into a string with a separator
     * @param array of strings
     * @param separator between elements
     * @return String
     */
    public static String join(final ArrayList<String> array, final String separator) {
    	if(array == null) {
    		return "";
    	}
    	return StringUtil.join(array.iterator(), separator);
    }
}
