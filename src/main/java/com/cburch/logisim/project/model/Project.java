package com.cburch.logisim.project.model;

import java.util.ArrayList;

public class Project {
	// Version of Logisim used to edit project
	String source;
	// circ file format version
	String version;
	// A list of libraries used in the project
	ArrayList<Library> libraries = new ArrayList<Library>();
}
