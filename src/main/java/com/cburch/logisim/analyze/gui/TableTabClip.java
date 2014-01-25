/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.gui;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import com.cburch.logisim.analyze.model.Entry;
import com.cburch.logisim.analyze.model.TruthTable;
import static com.cburch.logisim.util.LocaleString._;

class TableTabClip implements ClipboardOwner {
	private static final DataFlavor binaryFlavor = new DataFlavor(Data.class, "Binary data");
	
	private static class Data implements Transferable, Serializable {
		private String[] headers;
		private String[][] contents;
		
		Data(String[] headers, String[][] contents) {
			this.headers = headers;
			this.contents = contents;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { binaryFlavor, DataFlavor.stringFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor == binaryFlavor || flavor == DataFlavor.stringFlavor;
		}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (flavor == binaryFlavor) {
				return this;
			} else if (flavor == DataFlavor.stringFlavor) {
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < headers.length; i++) {
					buf.append(headers[i]);
					buf.append(i == headers.length - 1 ? '\n' : '\t');
				}
				for (int i = 0; i < contents.length; i++) {
					for (int j = 0; j < contents[i].length; j++) {
						buf.append(contents[i][j]);
						buf.append(j == contents[i].length - 1 ? '\n' : '\t');
					}
				}
				return buf.toString();
			} else {
				throw new UnsupportedFlavorException(flavor); 
			}
		}
	}
	
	private TableTab table;
	
	TableTabClip(TableTab table) {
		this.table = table;
	}
	
	public void copy() {
		TableTabCaret caret = table.getCaret();
		int c0 = caret.getCursorCol();
		int r0 = caret.getCursorRow();
		int c1 = caret.getMarkCol();
		int r1 = caret.getMarkRow();
		if (c1 < c0) { int t = c0; c0 = c1; c1 = t; }
		if (r1 < r0) { int t = r0; r0 = r1; r1 = t; }
		
		TruthTable t = table.getTruthTable();
		int inputs = t.getInputColumnCount();
		String[] header = new String[c1 - c0 + 1];
		for (int c = c0; c <= c1; c++) {
			if (c < inputs) {
				header[c - c0] = t.getInputHeader(c);
			} else {
				header[c - c0] = t.getOutputHeader(c - inputs);
			}
		}
		String[][] contents = new String[r1 - r0 + 1][c1 - c0 + 1];
		for (int r = r0; r <= r1; r++) {
			for (int c = c0; c <= c1; c++) {
				if (c < inputs) {
					contents[r - r0][c - c0] = t.getInputEntry(r, c).getDescription();
				} else {
					contents[r - r0][c - c0] = t.getOutputEntry(r, c - inputs).getDescription();
				}
			}
		}
		
		Clipboard clip = table.getToolkit().getSystemClipboard();
		clip.setContents(new Data(header, contents), this);
	}
	
	public boolean canPaste() {
		Clipboard clip = table.getToolkit().getSystemClipboard();
		Transferable xfer = clip.getContents(this);
		return xfer.isDataFlavorSupported(binaryFlavor);
	}
	
	public void paste() {
		Clipboard clip = table.getToolkit().getSystemClipboard();
		Transferable xfer;
		try {
			xfer = clip.getContents(this);
		} catch (IllegalStateException|ArrayIndexOutOfBoundsException e ) {
			// I don't know - the above was observed to throw an odd ArrayIndexOutOfBounds
			// exception on a Linux computer using Sun's Java 5 JVM
			JOptionPane.showMessageDialog(table.getRootPane(),
					_("clipPasteSupportedError"),
					_("clipPasteErrorTitle"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		Entry[][] entries;
		if (xfer.isDataFlavorSupported(binaryFlavor)) {
			try {
				Data data = (Data) xfer.getTransferData(binaryFlavor);
				entries = new Entry[data.contents.length][];
				for (int i = 0; i < entries.length; i++) {
					Entry[] row = new Entry[data.contents[i].length];
					for (int j = 0; j < row.length; j++) {
						row[j] = Entry.parse(data.contents[i][j]);
					}
					entries[i] = row;
				}
			} catch (UnsupportedFlavorException e) {
				return;
			} catch (IOException e) {
				return;
			}
		} else if (xfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				String buf = (String) xfer.getTransferData(DataFlavor.stringFlavor);
				StringTokenizer lines = new StringTokenizer(buf, "\r\n");
				String first;
				if (!lines.hasMoreTokens()) return;
				first = lines.nextToken();
				StringTokenizer toks = new StringTokenizer(first, "\t,");
				String[] headers = new String[toks.countTokens()];
				Entry[] firstEntries = new Entry[headers.length];
				boolean allParsed = true;
				for (int i = 0; toks.hasMoreTokens(); i++) {
					headers[i] = toks.nextToken();
					firstEntries[i] = Entry.parse(headers[i]);
					allParsed = allParsed && firstEntries[i] != null;
				}
				int rows = lines.countTokens();
				if (allParsed) rows++;
				entries = new Entry[rows][];
				int cur = 0;
				if (allParsed) {
					entries[0] = firstEntries;
					cur++;
				}
				while (lines.hasMoreTokens()) {
					toks = new StringTokenizer(lines.nextToken(), "\t");
					Entry[] ents = new Entry[toks.countTokens()];
					for (int i = 0; toks.hasMoreTokens(); i++) {
						ents[i] = Entry.parse(toks.nextToken());
					}
					entries[cur] = ents;
					cur++;
				}
			} catch (UnsupportedFlavorException e) {
				return;
			} catch (IOException e) {
				return;
			}
		} else {
			JOptionPane.showMessageDialog(table.getRootPane(),
				_("clipPasteSupportedError"),
				_("clipPasteErrorTitle"),
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		TableTabCaret caret = table.getCaret();
		int c0 = caret.getCursorCol();
		int c1 = caret.getMarkCol();
		int r0 = caret.getCursorRow();
		int r1 = caret.getMarkRow();
		if (r0 < 0 || r1 < 0 || c0 < 0 || c1 < 0) return;
		TruthTable model = table.getTruthTable();
		int rows = model.getRowCount();
		int inputs = model.getInputColumnCount();
		int outputs = model.getOutputColumnCount();
		if (c0 == c1 && r0 == r1) {
			if (r0 + entries.length > rows
					|| c0 + entries[0].length > inputs + outputs) {
				JOptionPane.showMessageDialog(table.getRootPane(),
						_("clipPasteEndError"),
						_("clipPasteErrorTitle"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		} else {
			if (r0 > r1) { int t = r0; r0 = r1; r1 = t; }
			if (c0 > c1) { int t = c0; c0 = c1; c1 = t; }
			
			if (r1 - r0 + 1 != entries.length
					|| c1 - c0 + 1 != entries[0].length) {
				JOptionPane.showMessageDialog(table.getRootPane(),
						_("clipPasteSizeError"),
						_("clipPasteErrorTitle"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		for (int r = 0; r < entries.length; r++) {
			for (int c = 0; c < entries[0].length; c++) {
				if (c0 + c >= inputs) {
					model.setOutputEntry(r0 + r, c0 + c - inputs,
							entries[r][c]);
				}
			}
		}
	}
	
	public void lostOwnership(Clipboard clip, Transferable transfer) { }

}
