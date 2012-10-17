/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;

public class SubcircuitPoker extends InstancePoker {
	
	private static final Color MAGNIFYING_INTERIOR = new Color(200, 200, 255, 64);
	private static final Color MAGNIFYING_INTERIOR_DOWN = new Color(128, 128, 255, 192);
	
	private boolean mouseDown;

	@Override
	public Bounds getBounds(InstancePainter painter) {
		Bounds bds = painter.getInstance().getBounds();
		int cx = bds.getX() + bds.getWidth() / 2;
		int cy = bds.getY() + bds.getHeight() / 2;
		return Bounds.create(cx - 5, cy - 5, 15, 15);
	}
	
	@Override
	public void paint(InstancePainter painter) {
		if (painter.getDestination() instanceof Canvas
				&& painter.getData() instanceof CircuitState) {
			Bounds bds = painter.getInstance().getBounds();
			int cx = bds.getX() + bds.getWidth() / 2;
			int cy = bds.getY() + bds.getHeight() / 2;
	
			int tx = cx + 3;
			int ty = cy + 3;
			int[] xp = { tx - 1, cx + 8, cx + 10, tx + 1 };
			int[] yp = { ty + 1, cy + 10, cy + 8, ty - 1 };
			Graphics g = painter.getGraphics();
			if (mouseDown) {
				g.setColor(MAGNIFYING_INTERIOR_DOWN);
			} else {
				g.setColor(MAGNIFYING_INTERIOR);
			}
			g.fillOval(cx - 5, cy - 5, 10, 10);
			g.setColor(Color.BLACK);
			g.drawOval(cx - 5, cy - 5, 10, 10);
			g.fillPolygon(xp, yp, xp.length);
		}
	}
	
	@Override
	public void mousePressed(InstanceState state, MouseEvent e) {
		if (isWithin(state, e)) {
			mouseDown = true;
			state.getInstance().fireInvalidated();
		}
	}
	
	@Override
	public void mouseReleased(InstanceState state, MouseEvent e) {
		if (mouseDown) {
			mouseDown = false;
			Object sub = state.getData();
			if (e.getClickCount() == 2 && isWithin(state, e)
					&& sub instanceof CircuitState) {
				state.getProject().setCircuitState((CircuitState) sub);
			} else {
				state.getInstance().fireInvalidated();
			}
		}
	}
	
	private boolean isWithin(InstanceState state, MouseEvent e) {
		Bounds bds = state.getInstance().getBounds();
		int cx = bds.getX() + bds.getWidth() / 2;
		int cy = bds.getY() + bds.getHeight() / 2;
		int dx = e.getX() - cx;
		int dy = e.getY() - cy;
		return dx * dx + dy * dy <= 60;
	}
}
