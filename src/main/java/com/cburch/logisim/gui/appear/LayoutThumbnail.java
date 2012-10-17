/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JComponent;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.appear.AppearancePort;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.util.GraphicsUtil;

public class LayoutThumbnail extends JComponent {
	private static final int BORDER = 10;
	
	private CircuitState circuitState;
	private Collection<Instance> ports;
	
	public LayoutThumbnail() {
		circuitState = null;
		ports = null;
		setBackground(Color.LIGHT_GRAY);
		setPreferredSize(new Dimension(200, 200));
	}
	
	public void setCircuit(CircuitState circuitState,
			Collection<Instance> ports) {
		this.circuitState = circuitState;
		this.ports = ports;
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		if (circuitState != null) {
			Circuit circuit = circuitState.getCircuit();
			Bounds bds = circuit.getBounds(g);
			Dimension size = getSize();
			double scaleX = (double) (size.width - 2 * BORDER) / bds.getWidth();
			double scaleY = (double) (size.height - 2 * BORDER) / bds.getHeight();
			double scale = Math.min(1.0, Math.min(scaleX, scaleY));
			
			Graphics gCopy = g.create();
			int borderX = (int) ((size.width - bds.getWidth() * scale) / 2);
			int borderY = (int) ((size.height - bds.getHeight() * scale) / 2);
			gCopy.translate(borderX, borderY);
			if (scale != 1.0 && g instanceof Graphics2D) {
				((Graphics2D) gCopy).scale(scale, scale);
			}
			gCopy.translate(-bds.getX(), -bds.getY());
			
			ComponentDrawContext context = new ComponentDrawContext(this, circuit,
					circuitState, g, gCopy);
			context.setShowState(false);
			context.setShowColor(false);
			circuit.draw(context, Collections.<Component>emptySet());
			if (ports != null) {
				gCopy.setColor(AppearancePort.COLOR);
				int width = Math.max(4, (int) ((2 / scale) + 0.5));
				GraphicsUtil.switchToWidth(gCopy, width);
				for (Instance port : ports) {
					Bounds b = port.getBounds();
					int x = b.getX();
					int y = b.getY();
					int w = b.getWidth();
					int h = b.getHeight();
					if (Pin.FACTORY.isInputPin(port)) {
						gCopy.drawRect(x, y, w, h);
					} else {
						if (b.getWidth() > 25) {
							gCopy.drawRoundRect(x, y, w, h, 4, 4);
						} else {
							gCopy.drawOval(x, y, w, h);
						}
					}
				}
			}
			gCopy.dispose();
			
			g.setColor(Color.BLACK);
			GraphicsUtil.switchToWidth(g, 2);
			g.drawRect(0, 0, size.width - 2, size.height - 2);
		}
	}

}
