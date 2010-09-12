/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.tools;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Icon;

import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.canvas.CanvasTool;
import com.cburch.draw.model.DrawingAttributeSet;
import com.cburch.logisim.data.Attribute;

public abstract class AbstractTool extends CanvasTool {
	public static AbstractTool[] getTools(DrawingAttributeSet attrs) {
		return new AbstractTool[] {
			new SelectTool(),
			new LineTool(attrs),
			new PolylineTool(attrs),
			new RectangleTool(attrs),
			new RoundRectangleTool(attrs),
			new OvalTool(attrs),
			new PolygonTool(attrs),
		};
	}
	
	public abstract Icon getIcon();
	public abstract List<Attribute<?>> getAttributes();
	public String getDescription() { return null; }
	
	/*TODO delete if still unneeded: other Tool methods used in Logisim 2.0
public abstract String getName();
public abstract String getDisplayName();

public Tool cloneTool() { return this; }
public boolean sharesSource(Tool other) { return this == other; }
public void setAttributeSet(AttributeSet attrs) { }
public void paintIcon(ComponentDrawContext c, int x, int y) { }
public String toString() { return getName(); }
*/



	
	//
	// CanvasTool methods
	//
	@Override
	public abstract Cursor getCursor(Canvas canvas);
	
	@Override
	public void toolSelected(Canvas canvas) { }
	@Override
	public void toolDeselected(Canvas canvas) { }
	
	@Override
	public void mouseMoved(Canvas canvas, MouseEvent e) { }
	@Override
	public void mousePressed(Canvas canvas, MouseEvent e) { }
	@Override
	public void mouseDragged(Canvas canvas, MouseEvent e) { }
	@Override
	public void mouseReleased(Canvas canvas, MouseEvent e) { }
	@Override
	public void mouseEntered(Canvas canvas, MouseEvent e) { }
	@Override
	public void mouseExited(Canvas canvas, MouseEvent e) { }

	@Override
	public void keyPressed(Canvas canvas, KeyEvent e) { }
	@Override
	public void keyReleased(Canvas canvas, KeyEvent e) { }
	@Override
	public void keyTyped(Canvas canvas, KeyEvent e) { }
	
	@Override
	public void draw(Canvas canvas, Graphics g) { }
}
