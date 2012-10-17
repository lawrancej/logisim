/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.hex;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

class Highlighter {
	private static class Entry {
		private long start;
		private long end;
		private Color color;
		
		Entry(long start, long end, Color color) {
			this.start = start;
			this.end = end;
			this.color = color;
		}
	}
	
	private HexEditor hex;
	private ArrayList<Entry> entries;
	
	Highlighter(HexEditor hex) {
		this.hex = hex;
		this.entries = new ArrayList<Entry>();
	}
	
	public synchronized Object add(long start, long end, Color color) {
		HexModel model = hex.getModel();
		if (model == null) return null;
		if (start > end) {
			long t = start; start = end; end = t;
		}
		if (start < model.getFirstOffset()) start = model.getFirstOffset();
		if (end > model.getLastOffset()) end = model.getLastOffset();
		if (start >= end) return null;
		
		Entry entry = new Entry(start, end, color);
		entries.add(entry);
		expose(entry);
		return entry;
	}
	
	public synchronized void remove(Object tag) {
		if (entries.remove(tag)) {
			Entry entry = (Entry) tag;
			expose(entry);
		}
	}
	
	public synchronized void clear() {
		ArrayList<Entry> oldEntries = entries;
		entries = new ArrayList<Entry>();
		for(int n = oldEntries.size(); n >= 0; n--) {
			expose(oldEntries.get(n));
		}
	}
	
	private void expose(Entry entry) {
		Measures m = hex.getMeasures();
		int y0 = m.toY(entry.start);
		int y1 = m.toY(entry.end);
		int h = m.getCellHeight();
		int cellWidth = m.getCellWidth();
		if (y0 == y1) {
			int x0 = m.toX(entry.start);
			int x1 = m.toX(entry.end) + cellWidth;
			hex.repaint(x0, y0, x1 - x0, h);
		} else {
			int lineStart = m.getValuesX();
			int lineWidth = m.getValuesWidth();
			hex.repaint(lineStart, y0, lineWidth, y1 - y0 + h);
		}
	}
	
	synchronized void paint(Graphics g, long start, long end) {
		int size = entries.size();
		if (size == 0) return;
		Measures m = hex.getMeasures();
		int lineStart = m.getValuesX();
		int lineWidth = m.getValuesWidth();
		int cellWidth = m.getCellWidth();
		int cellHeight = m.getCellHeight();
		for (Entry e : entries) {
			if (e.start <= end && e.end >= start) {
				int y0 = m.toY(e.start);
				int y1 = m.toY(e.end);
				int x0 = m.toX(e.start);
				int x1 = m.toX(e.end);
				g.setColor(e.color);
				if (y0 == y1) {
					g.fillRect(x0, y0, x1 - x0 + cellWidth, cellHeight);
				} else {
					int midHeight = y1 - (y0 + cellHeight);
					g.fillRect(x0, y0, lineStart + lineWidth - x0, cellHeight);
					if (midHeight > 0) g.fillRect(lineStart, y0 + cellHeight, lineWidth, midHeight);
					g.fillRect(lineStart, y1, x1 + cellWidth - lineStart, cellHeight);
				}
			}
		}
	}
}
