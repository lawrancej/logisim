/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.legacy;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;

class GroupedReader {
	private int depth = 0;
	private BufferedReader reader;
	private int line_number = 1;
	private String buffer;

	public GroupedReader(Reader reader) {
		this.reader = new BufferedReader(reader);
	}
	public GroupedReader(BufferedReader reader) {
		this.reader = reader;
	}

	public void close() throws IOException {
		reader.close();
	}

	public String readLine() throws IOException {
		getBuffer();

		// find end of buffer (if any)
		int pos_lb = findFirstUnescaped(buffer, '{');
		int pos_rb = findFirstUnescaped(buffer, '}');
		int pos = pos_lb;
		if (pos_rb >= 0 && (pos == -1 || pos_rb < pos)) pos = pos_rb;

		// compute ret; trim buffer
		String ret;
		if (pos < 0) {
			ret = buffer;
			buffer = null;
		} else {
			ret = buffer.substring(0, pos);
			buffer = buffer.substring(pos);
		}

		ret = unprotect(ret);
		return ret;
	}

	public void beginGroup() throws IOException {
		getBuffer();
		if (buffer.charAt(0) != '{') {
			throw new IOException(Strings.get("notStartError"));
		}
		++depth;
		buffer = buffer.substring(1);
	}
	public void startGroup() throws IOException {
		beginGroup();
	}
	public void endGroup() throws IOException {
		getBuffer();
		if (buffer.charAt(0) != '}') {
			throw new IOException(Strings.get("notEndError"));
		}
		--depth;
		buffer = buffer.substring(1);
	}
	public boolean atFileEnd() throws IOException {
		getBuffer();
		return buffer == null;
	}
	public boolean atGroupEnd() throws IOException {
		getBuffer();
		return buffer.charAt(0) == '}';
	}
	public boolean atGroupStart() throws IOException {
		getBuffer();
		return buffer != null && buffer.charAt(0) == '{';
	}
	public void skipGroup() throws IOException {
		boolean started = atGroupStart();
		if (started) startGroup();
		while (true) {
			if (atGroupStart()) skipGroup();
			if (atGroupEnd()) break;
			readLine();
		}
		if (started) endGroup();
	}

	public String readGroup() throws IOException {
		beginGroup();
		StringBuilder ret = new StringBuilder(readLine());
		getBuffer();
		while (buffer.charAt(0) != '}') {
			ret.append('\n');
			ret.append(readLine());
			getBuffer();
		}
		endGroup();
		return ret.toString();
	}

	private void getBuffer() throws IOException {
		if (buffer != null && buffer.length() > 0) return;
		
		++line_number;
		buffer = reader.readLine();
		if (buffer == null) return;

		int i = 0;
		while (i < depth && buffer.length() > i
				&& buffer.charAt(i) == '\t') {
			i++;
		}
		buffer = buffer.substring(i);
	}
	private int findFirstUnescaped(String search, char find) {
		int pos = 0;
		while (true) {
			int next = search.indexOf(find, pos);
			if (next < 0) {
				return -1;
			}
			int escape = search.indexOf('\\', pos);
			if (escape >= 0 && escape < next
					&& escape + 2 < search.length()) {
				pos = escape + 2;
			} else {
				return next;
			}
		}
	}
	private String unprotect(String what) {
		int pos = 0;
		StringBuilder ret = new StringBuilder();
		while (true) {
			int newpos = what.indexOf('\\', pos);
			if (newpos < 0) break;

			ret.append(what.substring(pos, newpos));
			ret.append(what.charAt(newpos + 1));
			pos = newpos + 2;
		}
		ret.append(what.substring(pos));
		return ret.toString();
	}
}
