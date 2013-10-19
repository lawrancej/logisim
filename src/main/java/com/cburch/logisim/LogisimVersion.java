/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim;
/**
 * Handles everything involving Logisim's version number
 * @author Carl Burch, Ryan Steinmetz
 *
 */
public class LogisimVersion implements Comparable<LogisimVersion> {
	private static final int FINAL_REVISION = Integer.MAX_VALUE / 4;
	
	/**
	 * Creates a new LogisimVersion object without a revision number
	 * @param major
	 * @param minor
	 * @param release
	 * @return a LogisimVersion object
	 */
	public static LogisimVersion get(int major, int minor, int release) {
		return get(major, minor, release, FINAL_REVISION);
	}

	/**
	 * Creates a new LogisimVersion object with a revision number
	 * @param major
	 * @param minor
	 * @param release
	 * @param revision
	 * @return a LogisimVersion object
	 */
	public static LogisimVersion get(int major, int minor, int release, int revision) {
		return new LogisimVersion(major, minor, release, revision);
	}
	
	/**
	 * Breaks up a single string containing the version number into several integers.
	 * Uses "." as delimiter.
	 * @param versionString
	 * @return a LogisimVersion object
	 */
	public static LogisimVersion parse(String versionString) {
		String[] parts = versionString.split("\\.");
		int major = 0;
		int minor = 0;
		int release = 0;
		int revision = FINAL_REVISION;
		try {
			if (parts.length >= 1) major = Integer.parseInt(parts[0]);
			if (parts.length >= 2) minor = Integer.parseInt(parts[1]);
			if (parts.length >= 3) release = Integer.parseInt(parts[2]);
			if (parts.length >= 4) revision = Integer.parseInt(parts[3]);
		} catch (NumberFormatException e) { }
		return new LogisimVersion(major, minor, release, revision);
	}
	
	private int major;
	private int minor;
	private int release;
	private int revision;
	private String repr;
	
	/**
	 * Logisim version number constructor. Versions have the form: major.minor.release.revision
	 * @param major
	 * @param minor
	 * @param release
	 * @param revision
	 */
	private LogisimVersion(int major, int minor, int release, int revision) {
		this.major = major;
		this.minor = minor;
		this.release = release;
		this.revision = revision;
		this.repr = null;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof LogisimVersion) {
			LogisimVersion o = (LogisimVersion) other;
			return this.major == o.major && this.minor == o.minor
				&& this.release == o.release && this.revision == o.revision;
		} else {
			return false;
		}
	}
	
	@Override
	public int compareTo(LogisimVersion other) {
		int ret = this.major - other.major;
		if (ret != 0) return ret;
		ret = this.minor - other.minor;
		if (ret != 0) return ret;
		ret = this.release - other.release;
		if (ret != 0) return ret;
		return this.revision - other.revision;
	}
	
	/**
	 * converts version number into a string
	 * @return ret
	 */
	@Override
	public String toString() {
		String ret = repr;
		if (ret == null) {
			ret = major + "." + minor + "." + release;
			if (revision != FINAL_REVISION) ret += "." + revision;
			repr = ret;
		}
		return ret;
	}
}
