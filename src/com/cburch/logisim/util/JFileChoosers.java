/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

public class JFileChoosers {
	/* A user reported that JFileChooser's constructor sometimes resulted in
	 * IOExceptions when Logisim is installed under a system administrator
	 * account and then is attempted to run as a regular user. This class is
	 * an attempt to be a bit more robust about which directory the JFileChooser
	 * opens up under. (23 Feb 2010) */

	private static final String[] PROP_NAMES = {
		null, "user.home", "user.dir", "java.home", "java.io.tmpdir" };
	
	private JFileChoosers() { }
	
	public static JFileChooser create() {
		RuntimeException first = null;
		for (int i = 0; i < PROP_NAMES.length; i++) {
			String prop = PROP_NAMES[i];
			try {
				if (prop == null) {
					return new JFileChooser();
				} else {
					String dirname = System.getProperty(prop);
					if (dirname != null) {
						File dir = new File(dirname);
						if (dir.canRead()) {
						    return new JFileChooser(dir);
						}
					}
				}
			} catch (RuntimeException t) {
				if (first == null) first = t;
				Throwable u = t.getCause();
				if (!(u instanceof IOException)) throw t;
			}
		}
		throw first;
	}
	
	public static JFileChooser createAt(File openDirectory) {
		if (openDirectory == null) {
			return create();
		} else {
			try {
				return new JFileChooser(openDirectory);
			} catch (RuntimeException t) {
				if (t.getCause() instanceof IOException) {
					try { return create(); }
					catch (RuntimeException u) {}
				}
				throw t;
			}
		}
	}
	
	public static JFileChooser createSelected(File selected) {
		if (selected == null) {
			return create();
		} else {
			JFileChooser ret = createAt(selected.getParentFile());
			ret.setSelectedFile(selected);
			return ret;
		}
	}
}
