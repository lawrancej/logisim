/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.hex.HexModel;
import com.cburch.hex.HexModelListener;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringUtil;

class MemState implements InstanceData, Cloneable, HexModelListener {
	private static final int ROWS = 4; // rows in memory display

	private static final int TABLE_WIDTH12 = 80; // width of table for addr bits <= 12
	private static final int TABLE_WIDTH32 = 65; // width of table for addr bits > 12

	private static final int ENTRY_HEIGHT = 15; // pixels high per entry

	private static final int ENTRY_XOFFS12 = 40; // x offset for entries for addr bits <= 12
	private static final int ENTRY_XOFFS32 = 60; // x offset for entries for addr bits > 12

	private static final int ENTRY_YOFFS = 5; // y offset for entries

	private static final int ADDR_WIDTH_PER_CHAR = 10; // pixels wide per address character

	private MemContents contents;
	private int columns;
	private long curScroll = 0;
	private long cursorLoc = -1;
	private long curAddr = -1;

	MemState(MemContents contents) {
		this.contents = contents;
		setBits(contents.getLogLength(), contents.getWidth());
		contents.addHexModelListener(this);
	}
	
	@Override
	public MemState clone() {
		try {
			MemState ret = (MemState) super.clone();
			ret.contents = contents.clone();
			ret.contents.addHexModelListener(ret);
			return ret;
		} catch (CloneNotSupportedException e) { return null; }
	}
	
	//
	// methods for accessing the address bits
	//
	private void setBits(int addrBits, int dataBits) {
		if (contents == null) {
			contents = MemContents.create(addrBits, dataBits);
		} else {
			contents.setDimensions(addrBits, dataBits);
		}
		if (addrBits <= 12) {
			if (dataBits <= 8) {
				columns = dataBits <= 4 ? 8 : 4;
			} else {
				columns = dataBits <= 16 ? 2 : 1;
			}
		} else {
			columns = dataBits <= 8 ? 2 : 1;
		}
		long newLast = contents.getLastOffset();
		// I do subtraction in the next two conditions to account for possibility of overflow
		if (cursorLoc > newLast) cursorLoc = newLast;
		if (curAddr - newLast > 0) curAddr = -1;
		long maxScroll = Math.max(0, newLast + 1 - (ROWS - 1) * columns);
		if (curScroll > maxScroll) curScroll = maxScroll;
	}
	
	public MemContents getContents() {
		return contents;
	}

	//
	// methods for accessing data within memory
	//
	int getAddrBits() {
		return contents.getLogLength();
	}
	
	int getDataBits() {
		return contents.getWidth();
	}
	
	long getLastAddress() {
		return (1L << contents.getLogLength()) - 1;
	}
	
	boolean isValidAddr(long addr) {
		int addrBits = contents.getLogLength();
		return addr >>> addrBits == 0;
	}
	
	int getRows() {
		return ROWS;
	}
	
	int getColumns() {
		return columns;
	}
	
	//
	// methods for manipulating cursor and scroll location
	//
	long getCursor() {
		return cursorLoc;
	}
	
	long getCurrent() {
		return curAddr;
	}
	
	long getScroll() {
		return curScroll;
	}
	
	void setCursor(long value) {
		cursorLoc = isValidAddr(value) ? value : -1L;
	}
	
	void setCurrent(long value) {
		curAddr = isValidAddr(value) ? value : -1L;
	}

	void scrollToShow(long addr) {
		if (isValidAddr(addr)) {
			addr = addr / columns * columns;
			long curTop = curScroll / columns * columns;
			if (addr < curTop) {
				curScroll = addr;
			} else if (addr >= curTop + ROWS * columns) {
				curScroll = addr - (ROWS - 1) * columns;
				if (curScroll < 0) curScroll = 0;
			}
		}
	}

	void setScroll(long addr) {
		long maxAddr = getLastAddress() - ROWS * columns;
		if (addr > maxAddr) addr = maxAddr; // note: maxAddr could be negative
		if (addr < 0) addr = 0;
		curScroll = addr;
	}
	
	//
	// graphical methods
	//
	public long getAddressAt(int x, int y) {
		int addrBits = getAddrBits();
		int boxX = addrBits <= 12 ? ENTRY_XOFFS12 : ENTRY_XOFFS32;
		int boxW = addrBits <= 12 ? TABLE_WIDTH12 : TABLE_WIDTH32;
		
		// See if outside box
		if (x < boxX || x >= boxX + boxW || y <= ENTRY_YOFFS
				|| y >= ENTRY_YOFFS + ROWS * ENTRY_HEIGHT) {
			return -1;
		}
	
		int col = (x - boxX) / (boxW / columns);
		int row = (y - ENTRY_YOFFS) / ENTRY_HEIGHT;
		long ret = (curScroll / columns * columns) + columns * row + col;
		return isValidAddr(ret) ? ret : getLastAddress();
	}
	
