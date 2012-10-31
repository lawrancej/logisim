/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.apache.commons.collections15.EnumerationUtils;

public class LocaleManager {
	// static members
	private static final String SETTINGS_NAME = "settings";
	private static ArrayList<LocaleManager> managers = new ArrayList<LocaleManager>();
	
	private static class LocaleGetter implements StringGetter {
		private LocaleManager source;
		private String key;

		LocaleGetter(LocaleManager source, String key) {
			this.source = source;
			this.key = key;
		}

		public String toString() {
			return source.get(key);
		}
	}
	
	private static ArrayList<LocaleListener> listeners = new ArrayList<LocaleListener>();
	private static boolean replaceAccents = false; 
	private static HashMap<Character,String> repl = null;
	private static Locale curLocale = null;

	public static Locale getLocale() {
		if (curLocale == null) {
			curLocale = Locale.getDefault();
		}
		return curLocale;
	}

	public static void setLocale(Locale loc) {
		Locale cur = getLocale();
		if (!loc.equals(cur)) {
			Locale[] opts = LocaleString.getUtilLocaleManager().getLocaleOptions();
			Locale select = null;
			Locale backup = null;
			String locLang = loc.getLanguage();
			for (Locale opt : opts) {
				if (select == null && opt.equals(loc)) {
					select = opt;
				}
				if (backup == null && opt.getLanguage().equals(locLang)) {
					backup = opt;
				}
			}
			if (select == null) {
				if (backup == null) {
					select = new Locale("en");
				} else {
					select = backup;
				}
			}
			
			curLocale = select;
			Locale.setDefault(select);
			for (LocaleManager man : managers) {
				man.loadDefault();
			}
			repl = replaceAccents ? fetchReplaceAccents() : null;
			fireLocaleChanged();
		}
	}
	
	public static boolean canReplaceAccents() {
		return fetchReplaceAccents() != null;
	}
	
	public static void setReplaceAccents(boolean value) {
		HashMap<Character,String> newRepl = value ? fetchReplaceAccents() : null;
		replaceAccents = value;
		repl = newRepl;
		fireLocaleChanged();
	}
	
	private static HashMap<Character,String> fetchReplaceAccents() {
		HashMap<Character,String> ret = null;
		String val;
		try {
			val = LocaleString.getUtilLocaleManager().locale.getString("accentReplacements");
		} catch (MissingResourceException e) {
			return null;
		}
		StringTokenizer toks = new StringTokenizer(val, "/");
		while (toks.hasMoreTokens()) {
			String tok = toks.nextToken().trim();
			char c = '\0';
			String s = null;
			if (tok.length() == 1) {
				c = tok.charAt(0);
				s = "";
			} else if (tok.length() >= 2 && tok.charAt(1) == ' ') {
				c = tok.charAt(0);
				s = tok.substring(2).trim();
			}
			if (s != null) {
				if (ret == null) ret = new HashMap<Character,String>();
				ret.put(new Character(c), s);
			}
		}
		return ret;
	}

	public static void addLocaleListener(LocaleListener l) {
		listeners.add(l);
	}

	public static void removeLocaleListener(LocaleListener l) {
		listeners.remove(l);
	}

	private static void fireLocaleChanged() {
		for (LocaleListener l : listeners) {
			l.localeChanged();
		}
	}

	// instance members
	private String dir_name;
	private String file_start;
	private ResourceBundle settings = null;
	private ResourceBundle locale = null;
	private ResourceBundle dflt_locale = null;

	public LocaleManager(String dir_name, String file_start) {
		this.dir_name = dir_name;
		this.file_start = file_start;
		loadDefault();
		managers.add(this);
	}

	private void loadDefault() {
		if (settings == null) {
			try {
				settings = ResourceBundle.getBundle(dir_name + "/" + SETTINGS_NAME);
			} catch (java.util.MissingResourceException e) { }
		}

		try {
			loadLocale(Locale.getDefault());
			if (locale != null) return;
		} catch (java.util.MissingResourceException e) { }
		try {
			loadLocale(Locale.ENGLISH);
			if (locale != null) return;
		} catch (java.util.MissingResourceException e) { }
		Locale[] choices = getLocaleOptions();
		if (choices != null && choices.length > 0) loadLocale(choices[0]);
		if (locale != null) return;
		throw new RuntimeException("No locale bundles are available");
	}

	private void loadLocale(Locale loc) {
		String bundleName = dir_name + "/" + loc.getLanguage() + "/" + file_start;
		locale = ResourceBundle.getBundle(bundleName, loc);
	}

	public Iterable<String> getKeys() {
		return EnumerationUtils.toList(locale.getKeys());
	}

	public String get(String key) {
		String ret;
		try {
			ret = locale.getString(key);
		} catch (MissingResourceException e) {
			ResourceBundle backup = dflt_locale;
			if (backup == null) {
				Locale backup_loc = Locale.US;
				backup = ResourceBundle.getBundle(dir_name + "/en/" + file_start, backup_loc);
				dflt_locale = backup;
			}
			try {
				ret = backup.getString(key);
			} catch (MissingResourceException e2) {
				ret = key;
			}
		}
		HashMap<Character,String> repl = LocaleManager.repl;
		if (repl != null) ret = replaceAccents(ret, repl);
		return ret;
	}

	public StringGetter getter(String key) {
		return new LocaleGetter(this, key);
	}
	
	public StringGetter getter(String key, String arg) {
		return StringUtil.formatter(getter(key), arg);
	}
	
	public Locale[] getLocaleOptions() {
		String locs = null;
		try {
			if (settings != null) locs = settings.getString("locales");
		} catch (java.util.MissingResourceException e) { }
		if (locs == null) return new Locale[] { };

		ArrayList<Locale> retl = new ArrayList<Locale>();
		StringTokenizer toks = new StringTokenizer(locs);
		while (toks.hasMoreTokens()) {
			String f = toks.nextToken();
			String language;
			String country;
			if (f.length() >= 2) {
				language = f.substring(0, 2);
				country = (f.length() >= 5 ? f.substring(3, 5) : null);
			} else {
				language = null;
				country = null;
			}
			if (language != null) {
				Locale loc = country == null ? new Locale(language) : new Locale(language, country);
				retl.add(loc);
			}
		}

		return retl.toArray(new Locale[retl.size()]);
	}
	
	public JComponent createLocaleSelector() {
		Locale[] locales = getLocaleOptions();
		if (locales == null || locales.length == 0) {
			Locale cur = getLocale();
			if (cur == null) cur = new Locale("en");
			locales = new Locale[] { cur };
		}
		return new JScrollPane(new LocaleSelector(locales));
	}
	
	private static String replaceAccents(String src, HashMap<Character,String> repl) {
		// find first non-standard character - so we can avoid the
		// replacement process if possible
		int i = 0;
		int n = src.length();
		for (; i < n; i++) {
			char ci = src.charAt(i);
			if (ci < 32 || ci >= 127) break;
		}
		if (i == n) return src;
		
		// ok, we'll have to consider replacing accents
		char[] cs = src.toCharArray();
		StringBuilder ret = new StringBuilder(src.substring(0, i));
		for (int j = i; j < cs.length; j++) {
			char cj = cs[j];
			if (cj < 32 || cj >= 127) {
				String out = repl.get(Character.valueOf(cj));
				if (out != null) {
					ret.append(out);
				} else {
					ret.append(cj);
				}
			} else {
				ret.append(cj);
			}
		}
		return ret.toString();
	}
}
