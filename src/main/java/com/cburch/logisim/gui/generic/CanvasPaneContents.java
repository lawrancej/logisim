/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.generic;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.Scrollable;

public interface CanvasPaneContents extends Scrollable {
	public void setCanvasPane(CanvasPane pane);
	public void recomputeSize();
	
	// from Scrollable
	public Dimension getPreferredScrollableViewportSize();
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction);
	public boolean getScrollableTracksViewportHeight();
	public boolean getScrollableTracksViewportWidth();
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction);
}
