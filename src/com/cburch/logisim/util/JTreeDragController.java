/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.awt.Point;

import javax.swing.JTree;

/* This comes from "Denis" at http://forum.java.sun.com/thread.jspa?forumID=57&threadID=296255 */

/*
 * My program use 4 classes. The application displays two trees.
 * You can drag (move by default or copy with ctrl pressed) a node from the left tree to the right one, from the right to the left and inside the same tree.
 * The rules for moving are :
 *   - you can't move the root
 *   - you can't move the selected node to its subtree (in the same tree).
 *   - you can't move the selected node to itself (in the same tree).
 *   - you can't move the selected node to its parent (in the same tree).
 *   - you can move a node to anywhere you want according to the 4 previous rules.
 *  The rules for copying are :
 *   - you can copy a node to anywhere you want.
 *
 * In the implementation I used DnD version of Java 1.3 because in 1.4 the DnD is too restrictive :
 * you can't do what you want (displaying the image of the node while dragging, changing the cursor
 * according to where you are dragging, etc...). In 1.4, the DnD is based on the 1.3 version but
 * it is too encapsulated.
 */

public interface JTreeDragController {
	public boolean canPerformAction(JTree target, Object draggedNode,
			int action, Point location);

	public boolean executeDrop(JTree tree, Object draggedNode,
			Object newParentNode, int action);
}
