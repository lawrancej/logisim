/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.Projects;

class ProjectsDirty {
	private ProjectsDirty() { }
	
	private static class DirtyListener implements LibraryListener {
		Project proj;
		
		DirtyListener(Project proj) {
			this.proj = proj;
		}
		
		public void libraryChanged(LibraryEvent event) {
			if (event.getAction() == LibraryEvent.DIRTY_STATE) {
				LogisimFile lib = proj.getLogisimFile();
				File file = lib.getLoader().getMainFile();
				LibraryManager.instance.setDirty(file, lib.isDirty());
			}
		}
	}
	
	private static class ProjectListListener implements PropertyChangeListener {
		public synchronized void propertyChange(PropertyChangeEvent event) {
			for (DirtyListener l : listeners) {
				l.proj.removeLibraryListener(l);
			}
			listeners.clear();
			for (Project proj : Projects.getOpenProjects()) {
				DirtyListener l = new DirtyListener(proj);
				proj.addLibraryListener(l);
				listeners.add(l);
				
				LogisimFile lib = proj.getLogisimFile();
				LibraryManager.instance.setDirty(lib.getLoader().getMainFile(), lib.isDirty());
			}
		}
	}
	
	private static ProjectListListener projectListListener = new ProjectListListener();
	private static ArrayList<DirtyListener> listeners = new ArrayList<DirtyListener>();

	public static void initialize() {
		Projects.addPropertyChangeListener(Projects.projectListProperty, projectListListener);
	}
}
