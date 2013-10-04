/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim;

public class LogisimVersion {
	private static final int FINAL_REVISION = Integer.MAX_VALUE / 4;
	
	/**
	 * creates a new LogisimVersion object without a revision number
	 * called in:
	 * com.cburch.logisim.Main
	 * com.cburch.logisim.file.XmlReader
	 * @param major
	 * @param minor
	 * @param release
	 * @return
	 */
	public static LogisimVersion get(int major, int minor, int release) {
		return get(major, minor, release, FINAL_REVISION);
	}

	/**
	 * creates a new LogisimVersion object with a revision number
	 * called in:
	 * com.cburch.logisim.circuit.SplitterFactory
	 * com.cburch.logisim.std.plexers.Decoder
	 * com.cburch.logisim.std.plexers.Demultiplexer
	 * com.cburch.logisim.std.plexers.Multiplexer
	 * @param major
	 * @param minor
	 * @param release
	 * @param revision
	 * @return
	 */
	public static LogisimVersion get(int major, int minor, int release, int revision) {
		return new LogisimVersion(major, minor, release, revision);
	}
	
	/**
	 * breaks up a single string containing the version number into several integers.
	 * uses "\\." as delimiter.
	 * @param versionString
	 * @return
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
	 * setter for variables that make up the logisim version number
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
	public int hashCode() {
		int ret = major * 31 + minor;
		ret = ret * 31 + release;
		ret = ret * 31 + revision;
		return ret;
	}
	
	/**
	 * checks if 2 objects of LogisimVersion are the same
	 */
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
	
	public int compareTo(LogisimVersion other) {
		int ret = this.major - other.major;
		if (ret != 0) {
			return ret;
		} else {
			ret = this.minor - other.minor;
			if (ret != 0) {
				return ret;
			} else {
				ret = this.release - other.release;
				if (ret != 0) {
					return ret;
				} else {
					return this.revision - other.revision;
				}
			}
		}
	}
	
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
