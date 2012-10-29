/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.prefs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import static com.cburch.logisim.util.LocaleString.*;

public class Template {
	public static Template createEmpty() {
		String circName = _("newCircuitName");
		StringBuilder buf = new StringBuilder();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buf.append("<project version=\"1.0\">");
		buf.append(" <circuit name=\"" + circName + "\" />");
		buf.append("</project>");
		return new Template(buf.toString());
	}
	
	public static Template create(InputStream in) {
		InputStreamReader reader = new InputStreamReader(in);
		char[] buf = new char[4096];
		StringBuilder dest = new StringBuilder();
		while (true) {
			try {
				int nbytes = reader.read(buf);
				if (nbytes < 0) break;
				dest.append(buf, 0, nbytes);
			} catch (IOException e) {
				break;
			}
		}
		return new Template(dest.toString());
	}
	
	private String contents;
	
	private Template(String contents) {
		this.contents = contents;
	}
	
	public InputStream createStream() {
		try {
			return new ByteArrayInputStream(contents.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			System.err.println("warning: UTF-8 is not supported"); //OK
			return new ByteArrayInputStream(contents.getBytes());
		}
	}
}
