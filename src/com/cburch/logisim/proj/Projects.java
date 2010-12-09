/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.proj;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import com.cburch.logisim.file.Loader;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.util.MacCompatibility;
import com.cburch.logisim.util.PropertyChangeWeakSupport;

public class Projects {
	public static final String projectListProperty = "projectList";
	
	private static final WeakHashMap<Window, Point> frameLocations
		= new WeakHashMap<Window, Point>();
	
	private static void projectRemoved(Project proj, Frame frame,
			MyListener listener) {
		frame.removeWindowListener(listener);
		openProjects.remove(proj);
		proj.getSimulator().shutDown();
		propertySupport.firePropertyChange(projectListProperty, null, null);
	}

	private static class MyListener extends WindowAdapter {
		@Override
		public void windowActivated(WindowEvent event) {
			mostRecentFrame = (Frame) event.getSource();
		}
		
		@Override
		public void windowClosing(WindowEvent event) {
			Frame frame = (Frame) event.getSource();
			if ((frame.getExtendedState() & Frame.ICONIFIED) == 0) {
				mostRecentFrame = frame;
				try {
					frameLocations.put(frame, frame.getLocationOnScreen());
				} catch (Throwable t) { }
			}
		}
		
		@Override
		public void windowClosed(WindowEvent event) {
			Frame frame = (Frame) event.getSource();
			Project proj = frame.getProject();
			
			if (frame == proj.getFrame()) {
				projectRemoved(proj, frame, this);
			}
			if (openProjects.isEmpty() && !MacCompatibility.isSwingUsingScreenMenuBar()) {
				ProjectActions.doQuit();
			}
		}
		
		@Override
		public void windowOpened(WindowEvent event) {
			Frame frame = (Frame) event.getSource();
			Project proj = frame.getProject();

			if (frame == proj.getFrame() && !openProjects.contains(proj)) {
				openProjects.add(proj);
				propertySupport.firePropertyChange(projectListProperty, null, null);
			}
		}
	}

	private static final MyListener myListener = new MyListener();
	private static final PropertyChangeWeakSupport propertySupport
		= new PropertyChangeWeakSupport(Projects.class);
	private static ArrayList<Project> openProjects = new ArrayList<Project>();
	private static Frame mostRecentFrame = null;

	private Projects() { }
	
	public static Frame getTopFrame() {
		Frame ret = mostRecentFrame;
		if (ret == null) {
			Frame backup = null;
			for (Project proj : openProjects) {
				Frame frame = proj.getFrame();
				if (ret == null) ret = frame;
				if (ret.isVisible() && (ret.getExtendedState() & Frame.ICONIFIED) != 0) {
					backup = ret;
				}
			}
			if (ret == null) ret = backup;
		}
		return ret;
	}
	
	static void windowCreated(Project proj, Frame oldFrame, Frame frame) {
		if (oldFrame != null) {
			projectRemoved(proj, oldFrame, myListener);
		}

		if (frame == null) return;
		
		// locate the window
		Point lowest = null;
		for (Project p : openProjects) {
			Frame f = p.getFrame();
			if (f == null) continue;
			Point loc = p.getFrame().getLocation();
			if (lowest == null || loc.y > lowest.y) lowest = loc;
		}
		if (lowest != null) {
			Dimension sz = frame.getToolkit().getScreenSize();
			int x = Math.min(lowest.x + 20, sz.width - 200);
			int y = Math.min(lowest.y + 20, sz.height - 200);
			if (x < 0) x = 0;
			if (y < 0) y = 0;
			frame.setLocation(x, y);
		}

		if (frame.isVisible() && !openProjects.contains(proj)) {
			openProjects.add(proj);
			propertySupport.firePropertyChange(projectListProperty, null, null);
		}
		frame.addWindowListener(myListener);
	}
	
	public static List<Project> getOpenProjects() {
		return Collections.unmodifiableList(openProjects);
	}
	
	public static boolean windowNamed(String name) {
		for (Project proj : openProjects) {
			if (proj.getLogisimFile().getName().equals(name)) return true;
		}
		return false;
	}
	
	public static Project findProjectFor(File query) {
		for (Project proj : openProjects) {
			Loader loader = proj.getLogisimFile().getLoader();
			if (loader == null) continue;
			File f = loader.getMainFile();
			if (query.equals(f)) return proj;
		}
		return null;
	}

	//
	// PropertyChangeSource methods
	//
	public static void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}
	public static void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(propertyName, listener);
	}
	public static void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}
	public static void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
	}
	
	public static Point getLocation(Window win) {
		Point ret = frameLocations.get(win);
		return ret == null ? null : (Point) ret.clone();
	}
}
