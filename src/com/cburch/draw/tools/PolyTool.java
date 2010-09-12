/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.cburch.draw.actions.ModelAddAction;
import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.canvas.CanvasModel;
import com.cburch.draw.canvas.CanvasObject;
import com.cburch.logisim.data.Location;

abstract class PolyTool extends AbstractTool {
	// how close we need to be to the start point to count as "closing the loop"
	private static final int CLOSE_TOLERANCE = 2;
	
	private boolean active;
	private ArrayList<Location> locations;
	private int[] xs;
	private int[] ys;
	private boolean mouseDown;
	private int lastMouseX;
	private int lastMouseY;
	
	public PolyTool() {
		active = false;
		locations = new ArrayList<Location>();
		xs = new int[2];
		ys = new int[2];
	}
	
	@Override
	public abstract Icon getIcon();

	protected abstract CanvasObject createShape(List<Location> locations);

	@Override
	public Cursor getCursor(Canvas canvas) {
		return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
	}
	
	@Override
	public void toolDeselected(Canvas canvas) {
		CanvasObject add = commit(canvas);
		canvas.toolGestureComplete(this, add);
		repaintArea(canvas);
	}
	
	@Override
	public void mousePressed(Canvas canvas, MouseEvent e) {
		int mx = e.getX();
		int my = e.getY();
		lastMouseX = mx;
		lastMouseY = my;
		int mods = e.getModifiersEx();
		if ((mods & InputEvent.CTRL_DOWN_MASK) != 0) {
			mx = canvas.snapX(mx);
			my = canvas.snapY(my);
		}

		if (active && e.getClickCount() > 1) {
			CanvasObject add = commit(canvas);
			canvas.toolGestureComplete(this, add);
			return;
		}

		Location loc = Location.create(mx, my);
		ArrayList<Location> locs = locations;
		if (!active) { locs.clear(); locs.add(loc); }
		locs.add(loc);

		int size = locs.size();
		int[] x = new int[size];
		int[] y = new int[size];
		for(int i = 0; i < size; i++) {
			loc = locations.get(i);
			x[i] = loc.getX();
			y[i] = loc.getY();
		}
		xs = x;
		ys = y;
		mouseDown = true;
		active = canvas.getModel() != null;
		repaintArea(canvas);
	}
	
	@Override
	public void mouseDragged(Canvas canvas, MouseEvent e) {
		updateMouse(canvas, e.getX(), e.getY(), e.getModifiersEx());
	}
	
	@Override
	public void mouseReleased(Canvas canvas, MouseEvent e) {
		if (active) {
			updateMouse(canvas, e.getX(), e.getY(), e.getModifiersEx());
			mouseDown = false;
			int size = locations.size();
			if (size >= 3) {
				Location first = locations.get(0);
				Location last = locations.get(size - 1);
				if (first.manhattanDistanceTo(last) <= CLOSE_TOLERANCE) {
					locations.remove(size - 1);
					CanvasObject add = commit(canvas);
					canvas.toolGestureComplete(this, add);
				}
			}
		}
	}
	
	@Override
	public void keyPressed(Canvas canvas, KeyEvent e) {
		int code = e.getKeyCode();
		if (active && mouseDown
				&& (code == KeyEvent.VK_SHIFT || code == KeyEvent.VK_CONTROL)) {
			updateMouse(canvas, lastMouseX, lastMouseY, e.getModifiersEx());
		}
	}
	
	@Override
	public void keyReleased(Canvas canvas, KeyEvent e) {
		keyPressed(canvas, e);
	}

	@Override
	public void keyTyped(Canvas canvas, KeyEvent e) {
		if (active) {
			char ch = e.getKeyChar();
			if (ch == '\u001b') { // escape key
				active = false;
				locations.clear();
				repaintArea(canvas);
				canvas.toolGestureComplete(this, null);
			} else if (ch == '\n') { // enter key
				CanvasObject add = commit(canvas);
				canvas.toolGestureComplete(this, add);
			}
		}
	}
	
	private CanvasObject commit(Canvas canvas) {
		if (!active) return null;
		CanvasObject add = null;
		active = false;
		ArrayList<Location> locs = locations;
		for(int i = locs.size() - 2; i >= 0; i--) {
			if (locs.get(i).equals(locs.get(i + 1))) locs.remove(i);
		}
		if (locs.size() > 1) {
			CanvasModel model = canvas.getModel();
			add = createShape(locs);
			canvas.doAction(new ModelAddAction(model, add));
			repaintArea(canvas);
		}
		locs.clear();
		return add;
	}
	
	private void updateMouse(Canvas canvas, int mx, int my, int mods) {
		lastMouseX = mx;
		lastMouseY = my;
		if (active) {
			int index = locations.size() - 1;
			Location last = locations.get(index);
			Location newLast;
			if ((mods & MouseEvent.SHIFT_DOWN_MASK) != 0 && index > 0) {
				Location nextLast = locations.get(index - 1);
				newLast = LineTool.snapTo8Cardinals(nextLast, mx, my);
			} else {
				newLast = Location.create(mx, my);
			}
			if ((mods & MouseEvent.CTRL_DOWN_MASK) != 0) {
				int lastX = newLast.getX();
				int lastY = newLast.getY();
				lastX = canvas.snapX(lastX);
				lastY = canvas.snapY(lastY);
				newLast = Location.create(lastX, lastY);
			}
			
			if (!newLast.equals(last)) {
				locations.set(index, newLast);
				xs[index] = newLast.getX();
				ys[index] = newLast.getY();
				repaintArea(canvas);
			}
		}
	}

	private void repaintArea(Canvas canvas) {
		canvas.repaint();
	}
	
	@Override
	public void draw(Canvas canvas, Graphics g) {
		if (active) {
			g.setColor(Color.GRAY);
			g.drawPolyline(xs, ys, xs.length);
			int lastX = xs[xs.length - 1];
			int lastY = ys[ys.length - 1];
			g.fillOval(lastX - 2, lastY - 2, 4, 4);
		}
	}
}
