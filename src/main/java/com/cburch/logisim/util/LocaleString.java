package com.cburch.logisim.util;

import java.util.HashMap;

/**
 * Given a string, return the locale specific translation,
 * regardless of section.
 * Replaces Strings.get, Strings.getter.
 * 
 * @author Joey Lawrance
 *
 */
public class LocaleString {
	private static LocaleString self = null;
	private String[] sections = ("analyze circuit data draw file gui hex " +
			"log menu opts prefs proj start std tools util").split(" ");
	private HashMap<String,LocaleManager> sourceMap = new HashMap<String,LocaleManager>();
	private LocaleString() {
		for (String section : sections) {
			LocaleManager manager = new LocaleManager("logisim", section);
			for (String key : manager.getKeys()) {
				sourceMap.put(key, manager);
			}
		}
	}
	private static LocaleString getInstance() {
		if (self == null) {
			self = new LocaleString();
		}
		return self;
	}
	public static String _(String s) {
		return getInstance().sourceMap.get(s).get(s);
	}
	public static String _(String key, String arg) {
		return StringUtil.format(_(key), arg);
	}
	public static String _(String key, String arg0, String arg1) {
		return StringUtil.format(_(key), arg0, arg1);
	}
	public static StringGetter __(String s) {
		return getInstance().sourceMap.get(s).getter(s);
	}
	public static StringGetter __(String key, String arg) {
		return getInstance().sourceMap.get(key).getter(key, arg);
	}
	public static StringGetter __(String key, StringGetter arg) {
		return __(key, arg.toString());
	}
}
