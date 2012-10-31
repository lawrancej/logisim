/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;

import java.util.Collections;
import java.util.List;

import com.cburch.logisim.comp.ComponentFactory;

public abstract class Library {
	public String getName() {
		return getClass().getName();
	}

	public abstract List<? extends Tool> getTools();

	@Override
	public String toString() { return getName(); }

	public String getDisplayName() { return getName(); }
	
	public boolean isDirty() { return false; }

	public List<Library> getLibraries() {
		return Collections.emptyList();
	}

	public Tool getTool(String name) {
		for (Tool tool : getTools()) {
			if (tool.getName().equals(name)) {
				return tool;
			}
		}
		return null;
	}

	public boolean containsFromSource(Tool query) {
		for (Tool tool : getTools()) {
			if (tool.sharesSource(query)) {
				return true;
			}
		}
		return false;
	}
	
	public int indexOf(ComponentFactory query) {
		int index = -1;
		for (Tool obj : getTools()) {
			index++;
			if (obj instanceof AddTool) {
				AddTool tool = (AddTool) obj;
				if (tool.getFactory() == query) return index;
			}
		}
		return -1;
	}
	
	public boolean contains(ComponentFactory query) {
		return indexOf(query) >= 0;
	}

	public Library getLibrary(String name) {
		for (Library lib : getLibraries()) {
			if (lib.getName().equals(name)) {
				return lib;
			}
		}
		return null;
	}

}
