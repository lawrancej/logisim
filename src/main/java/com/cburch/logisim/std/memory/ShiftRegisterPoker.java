/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.StdAttr;

public class ShiftRegisterPoker extends InstancePoker {
	private int loc;
	
	@Override
	public boolean init(InstanceState state, MouseEvent e) {
		loc = computeStage(state, e);
		return loc >= 0;
	}
	
	private int computeStage(InstanceState state, MouseEvent e) {
		Integer lenObj = state.getAttributeValue(ShiftRegister.ATTR_LENGTH);
		BitWidth widObj = state.getAttributeValue(StdAttr.WIDTH);
		Boolean loadObj = state.getAttributeValue(ShiftRegister.ATTR_LOAD);
		Bounds bds = state.getInstance().getBounds();

		int y = bds.getY();
		String label = state.getAttributeValue(StdAttr.LABEL);
		if (label == null || label.equals("")) y += bds.getHeight() / 2;
		else y += 3 * bds.getHeight() / 4;
		y = e.getY() - y;
		if (y <= -6 || y >= 8) return -1;
		
		int x = e.getX() - (bds.getX() + 15);
		if (!loadObj.booleanValue() || widObj.getWidth() > 4) return -1;
		if (x < 0 || x >= lenObj.intValue() * 10) return -1;
		return x / 10;
	}

	@Override
	public void paint(InstancePainter painter) {
		int loc = this.loc;
		if (loc < 0) return;
		Bounds bds = painter.getInstance().getBounds();
		int x = bds.getX() + 15 + loc * 10;
		int y = bds.getY();
		String label = painter.getAttributeValue(StdAttr.LABEL);
		if (label == null || label.equals("")) y += bds.getHeight() / 2;
		else y += 3 * bds.getHeight() / 4;
		Graphics g = painter.getGraphics();
		g.setColor(Color.RED);
		g.drawRect(x, y - 6, 10, 13);
	}
	
	@Override
	public void mousePressed(InstanceState state, MouseEvent e) {
		loc = computeStage(state, e);
	}
	
	@Override
	public void mouseReleased(InstanceState state, MouseEvent e) {
		int oldLoc = loc;
		if (oldLoc < 0) return;
		BitWidth widObj = state.getAttributeValue(StdAttr.WIDTH);
		if (widObj.equals(BitWidth.ONE)) {
			int newLoc = computeStage(state, e);
			if (oldLoc == newLoc) {
				ShiftRegisterData data = (ShiftRegisterData) state.getData();
				int i = data.getLength() - 1 - loc;
				Value v = data.get(i);
				if (v == Value.FALSE) v = Value.TRUE;
				else v = Value.FALSE;
				data.set(i, v);
				state.fireInvalidated();
			}
		}
	}
	
	@Override
	public void keyTyped(InstanceState state, KeyEvent e) {
		int loc = this.loc;
		if (loc < 0) return;
		char c = e.getKeyChar();
		if (c == ' ') {
			Integer lenObj = state.getAttributeValue(ShiftRegister.ATTR_LENGTH);
			if (loc < lenObj.intValue() - 1) {
				this.loc = loc + 1;
				state.fireInvalidated();
			}
		} else if (c == '\u0008') {
			if (loc > 0) {
				this.loc = loc - 1;
				state.fireInvalidated();
			}
		} else {
			try {
				int val = Integer.parseInt("" + e.getKeyChar(), 16);
				BitWidth widObj = state.getAttributeValue(StdAttr.WIDTH);
				if ((val & ~widObj.getMask()) != 0) return;
				Value valObj = Value.createKnown(widObj, val);
				ShiftRegisterData data = (ShiftRegisterData) state.getData();
				int i = data.getLength() - 1 - loc;
				if (!data.get(i).equals(valObj)) {
					data.set(i, valObj);
					state.fireInvalidated();
				}
			} catch (NumberFormatException ex) {
				return;
			}
		}
	}
}