	public Bounds getBounds(long addr, Bounds bds) {
		int addrBits = getAddrBits();
		int boxX = bds.getX() + (addrBits <= 12 ? ENTRY_XOFFS12 : ENTRY_XOFFS32);
		int boxW = addrBits <= 12 ? TABLE_WIDTH12 : TABLE_WIDTH32;
		if (addr < 0) {
			int addrLen = (contents.getWidth() + 3) / 4;
			int width = ADDR_WIDTH_PER_CHAR * addrLen;
			return Bounds.create(boxX - width, bds.getY() + ENTRY_YOFFS,
					width, ENTRY_HEIGHT);
		} else {
			int bdsX = addrToX(bds, addr);
			int bdsY = addrToY(bds, addr);
			return Bounds.create(bdsX, bdsY, boxW / columns, ENTRY_HEIGHT);
		}
	}

	public void paint(Graphics g, int leftX, int topY) {
		int addrBits = getAddrBits();
		int dataBits = contents.getWidth();
		int boxX = leftX + (addrBits <= 12 ? ENTRY_XOFFS12 : ENTRY_XOFFS32);
		int boxY = topY + ENTRY_YOFFS;
		int boxW = addrBits <= 12 ? TABLE_WIDTH12 : TABLE_WIDTH32;
		int boxH = ROWS * ENTRY_HEIGHT;
		
		GraphicsUtil.switchToWidth(g, 1);
		g.drawRect(boxX, boxY, boxW, boxH);
		int entryWidth = boxW / columns;
		for (int row = 0; row < ROWS; row++) {
			long addr = (curScroll / columns * columns) + columns * row;
			int x = boxX;
			int y = boxY + ENTRY_HEIGHT * row;
			int yoffs = ENTRY_HEIGHT - 3;
			if (isValidAddr(addr)) {
				g.setColor(Color.GRAY);
				GraphicsUtil.drawText(g, StringUtil.toHexString(getAddrBits(), (int) addr),
						x - 2, y + yoffs,
						GraphicsUtil.H_RIGHT, GraphicsUtil.V_BASELINE);
			}
			g.setColor(Color.BLACK);
			for (int col = 0; col < columns && isValidAddr(addr); col++) {
				int val = contents.get(addr);
				if (addr == curAddr) {
					g.fillRect(x, y, entryWidth, ENTRY_HEIGHT);
					g.setColor(Color.WHITE);
					GraphicsUtil.drawText(g, StringUtil.toHexString(dataBits, val),
							x + entryWidth / 2, y + yoffs,
							GraphicsUtil.H_CENTER, GraphicsUtil.V_BASELINE);
					g.setColor(Color.BLACK);
				} else {
					GraphicsUtil.drawText(g, StringUtil.toHexString(dataBits, val),
							x + entryWidth / 2, y + yoffs,
							GraphicsUtil.H_CENTER, GraphicsUtil.V_BASELINE);
				}
				addr++;
				x += entryWidth;
			}
		}
	}

	private int addrToX(Bounds bds, long addr) {
		int addrBits = getAddrBits();
		int boxX = bds.getX() + (addrBits <= 12 ? ENTRY_XOFFS12 : ENTRY_XOFFS32);
		int boxW = addrBits <= 12 ? TABLE_WIDTH12 : TABLE_WIDTH32;

		long topRow = curScroll / columns;
		long row = addr / columns;
		if (row < topRow || row >= topRow + ROWS) return -1;
		int col = (int) (addr - row * columns);
		if (col < 0 || col >= columns) return -1;
		return boxX + boxW * col / columns;
	}

	private int addrToY(Bounds bds, long addr) {
		long topRow = curScroll / columns;
		long row = addr / columns;
		if (row < topRow || row >= topRow + ROWS) return -1;
		return (int) (bds.getY() + ENTRY_YOFFS + ENTRY_HEIGHT * (row - topRow));
	}

	public void metainfoChanged(HexModel source) {
		setBits(contents.getLogLength(), contents.getWidth());
	}

	public void bytesChanged(HexModel source, long start, long numBytes, int[] oldValues) { }
}
