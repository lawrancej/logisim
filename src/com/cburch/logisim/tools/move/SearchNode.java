/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools.move;

import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;

class SearchNode implements Comparable<SearchNode> {
	private static final int CROSSING_PENALTY = 20;
	private static final int TURN_PENALTY = 50;
	
	private final Location loc;
	private final Direction dir;
	private ConnectionData conn;
	private final Location dest; 
	private int dist;
	private int heur;
	private boolean extendsWire;
	private SearchNode prev;
	
	public SearchNode(ConnectionData conn, Location src,
			Direction srcDir, Location dst) {
		this(src, srcDir, conn, dst, 0, srcDir != null, null);
	}
	
	private SearchNode(Location loc, Direction dir,
			ConnectionData conn, Location dest, int dist, boolean extendsWire,
			SearchNode prev) {
		this.loc = loc;
		this.dir = dir;
		this.conn = conn;
		this.dest = dest;
		this.dist = dist;
		this.heur = dist + this.getHeuristic();
		this.extendsWire = extendsWire;
		this.prev = prev;
	}
	
	private int getHeuristic() {
		Location cur = loc;
		Location dst = dest;
		Direction curDir = dir;
		int dx = dst.getX() - cur.getX();
		int dy = dst.getY() - cur.getY();
		int ret = -1;
		if (extendsWire) {
			ret = -1;
			if (curDir == Direction.EAST) {
				if (dx > 0) ret = dx / 10 * 9 + Math.abs(dy);
			} else if (curDir == Direction.WEST) {
				if (dx < 0) ret = -dx / 10 * 9 + Math.abs(dy);
			} else if (curDir == Direction.SOUTH) {
				if (dy > 0) ret = Math.abs(dx) + dy / 10 * 9;
			} else if (curDir == Direction.NORTH) {
				if (dy < 0) ret = Math.abs(dx) - dy / 10 * 9;
			}
		}
		if (ret < 0) {
			ret = Math.abs(dx) + Math.abs(dy);
		}
		boolean penalizeDoubleTurn = false;
		if (curDir == Direction.EAST) {
			penalizeDoubleTurn = dx < 0;
		} else if (curDir == Direction.WEST) {
			penalizeDoubleTurn = dx > 0;
		} else if (curDir == Direction.NORTH) {
			penalizeDoubleTurn = dy > 0;
		} else if (curDir == Direction.SOUTH) {
			penalizeDoubleTurn = dy < 0;
		} else if (curDir == null) {
			if (dx != 0 || dy != 0) ret += TURN_PENALTY;
		}
		if (penalizeDoubleTurn) {
			ret += 2 * TURN_PENALTY;
		} else if (dx != 0 && dy != 0) {
			ret += TURN_PENALTY;
		}
		return ret;
	}
	
	public SearchNode next(Direction moveDir, boolean crossing) {
		int newDist = dist;
		Direction connDir = conn.getDirection();
		Location nextLoc = loc.translate(moveDir, 10);
		boolean exWire = extendsWire && moveDir == connDir;
		if (exWire) {
			newDist += 9;
		} else {
			newDist += 10;
		}
		if (crossing) newDist += CROSSING_PENALTY;
		if (moveDir != dir) newDist += TURN_PENALTY;
		if (nextLoc.getX() < 0 || nextLoc.getY() < 0) {
			return null;
		} else {
			return new SearchNode(nextLoc, moveDir, conn, dest,
					newDist, exWire, this);
		}
	}
	
	public boolean isStart() {
		return prev == null;
	}
	
	public boolean isDestination() {
		return dest.equals(loc);
	}
	
	public SearchNode getPrevious() {
		return prev;
	}
	
	public int getDistance() {
		return dist;
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public Direction getDirection() {
		return dir;
	}
	
	public int getHeuristicValue() {
		return heur;
	}
	
	public Location getDestination() {
		return dest;
	}
	
	public boolean isExtendingWire() {
		return extendsWire;
	}
	
	public ConnectionData getConnection() {
		return conn;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof SearchNode) {
			SearchNode o = (SearchNode) other;
			return this.loc.equals(o.loc)
				&& (this.dir == null ? o.dir == null : this.dir.equals(o.dir))
				&& this.dest.equals(o.dest);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		int dirHash = dir == null ? 0 : dir.hashCode();
		return ((loc.hashCode() * 31) + dirHash) * 31 + dest.hashCode();
	}
	
	public int compareTo(SearchNode o) {
		int ret = this.heur - o.heur;
		
		if (ret == 0) {
			return this.hashCode() - o.hashCode(); 
		} else {
			return ret;
		}
	}
	
	@Override
	public String toString() {
		return loc + "/" + (dir == null ? "null" : dir.toString())
			+ (extendsWire ? "+" : "-")
			+ "/" + dest + ":" + dist + "+" + (heur-dist);
	}
}
