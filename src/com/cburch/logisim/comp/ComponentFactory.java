/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.comp;

import java.awt.Color;

import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeDefaultProvider;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.StringGetter;

/**
 * Represents a category of components that appear in a circuit. This class
 * and <code>Component</code> share the same sort of relationship as the
 * relation between <em>classes</em> and <em>instances</em> in Java. Normally,
 * there is only one ComponentFactory created for any particular category.
 */
public interface ComponentFactory extends AttributeDefaultProvider {
	public static final Object SHOULD_SNAP = new Object();
	public static final Object TOOL_TIP = new Object();
	public static final Object FACING_ATTRIBUTE_KEY = new Object();
	
	public String getName();
	public String getDisplayName();
	public StringGetter getDisplayGetter();
	public Component createComponent(Location loc, AttributeSet attrs);
	public Bounds getOffsetBounds(AttributeSet attrs);
	public AttributeSet createAttributeSet();
	public boolean isAllDefaultValues(AttributeSet attrs, LogisimVersion ver);
	public Object getDefaultAttributeValue(Attribute<?> attr, LogisimVersion ver);
	public void drawGhost(ComponentDrawContext context, Color color,
			int x, int y, AttributeSet attrs);
	public void paintIcon(ComponentDrawContext context,
			int x, int y, AttributeSet attrs);
	/**
	 * Retrieves special-purpose features for this factory. This technique
	 * allows for future Logisim versions to add new features
	 * for components without requiring changes to existing components.
	 * It also removes the necessity for the Component API to directly
	 * declare methods for each individual feature.
	 * In most cases, the <code>key</code> is a <code>Class</code> object
	 * corresponding to an interface, and the method should return an
	 * implementation of that interface if it supports the feature.
	 * 
	 * As of this writing, possible values for <code>key</code> include:
	 * <code>TOOL_TIP</code> (return a <code>String</code>) and
	 * <code>SHOULD_SNAP</code> (return a <code>Boolean</code>).
	 * 
	 * @param key  an object representing a feature.
	 * @return an object representing information about how the component
	 *    supports the feature, or <code>null</code> if it does not support
	 *    the feature.
	 */
	public Object getFeature(Object key, AttributeSet attrs);
}
