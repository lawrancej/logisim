/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;

import com.cburch.draw.actions.ModelDeleteHandleAction;
import com.cburch.draw.actions.ModelInsertHandleAction;
import com.cburch.draw.actions.ModelMoveHandleAction;
import com.cburch.draw.actions.ModelRemoveAction;
import com.cburch.draw.actions.ModelTranslateAction;
import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.canvas.CanvasModel;
import com.cburch.draw.canvas.CanvasObject;
import com.cburch.draw.canvas.Selection;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.Icons;

public class SelectTool extends AbstractTool {
	private static final int IDLE = 0;
	private static final int MOVE_ALL = 1;
	private static final int RECT_SELECT = 2;
	private static final int RECT_TOGGLE = 3;
	private static final int MOVE_HANDLE = 4;
	
	private static final int DRAG_TOLERANCE = 2;
	private static final int HANDLE_SIZE = 8;
	
	private int curAction;
	private Location dragStart;
	private Location dragEnd;
	private boolean dragEffective;
	private int lastMouseX;
	private int lastMouseY;
	
	public SelectTool() {
		curAction = IDLE;
		dragStart = Location.create(0, 0);
		dragEnd = dragStart;
		dragEffective = false;
	}
	
	@Override
	public Icon getIcon() {
		return Icons.getIcon("select.gif");
	}

	@Override
	public Cursor getCursor(Canvas canvas) {
		return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	}
	
	@Override
	public List<Attribute<?>> getAttributes() {
		return Collections.emptyList();
	}
	
	@Override
	public void toolSelected(Canvas canvas) {
		canvas.getSelection().clearSelected();
		repaintArea(canvas);
	}
	
	@Override
	public void toolDeselected(Canvas canvas) {
		canvas.getSelection().clearSelected();
		repaintArea(canvas);
	}
	
	private int getHandleSize(Canvas canvas) {
		double zoom = canvas.getZoomFactor();
		return (int) Math.ceil(HANDLE_SIZE / Math.sqrt(zoom));
	}
	
	@Override
	public void mousePressed(Canvas canvas, MouseEvent e) {
		int mx = e.getX();
		int my = e.getY();
		boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
		dragStart = Location.create(mx, my);
		dragEffective = false;
		dragEnd = dragStart;
		lastMouseX = mx;
		lastMouseY = my;
		Selection selection = canvas.getSelection();
		selection.setHandleSelected(null, null);
		
		// see whether user is pressing within an existing handle
		int halfSize = getHandleSize(canvas) / 2;
		CanvasObject clicked = null;
		for (CanvasObject shape : selection.getSelected()) {
			List<Location> handles = shape.getHandles(null, 0, 0);
			for (Location loc : handles) {
				int dx = loc.getX() - mx;
				int dy = loc.getY() - my;
				if (dx >= -halfSize && dx <= halfSize
						&& dy >= -halfSize && dy <= halfSize) {
					if (shape.canMoveHandle(loc)) {
						curAction = MOVE_HANDLE;
						selection.setHandleSelected(shape, loc);
						repaintArea(canvas);
						return;
					} else if (clicked == null) {
						clicked = shape;
					}
				}
			}
		}

		// see whether the user is clicking within a shape
		if (clicked == null) {
			clicked = canvas.getModel().getObjectAt(e.getX(), e.getY());
		}
		if (clicked != null) {
			if (shift && selection.isSelected(clicked)) {
				selection.setSelected(clicked, false);
				curAction = IDLE;
			} else {
				if (!shift && !selection.isSelected(clicked)) {
					selection.clearSelected();
				}
				selection.setSelected(clicked, true);
				selection.setMovingShapes(selection.getSelected(), 0, 0);
				curAction = MOVE_ALL;
			}
			repaintArea(canvas);
			return;
		}
		
		if (shift) {
			curAction = RECT_TOGGLE;
		} else {
			selection.clearSelected();
			curAction = RECT_SELECT;
		}
		repaintArea(canvas);
	}
	
	@Override
	public void mouseDragged(Canvas canvas, MouseEvent e) {
		setMouse(canvas, e.getX(), e.getY(), e.getModifiersEx());
	}
	
