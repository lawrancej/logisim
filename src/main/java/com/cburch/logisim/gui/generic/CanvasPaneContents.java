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
    @Override
    public Dimension getPreferredScrollableViewportSize();
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation, int direction);
    @Override
    public boolean getScrollableTracksViewportHeight();
    @Override
    public boolean getScrollableTracksViewportWidth();
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect,
            int orientation, int direction);
}
