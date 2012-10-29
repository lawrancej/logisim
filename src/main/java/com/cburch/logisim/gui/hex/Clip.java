/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.hex;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.JOptionPane;

import com.cburch.hex.Caret;
import com.cburch.hex.HexEditor;
import com.cburch.hex.HexModel;
import static com.cburch.logisim.util.LocaleString.*;

class Clip implements ClipboardOwner {
	private static final DataFlavor binaryFlavor = new DataFlavor(int[].class, "Binary data");
	
	private static class Data implements Transferable {
		private int[] data;
		
		Data(int[] data) {
			this.data = data;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { binaryFlavor, DataFlavor.stringFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor == binaryFlavor || flavor == DataFlavor.stringFlavor;
		}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (flavor == binaryFlavor) {
				return data;
			} else if (flavor == DataFlavor.stringFlavor) {
				int bits = 1;
				for (int i = 0; i < data.length; i++) {
					int k = data[i] >> bits;
					while (k != 0 && bits < 32) {
						bits++;
						k >>= 1;
					}
				}
				
				int chars = (bits + 3) / 4;
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < data.length; i++) {
					if (i > 0) {
						buf.append(i % 8 == 0 ? '\n' : ' ');
					}
					String s = Integer.toHexString(data[i]);
					while (s.length() < chars) s = "0" + s;
					buf.append(s);
				}
				return buf.toString();
			} else {
				throw new UnsupportedFlavorException(flavor); 
			}
		}
	}
	
	private HexEditor editor;
	
	Clip(HexEditor editor) {
		this.editor = editor;
	}
	
	public void copy() {
		Caret caret = editor.getCaret();
		long p0 = caret.getMark();
		long p1 = caret.getDot();
		if (p0 < 0 || p1 < 0) return;
		if (p0 > p1) {
			long t = p0; p0 = p1; p1 = t;
		}
		p1++;
		
		int[] data = new int[(int) (p1 - p0)];
		HexModel model = editor.getModel();
		for (long i = p0; i < p1; i++) {
			data[(int) (i - p0)] = model.get(i);
		}
		
		Clipboard clip = editor.getToolkit().getSystemClipboard();
		clip.setContents(new Data(data), this);
	}
	
	public boolean canPaste() {
		Clipboard clip = editor.getToolkit().getSystemClipboard();
		Transferable xfer = clip.getContents(this);
		return xfer.isDataFlavorSupported(binaryFlavor);
	}
	
	public void paste() {
		Clipboard clip = editor.getToolkit().getSystemClipboard();
		Transferable xfer = clip.getContents(this);
		int[] data;
		if (xfer.isDataFlavorSupported(binaryFlavor)) {
			try {
				data = (int[]) xfer.getTransferData(binaryFlavor);
			} catch (UnsupportedFlavorException e) {
				return;
			} catch (IOException e) {
				return;
			}
		} else if (xfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			String buf;
			try {
				buf = (String) xfer.getTransferData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException e) {
				return;
			} catch (IOException e) {
				return;
			}
			
			try {
				data = HexFile.parse(new StringReader(buf));
			} catch (IOException e) {
				JOptionPane.showMessageDialog(editor.getRootPane(),
						e.getMessage(),
						// _("hexPasteSupportedError"),
						_("hexPasteErrorTitle"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		} else {
			JOptionPane.showMessageDialog(editor.getRootPane(),
				_("hexPasteSupportedError"),
				_("hexPasteErrorTitle"),
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		Caret caret = editor.getCaret();
		long p0 = caret.getMark();
		long p1 = caret.getDot();
		if (p0 == p1) {
			HexModel model = editor.getModel();
			if (p0 + data.length - 1 <= model.getLastOffset()) {
				model.set(p0, data);
			} else {
				JOptionPane.showMessageDialog(editor.getRootPane(),
						_("hexPasteEndError"),
						_("hexPasteErrorTitle"),
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			if (p0 < 0 || p1 < 0) return;
			if (p0 > p1) {
				long t = p0; p0 = p1; p1 = t;
			}
			p1++;
			
			HexModel model = editor.getModel();
			if (p1 - p0 == data.length) {
				model.set(p0, data);
			} else {
				JOptionPane.showMessageDialog(editor.getRootPane(),
						_("hexPasteSizeError"),
						_("hexPasteErrorTitle"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public void lostOwnership(Clipboard clip, Transferable transfer) { }

}