	@Override
	public void mouseReleased(Canvas canvas, MouseEvent e) {
		setMouse(canvas, e.getX(), e.getY(), e.getModifiersEx());
		
		CanvasModel model = canvas.getModel();
		Selection selection = canvas.getSelection();
		Set<CanvasObject> selected = selection.getSelected();
		int action = curAction;
		curAction = IDLE;
		
		Location start = dragStart;
		int x1 = e.getX();
		int y1 = e.getY();
		switch(action) {
		case MOVE_ALL:
			Location delta = selection.getMovingDelta();
			int dx = delta.getX();
			int dy = delta.getY();
			if (dragEffective) {
				canvas.doAction(new ModelTranslateAction(model, selected, dx, dy));
			}
			break;
		case MOVE_HANDLE:
			delta = selection.getHandleDelta();
			dx = delta.getX();
			dy = delta.getY();
			CanvasObject hanShape = selection.getHandleShape();
			Location handle = selection.getHandleLocation();
			if (dragEffective && hanShape != null) {
				canvas.doAction(new ModelMoveHandleAction(model, hanShape,
						handle, dx, dy));
			}
			break;
		case RECT_SELECT:
			Bounds bds = Bounds.create(start).add(x1, y1);
			selection.setSelected(canvas.getModel().getObjectsIn(bds), true);
			break;
		case RECT_TOGGLE:
			bds = Bounds.create(start).add(x1, y1);
			selection.toggleSelected(canvas.getModel().getObjectsIn(bds));
			break;
		}
		selection.clearDrawsSuppressed();
		repaintArea(canvas);
	}
	
