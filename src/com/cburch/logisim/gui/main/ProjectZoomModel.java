/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.gui.generic.ZoomModel;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;

class ProjectZoomModel implements ZoomModel, ProjectListener, AttributeListener {
	private static final double[] ZOOM_OPTIONS = { 20, 50, 75, 100, 133, 150, 200 };

	private Project proj;
	private PropertyChangeSupport support;
	private double zoomFactor;
	private boolean showGrid;
	
	public ProjectZoomModel(Project proj) {
		this.proj = proj;
		support = new PropertyChangeSupport(this);
		zoomFactor = 1.0;
		showGrid = true;
		proj.addProjectListener(this);
		loadOptions(proj.getLogisimFile());
	}
	
	private void loadOptions(LogisimFile file) {
		AttributeSet attrs = file.getOptions().getAttributeSet();
		setZoomFactor(attrs.getValue(Options.zoom_attr).doubleValue());
		setShowGrid(attrs.getValue(Options.showgrid_attr).booleanValue());
		attrs.addAttributeListener(this);
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
			AttributeSet attrs = proj.getOptions().getAttributeSet();
			attrs.setValue(Options.showgrid_attr, Boolean.valueOf(value));
			support.firePropertyChange(ZoomModel.SHOW_GRID, !value, value);
		}
	}

	public void setZoomFactor(double value) {
		double oldValue = zoomFactor;
		if (value != oldValue) {
			zoomFactor = value;
			AttributeSet attrs = proj.getOptions().getAttributeSet();
			attrs.setValue(Options.zoom_attr, Double.valueOf(value));
			support.firePropertyChange(ZoomModel.ZOOM, oldValue, value);
		}
	}

	public void projectChanged(ProjectEvent event) {
		if (event.getAction() == ProjectEvent.ACTION_SET_FILE) {
			Object oldFile = event.getOldData();
			if (oldFile instanceof LogisimFile) {
				Options opts = ((LogisimFile) oldFile).getOptions();
				opts.getAttributeSet().removeAttributeListener(this);
			}
			Object newFile = event.getData();
			if (newFile instanceof LogisimFile) {
				loadOptions((LogisimFile) newFile);
			}
		}
	}

	public void attributeListChanged(AttributeEvent e) {}

	public void attributeValueChanged(AttributeEvent e) {
		Object attr = e.getAttribute();
		Object val = e.getValue();
		if (attr == Options.zoom_attr) {
			setZoomFactor(((Double) val).doubleValue());
		} else if (attr == Options.showgrid_attr) {
			setShowGrid(((Boolean) val).booleanValue());
		}
	}
}
