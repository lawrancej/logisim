/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.cburch.logisim.data.Value;

class LogThread extends Thread implements ModelListener {
	// file will be flushed with at least this frequency
	private static final int FLUSH_FREQUENCY = 500;
	
	// file will be closed after waiting this many milliseconds between writes
	private static final int IDLE_UNTIL_CLOSE = 10000;
	
	private Model model;
	private boolean canceled = false;
	private Object lock = new Object();
	private PrintWriter writer = null;
	private boolean headerDirty = true;
	private long lastWrite = 0;
	
	public LogThread(Model model) {
		this.model = model;
		model.addModelListener(this);
	}
	
	@Override
	public void run() {
		while (!canceled) {
			synchronized(lock) {
				if (writer != null) {
					if (System.currentTimeMillis() - lastWrite > IDLE_UNTIL_CLOSE) {
						writer.close();
						writer = null;
					} else {
						writer.flush();
					}
				}
			}
			try {
				Thread.sleep(FLUSH_FREQUENCY);
			} catch (InterruptedException e) { }
		}
		synchronized(lock) {
			if (writer != null) {
				writer.close();
				writer = null;
			}
		}
	}
	
	public void cancel() {
		synchronized(lock) {
			canceled = true;
			if (writer != null) {
				writer.close();
				writer = null;
			}
		}
	}

	public void selectionChanged(ModelEvent event) {
		headerDirty = true;
	}

	public void entryAdded(ModelEvent event, Value[] values) {
		synchronized(lock) {
			if (isFileEnabled()) addEntry(values);
		}
	}

	public void filePropertyChanged(ModelEvent event) {
		synchronized(lock) {
			if (isFileEnabled()) {
				if (writer == null) {
					Selection sel = model.getSelection();
					Value[] values = new Value[sel.size()];
					boolean found = false;
					for (int i = 0; i < values.length; i++) {
						values[i] = model.getValueLog(sel.get(i)).getLast();
						if (values[i] != null) found = true;
					}
					if (found) addEntry(values);
				}
			} else {
				if (writer != null) {
					writer.close();
					writer = null;
				}
			}
		}
	}
	
	private boolean isFileEnabled() {
		return !canceled && model.isSelected() && model.isFileEnabled()
			&& model.getFile() != null;
	}
	
	// Should hold lock and have verified that isFileEnabled() before
	// entering this method.
	private void addEntry(Value[] values) {
		if (writer == null) {
			try {
				writer = new PrintWriter(new FileWriter(model.getFile(), true));
			} catch (IOException e) {
				model.setFile(null);
				return;
			}
		}
		Selection sel = model.getSelection();
		if (headerDirty) {
			if (model.getFileHeader()) {
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < sel.size(); i++) {
					if (i > 0) buf.append("\t");
					buf.append(sel.get(i).toString());
				}
				writer.println(buf.toString());
			}
			headerDirty = false;
		}
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			if (i > 0) buf.append("\t");
			if (values[i] != null) {
				int radix = sel.get(i).getRadix();
				buf.append(values[i].toDisplayString(radix));
			}
		}
		writer.println(buf.toString());
		lastWrite = System.currentTimeMillis();
	}
}
