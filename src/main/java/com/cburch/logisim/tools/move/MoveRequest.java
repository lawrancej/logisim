/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools.move;

class MoveRequest {
	private MoveGesture gesture;
	private int dx;
	private int dy;
	
	public MoveRequest(MoveGesture gesture, int dx, int dy) {
		this.gesture = gesture;
		this.dx = dx;
		this.dy = dy;
	}
	
	public MoveGesture getMoveGesture() {
		return gesture;
	}
	
	public int getDeltaX() {
		return dx;
	}
	
	public int getDeltaY() {
		return dy;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof MoveRequest) {
			MoveRequest o = (MoveRequest) other;
			return this.gesture == o.gesture && this.dx == o.dx && this.dy == o.dy;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return (gesture.hashCode() * 31 + dx) * 31 + dy;
	}
}
