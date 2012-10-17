/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JViewport;
import javax.swing.Popup;
import javax.swing.PopupFactory;

import com.cburch.draw.canvas.SelectionEvent;
import com.cburch.draw.canvas.SelectionListener;
import com.cburch.draw.model.CanvasObject;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.appear.AppearancePort;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.gui.generic.CanvasPane;
import com.cburch.logisim.instance.Instance;

class LayoutPopupManager implements SelectionListener, MouseListener, MouseMotionListener {
	private CanvasPane canvasPane;
	private AppearanceCanvas canvas;
	private Popup curPopup;
	private long curPopupTime;
	private Location dragStart;
	
	public LayoutPopupManager(CanvasPane canvasPane, AppearanceCanvas canvas) {
		this.canvasPane = canvasPane;
		this.canvas = canvas;
		this.curPopup = null;
		this.dragStart = null;
		
		canvas.getSelection().addSelectionListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
	}
	
	public void hideCurrentPopup() {
		Popup cur = curPopup;
		if (cur != null) {
			curPopup = null;
			dragStart = null;
			cur.hide();
		}
	}

	public void selectionChanged(SelectionEvent e) {
		int act = e.getAction();
		if (act == SelectionEvent.ACTION_ADDED) {
			Set<AppearancePort> ports = shouldShowPopup(e.getAffected());
			if (ports == null) {
				hideCurrentPopup();
			} else {
				showPopup(ports);
			}
		}
	}
	
	private Set<AppearancePort> shouldShowPopup(Collection<CanvasObject> add) {
		boolean found = false;
		for (CanvasObject o : add) {
			if (o instanceof AppearancePort) {
				found = true;
				break;
			}
		}
		if (found) {
			Set<AppearancePort> ports = getSelectedPorts();
			if (!ports.isEmpty() && isPortUnselected(ports)) {
				return ports;
			}
		}
		return null;
	}
	
	// returns all the ports in the current selection
	private Set<AppearancePort> getSelectedPorts() {
		HashSet<AppearancePort> ports = new HashSet<AppearancePort>();
		for (CanvasObject o : canvas.getSelection().getSelected()) {
			if (o instanceof AppearancePort) {
				ports.add((AppearancePort) o);
			}
		}
		return ports;
	}
	
	// returns true if the canvas contains any port not in the given set
	private boolean isPortUnselected(Set<AppearancePort> selected) {
		for (CanvasObject o : canvas.getModel().getObjectsFromBottom()) {
			if (o instanceof AppearancePort) {
				if (!selected.contains(o)) {
					return true;
				}
			}
		}
		return false;
	}

	private void showPopup(Set<AppearancePort> portObjects) {
		dragStart = null;
		CircuitState circuitState = canvas.getCircuitState();
		if (circuitState == null) return;
		ArrayList<Instance> ports = new ArrayList<Instance>(portObjects.size());
		for (AppearancePort portObject : portObjects) {
			ports.add(portObject.getPin());
		}
		
		hideCurrentPopup();
		LayoutThumbnail layout = new LayoutThumbnail();
		layout.setCircuit(circuitState, ports);
		JViewport owner = canvasPane.getViewport();
		Point ownerLoc = owner.getLocationOnScreen();
		Dimension ownerDim = owner.getSize();
		Dimension layoutDim = layout.getPreferredSize();
		int x = ownerLoc.x + Math.max(0, ownerDim.width - layoutDim.width - 5);
		int y = ownerLoc.y + Math.max(0, ownerDim.height - layoutDim.height - 5);
		PopupFactory factory = PopupFactory.getSharedInstance();
		Popup popup = factory.getPopup(canvasPane.getViewport(), layout, x, y);
		popup.show();
		curPopup = popup;
		curPopupTime = System.currentTimeMillis();
	}

	public void mouseClicked(MouseEvent e) { }

	public void mouseEntered(MouseEvent e) {
		hideCurrentPopup();
	}

	public void mouseExited(MouseEvent e) {
		long sincePopup = System.currentTimeMillis() - curPopupTime;
		if (sincePopup > 50) hideCurrentPopup();
	}

	public void mousePressed(MouseEvent e) {
		long sincePopup = System.currentTimeMillis() - curPopupTime;
		if (sincePopup > 50) hideCurrentPopup();
		dragStart = Location.create(e.getX(), e.getY());
	}

	public void mouseReleased(MouseEvent e) { }

	public void mouseDragged(MouseEvent e) {
		Location start = dragStart;
		if (start != null && start.manhattanDistanceTo(e.getX(), e.getY()) > 4) {
			hideCurrentPopup();
		}
	}

	public void mouseMoved(MouseEvent arg0) { }
	

}
