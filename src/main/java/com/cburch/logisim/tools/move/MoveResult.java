/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools.move;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

import com.cburch.logisim.circuit.ReplacementMap;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Location;

public class MoveResult {
	private ReplacementMap replacements;
	private Collection<ConnectionData> unsatisfiedConnections;
	private Collection<Location> unconnectedLocations;
	private int totalDistance;
	
	public MoveResult(MoveRequest request, ReplacementMap replacements,
			Collection<ConnectionData> unsatisfiedConnections,
			int totalDistance) {
		this.replacements = replacements;
		this.unsatisfiedConnections = unsatisfiedConnections;
		this.totalDistance = totalDistance;
		
		ArrayList<Location> unconnected = new ArrayList<Location>();
		for (ConnectionData conn : unsatisfiedConnections) {
			unconnected.add(conn.getLocation());
		}
		unconnectedLocations = unconnected;
	}
	
	void addUnsatisfiedConnections(Collection<ConnectionData> toAdd) {
		unsatisfiedConnections.addAll(toAdd);
		for (ConnectionData conn : toAdd) {
			unconnectedLocations.add(conn.getLocation());
		}
	}
	
	public Collection<Wire> getWiresToAdd() {
		@SuppressWarnings("unchecked")
		Collection<Wire> ret = (Collection<Wire>) replacements.getAdditions();
		return ret;
	}
	
	public Collection<Wire> getWiresToRemove() {
		@SuppressWarnings("unchecked")
		Collection<Wire> ret = (Collection<Wire>) replacements.getAdditions();
		return ret;
	}
	
	public ReplacementMap getReplacementMap() {
		return replacements;
	}
	
	public Collection<Location> getUnconnectedLocations() {
		return unconnectedLocations;
	}
	
	Collection<ConnectionData> getUnsatisifiedConnections() {
		return unsatisfiedConnections;
	}
	
	int getTotalDistance() {
		return totalDistance;
	}
	
	public void print(PrintStream out) {
		boolean printed = false;
		for (Component w : replacements.getAdditions()) {
			printed = true;
			out.println("add " + w);
		}
		for (Component w : replacements.getRemovals()) {
			printed = true;
			out.println("del " + w);
		}
		for (Component w : replacements.getReplacedComponents()) {
			printed = true;
			out.print("repl " + w + " by");
			for (Component w2 : replacements.getComponentsReplacing(w)) {
				out.print(" " + w2);
			}
			out.println();
		}
		if (!printed) {
			out.println("no replacements");
		}
	}
}