	@Override
	public void keyPressed(Canvas canvas, KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.VK_SHIFT:
		case KeyEvent.VK_CONTROL:
			if (curAction != IDLE) {
				setMouse(canvas, lastMouseX, lastMouseY, e.getModifiersEx());
			}
			break;
		case KeyEvent.VK_INSERT:
			Selection selection = canvas.getSelection();
			CanvasObject shape = selection.getHandleShape();
			Location handle = selection.getHandleLocation();
			if (shape != null && shape.canInsertHandle(handle)) {
				CanvasModel model = canvas.getModel();
				canvas.doAction(new ModelInsertHandleAction(model, shape, handle));
				repaintArea(canvas);
				e.consume();
			}
			break;
		case KeyEvent.VK_DELETE:
			selection = canvas.getSelection();
			shape = selection.getHandleShape();
			handle = selection.getHandleLocation();
			if (shape != null && shape.canDeleteHandle(handle)) {
				CanvasModel model = canvas.getModel();
				canvas.doAction(new ModelDeleteHandleAction(model, shape, handle));
				repaintArea(canvas);
				e.consume();
			}
			break;
		}
	}
	
	@Override
	public void keyReleased(Canvas canvas, KeyEvent e) {
		int code = e.getKeyCode();
		if ((code == KeyEvent.VK_SHIFT || code == KeyEvent.VK_CONTROL)
				&& curAction != IDLE) {
			setMouse(canvas, lastMouseX, lastMouseY, e.getModifiersEx());
		}
	}
	
	@Override
	public void keyTyped(Canvas canvas, KeyEvent e) {
		char ch = e.getKeyChar();
		Selection selected = canvas.getSelection();
		if ((ch == '\u0008' || ch == '\u007F') && !selected.isEmpty()) {
			ArrayList<CanvasObject> toRemove = new ArrayList<CanvasObject>();
			for (CanvasObject shape : selected.getSelected()) {
				if (shape.canRemove()) {
					toRemove.add(shape);
				}
			}
			if (!toRemove.isEmpty()) {
				e.consume();
				CanvasModel model = canvas.getModel();
				canvas.doAction(new ModelRemoveAction(model, toRemove));
				selected.clearSelected();
				repaintArea(canvas);
			}
		} else if (ch == '\u001b' && !selected.isEmpty()) {
			selected.clearSelected();
			repaintArea(canvas);
		}
	}
	
	
	private void setMouse(Canvas canvas, int mx, int my, int mods) {
		lastMouseX = mx;
		lastMouseY = my;
		boolean shift = (mods & MouseEvent.SHIFT_DOWN_MASK) != 0
			&& curAction == MOVE_ALL;
		boolean ctrl = (mods & InputEvent.CTRL_DOWN_MASK) != 0;
		Location newEnd = shift ? LineTool.snapTo4Cardinals(dragStart, mx, my)
				: Location.create(mx, my);
		dragEnd = newEnd;

		Location start = dragStart;
		int dx = newEnd.getX() - start.getX();
		int dy = newEnd.getY() - start.getY();
		if (Math.abs(dx) + Math.abs(dy) > DRAG_TOLERANCE) {
			dragEffective = true;
		}

		switch(curAction) {
		case MOVE_HANDLE:
			if (ctrl) {
				Location handle = canvas.getSelection().getHandleLocation();
				dx = canvas.snapX(handle.getX() + dx) - handle.getX();
				dy = canvas.snapY(handle.getY() + dy) - handle.getY();
			}
			canvas.getSelection().setHandleDelta(dx, dy);
			break;
		case MOVE_ALL:
			if (ctrl) {
				int minX = Integer.MAX_VALUE;
				int minY = Integer.MAX_VALUE;
				for (CanvasObject o : canvas.getSelection().getSelected()) {
					Bounds b = o.getBounds();
					int x = b.getX();
					int y = b.getY();
					if (x < minX) minX = x;
					if (y < minY) minY = y;
				}
				dx = canvas.snapX(minX + dx) - minX;
				dy = canvas.snapY(minY + dy) - minY;
			}
			canvas.getSelection().setMovingDelta(dx, dy);
			break;
		}
		repaintArea(canvas);
	}

	private void repaintArea(Canvas canvas) {
		canvas.repaint();
	}
	
	@Override
	public void draw(Canvas canvas, Graphics g) {
		Selection selection = canvas.getSelection();
		int action = curAction;

		Location start = dragStart;
		Location end = dragEnd;
		int dx;
		int dy;
		boolean drawHandles;
		switch (action) {
		case MOVE_ALL:
			drawHandles = false;
			Location delta = selection.getMovingDelta();
			dx = delta.getX();
			dy = delta.getY();
			break;
		case MOVE_HANDLE:
			drawHandles = false;
			delta = selection.getHandleDelta();
			dx = delta.getX();
			dy = delta.getY();
			break;
		default:
			drawHandles = true;
			dx = end.getX() - start.getX();
			dy = end.getY() - start.getY();
		}
		if (!dragEffective) { dx = 0; dy = 0; }

		CanvasObject hanShape = selection.getHandleShape();
		Location handle = selection.getHandleLocation();
		if (drawHandles) {
			// unscale the coordinate system so that the stroke width isn't scaled
			double zoom = 1.0;
			Graphics gCopy = g.create();
			if (gCopy instanceof Graphics2D) {
				zoom = canvas.getZoomFactor();
				if (zoom != 1.0) {
					((Graphics2D) gCopy).scale(1.0 / zoom, 1.0 / zoom);
				}
			}
			GraphicsUtil.switchToWidth(gCopy, 1);

			int size = (int) Math.ceil(HANDLE_SIZE * Math.sqrt(zoom));
			int offs = size / 2;
			for (CanvasObject d : selection.getSelected()) {
				List<Location> handles;
				if (action == MOVE_HANDLE && d == hanShape) {
					handles = d.getHandles(handle, dx, dy);
				} else {
					handles = d.getHandles(null, 0, 0);
				}
				for (Location loc : handles) {
					int x = loc.getX();
					int y = loc.getY();
					if (action == MOVE_ALL) { x += dx; y += dy; }
					x = (int) (zoom * loc.getX());
					y = (int) (zoom * loc.getY());
					if (d == hanShape && loc.equals(handle)) {
						gCopy.fillRect(x - offs, y - offs, size + 1, size + 1);
					} else {
						gCopy.clearRect(x - offs, y - offs, size, size);
						gCopy.drawRect(x - offs, y - offs, size, size);
					}
				}
			}
		}
		
		switch(action) {
		case RECT_SELECT:
		case RECT_TOGGLE:
			int x0 = start.getX();
			int y0 = start.getY();
			int x1 = end.getX();
			int y1 = end.getY();
			g.setColor(Color.gray);
			if (x1 < x0) { int t = x0; x0 = x1; x1 = t; }
			if (y1 < y0) { int t = y0; y0 = y1; y1 = t; }
			g.drawRect(x0, y0, x1 - x0, y1 - y0);
			break;
		}
	}
}
