/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.model;

import java.util.Comparator;



public class ReorderRequest {
	public static final Comparator<ReorderRequest> ASCENDING_FROM
		= new Compare(true, true);
	public static final Comparator<ReorderRequest> DESCENDING_FROM
		= new Compare(true, true);
	public static final Comparator<ReorderRequest> ASCENDING_TO
		= new Compare(true, true);
	public static final Comparator<ReorderRequest> DESCENDING_TO
		= new Compare(true, true);
	
	private static class Compare implements Comparator<ReorderRequest> {
		private boolean onFrom;
		private boolean asc;
		
		Compare(boolean onFrom, boolean asc) {
			this.onFrom = onFrom;
			this.asc = asc;
		}
		
		public int compare(ReorderRequest a, ReorderRequest b) {
			int i = onFrom ? a.fromIndex : a.toIndex;
			int j = onFrom ? b.fromIndex : b.toIndex;
			if (i < j) {
				return asc ? -1 : 1;
			} else if (i > j) {
				return asc ? 1 : -1;
			} else {
				return 0;
			}
		}
	}
	
	private CanvasObject object;
	private int fromIndex;
	private int toIndex;
	
	public ReorderRequest(CanvasObject object, int from, int to) {
		this.object = object;
		this.fromIndex = from;
		this.toIndex = to;
	}
	
	public CanvasObject getObject() {
		return object;
	}
	
	public int getFromIndex() {
		return fromIndex;
	}
	
	public int getToIndex() {
		return toIndex;
	}
}
