/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.comp;


import java.awt.Graphics;
import java.util.List;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;

public interface Component {
	// listener methods
	public void addComponentListener(ComponentListener l);
	public void removeComponentListener(ComponentListener l);

	// basic information methods
	public ComponentFactory getFactory();
	public AttributeSet getAttributeSet();

	// location/extent methods
	public Location getLocation();
	public Bounds getBounds();
	public Bounds getBounds(Graphics g);
	public boolean contains(Location pt);
	public boolean contains(Location pt, Graphics g);

	// user interface methods
	public void expose(ComponentDrawContext context);
	public void draw(ComponentDrawContext context);
	/**
	 * Retrieves information about a special-purpose feature for this
	 * component. This technique allows future Logisim versions to add
	 * new features for components without requiring changes to existing
	 * components. It also removes the necessity for the Component API to
	 * directly declare methods for each individual feature.
	 * In most cases, the <code>key</code> is a <code>Class</code> object
	 * corresponding to an interface, and the method should return an
	 * implementation of that interface if it supports the feature.
	 * 
	 * As of this writing, possible values for <code>key</code> include:
	 * <code>Pokable.class</code>, <code>CustomHandles.class</code>,
	 * <code>WireRepair.class</code>, <code>TextEditable.class</code>,
	 * <code>MenuExtender.class</code>, <code>ToolTipMaker.class</code>,
	 * <code>ExpressionComputer.class</code>, and <code>Loggable.class</code>.
	 * 
	 * @param key  an object representing a feature.
	 * @return an object representing information about how the component
	 *    supports the feature, or <code>null</code> if it does not support
	 *    the feature.
	 */
	public Object getFeature(Object key);
	
	// propagation methods
	public List<EndData> getEnds(); // list of EndDatas
	public EndData getEnd(int index);
	public boolean endsAt(Location pt);
	public void propagate(CircuitState state);
}
