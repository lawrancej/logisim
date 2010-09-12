/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.appear;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.cburch.logisim.gui.generic.ZoomModel;

class AppearanceZoomModel implements ZoomModel {
	private static final double[] ZOOM_OPTIONS = { 100, 150, 200, 300, 400, 600, 800 };

	private PropertyChangeSupport support;
	private double zoomFactor;
	private boolean showGrid;
	
	public AppearanceZoomModel() {
		support = new PropertyChangeSupport(this);
		zoomFactor = 2.0;
		showGrid = true;
	}

	public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
		support.addPropertyChangeListener(prop, l);
	}

	public void removePropertyChangeListener(String prop,
			PropertyChangeListener l) {
		support.removePropertyChangeListener(prop, l);
	}

	public boolean getShowGrid() {
		return showGrid;
	}

	public double getZoomFactor() {
		return zoomFactor;
	}

	public double[] getZoomOptions() {
		return ZOOM_OPTIONS;
	}

	public void setShowGrid(boolean value) {
		if (value != showGrid) {
			showGrid = value;
			support.firePropertyChange(ZoomModel.SHOW_GRID, !value, value);
		}
	}

	public void setZoomFactor(double value) {
		double oldValue = zoomFactor;
		if (value != oldValue) {
			zoomFactor = value;
			support.firePropertyChange(ZoomModel.ZOOM, oldValue, value);
		}
	}

}
