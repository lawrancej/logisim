/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim;

public class LogisimVersion {
    public static LogisimVersion get(int major, int minor, int revision) {
        return new LogisimVersion(major, minor, revision);
    }
    
    public static LogisimVersion parse(String versionString) {
        String[] parts = versionString.split(".");
        int major = 0;
        int minor = 0;
        int revision = 0;
        try {
            if (parts.length >= 1) major = Integer.parseInt(parts[0]);
            if (parts.length >= 2) minor = Integer.parseInt(parts[1]);
            if (parts.length >= 3) revision = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) { }
        return new LogisimVersion(major, minor, revision);
    }
    
    private int major;
    private int minor;
    private int revision;
    private String repr;
    
    private LogisimVersion(int major, int minor, int revision) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.repr = null;
    }
    
    @Override
    public int hashCode() {
        return (major * 31 + minor) * 31 + revision;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof LogisimVersion) {
            LogisimVersion o = (LogisimVersion) other;
            return this.major == o.major && this.minor == o.minor
                && this.revision == o.revision;
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
                return this.revision - other.revision;
            }
        }
    }
    
    @Override
    public String toString() {
        String ret = repr;
        if (ret == null) {
            ret = major + "." + minor + "." + revision;
            repr = ret;
        }
        return ret;
    }
}
