/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import static com.cburch.logisim.util.LocaleString.*;

public class AnalyzeException extends Exception {
	public static class Circular extends AnalyzeException {
		public Circular() {
			super(_("analyzeCircularError"));
		}
	}

	public static class Conflict extends AnalyzeException {
		public Conflict() {
			super(_("analyzeConflictError"));
		}
	}
	
	public static class CannotHandle extends AnalyzeException {
		public CannotHandle(String reason) {
			super(_("analyzeCannotHandleError", reason));
		}
	}
	
	public AnalyzeException() { }
	
	public AnalyzeException(String message) {
		super(message);
	}
}
