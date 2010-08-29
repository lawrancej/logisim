/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

import com.cburch.logisim.circuit.RadixOption;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.AttributeSets;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Direction;

public class Options {
	public static final AttributeOption GATE_UNDEFINED_IGNORE
	= new AttributeOption("ignore", Strings.getter("gateUndefinedIgnore"));
	public static final AttributeOption GATE_UNDEFINED_ERROR
		= new AttributeOption("error", Strings.getter("gateUndefinedError"));
		
	public static final Attribute<Boolean> showgrid_attr
		= Attributes.forBoolean("showgrid", Strings.getter("showGridOption"));
	public static final Attribute<Boolean> preview_attr
		= Attributes.forBoolean("preview", Strings.getter("printPreviewOption"));
	public static final Attribute<Boolean> showhalo_attr
		= Attributes.forBoolean("showhalo", Strings.getter("showHaloOption"));
	public static final Attribute<Boolean> showtips_attr
		= Attributes.forBoolean("showhalo", Strings.getter("showHaloOption"));
	public static final Attribute<Double> zoom_attr
		= Attributes.forDouble("zoom", Strings.getter("zoomFactorOption"));
	public static final Attribute<Integer> sim_limit_attr
		= Attributes.forInteger("simlimit", Strings.getter("simLimitOption"));
	public static final Attribute<Integer> sim_rand_attr
		= Attributes.forInteger("simrand", Strings.getter("simRandomOption"));
	public static final Attribute<RadixOption> ATTR_RADIX_1 = RadixOption.ATTRIBUTE;
	public static final Attribute<RadixOption> ATTR_RADIX_2
		= Attributes.forOption("radix2", Strings.getter("radix2Option"), RadixOption.OPTIONS);
	public static final Attribute<AttributeOption> ATTR_GATE_UNDEFINED
		= Attributes.forOption("gateUndefined", Strings.getter("gateUndefinedOption"),
				new AttributeOption[] { GATE_UNDEFINED_IGNORE, GATE_UNDEFINED_ERROR });
	public static final Attribute<Boolean> ATTR_CONNECT_ON_MOVE
		= Attributes.forBoolean("moveconnect", Strings.getter("connectOnMoveOption"));
	
	public static final Object TOOLBAR_HIDDEN = new AttributeOption("hidden", "hidden", Strings.getter("toolbarHidden"));
	public static final Object TOOLBAR_DOWN_MIDDLE = new AttributeOption("middle", "middle", Strings.getter("toolbarDownMiddle"));
	public static final Object[] OPTIONS_TOOLBAR_LOC =
		{ Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST,
			TOOLBAR_DOWN_MIDDLE, TOOLBAR_HIDDEN };
	public static final Attribute<Object> ATTR_TOOLBAR_LOC
		= Attributes.forOption("toolbarloc", Strings.getter("toolbarlocOption"),
				OPTIONS_TOOLBAR_LOC);
	
	public static final Integer sim_rand_dflt = Integer.valueOf(32);

	private static final Attribute<?>[] ATTRIBUTES = {
		showgrid_attr, preview_attr, showhalo_attr,
			showtips_attr, ATTR_CONNECT_ON_MOVE, zoom_attr,
			ATTR_GATE_UNDEFINED, sim_limit_attr, sim_rand_attr,
			ATTR_RADIX_1, ATTR_RADIX_2, ATTR_TOOLBAR_LOC,
	};
	private static final Object[] DEFAULTS = {
		Boolean.TRUE, Boolean.FALSE, Boolean.TRUE,
			Boolean.TRUE, Boolean.TRUE, new Double(1.0),
			GATE_UNDEFINED_IGNORE, Integer.valueOf(1000), Integer.valueOf(0),
			RadixOption.RADIX_2, RadixOption.RADIX_10_SIGNED, Direction.NORTH,
	};
	
	private AttributeSet attrs = AttributeSets.fixedSet(ATTRIBUTES, DEFAULTS);
	private MouseMappings mmappings = new MouseMappings();
	private ToolbarData toolbar = new ToolbarData();

	public Options() { }

	public AttributeSet getAttributeSet() {
		return attrs;
	}

	public MouseMappings getMouseMappings() {
		return mmappings;
	}

	public ToolbarData getToolbarData() {
		return toolbar;
	}

	public void copyFrom(Options other, LogisimFile dest) {
		AttributeSets.copy(other.attrs, this.attrs);
		this.toolbar.copyFrom(other.toolbar, dest);
		this.mmappings.copyFrom(other.mmappings, dest);
	}
}
