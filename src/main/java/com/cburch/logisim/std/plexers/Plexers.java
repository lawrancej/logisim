/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.plexers;

import java.awt.Graphics;
import java.util.List;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.tools.FactoryDescription;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.GraphicsUtil;
import static com.cburch.logisim.util.LocaleString.*;

public class Plexers extends Library {
	public static final Attribute<BitWidth> ATTR_SELECT
		= Attributes.forBitWidth("select", __("plexerSelectBitsAttr"), 1, 5);
	public static final Object DEFAULT_SELECT = BitWidth.create(1);

	public static final Attribute<Boolean> ATTR_TRISTATE
		= Attributes.forBoolean("tristate", __("plexerThreeStateAttr"));
	public static final Object DEFAULT_TRISTATE = Boolean.FALSE;
	
	public static final AttributeOption DISABLED_FLOATING
		= new AttributeOption("Z", __("plexerDisabledFloating"));
	public static final AttributeOption DISABLED_ZERO
		= new AttributeOption("0", __("plexerDisabledZero"));
	public static final Attribute<AttributeOption> ATTR_DISABLED
		= Attributes.forOption("disabled", __("plexerDisabledAttr"),
				new AttributeOption[] { DISABLED_FLOATING, DISABLED_ZERO });
	
	public static final Attribute<Boolean> ATTR_ENABLE
		= Attributes.forBoolean("enable", __("plexerEnableAttr"));

	static final AttributeOption SELECT_BOTTOM_LEFT
		= new AttributeOption("bl", __("plexerSelectBottomLeftOption"));
	static final AttributeOption SELECT_TOP_RIGHT
		= new AttributeOption("tr", __("plexerSelectTopRightOption"));
	static final Attribute<AttributeOption> ATTR_SELECT_LOC = Attributes.forOption("selloc",
			__("plexerSelectLocAttr"),
			new AttributeOption[] { SELECT_BOTTOM_LEFT, SELECT_TOP_RIGHT });

	protected static final int DELAY = 3;
	
	private static FactoryDescription[] DESCRIPTIONS = {
		new FactoryDescription("Multiplexer", __("multiplexerComponent"),
				"multiplexer.gif", "Multiplexer"),
		new FactoryDescription("Demultiplexer", __("demultiplexerComponent"),
				"demultiplexer.gif", "Demultiplexer"),
		new FactoryDescription("Decoder", __("decoderComponent"),
				"decoder.gif", "Decoder"),
		new FactoryDescription("Priority Encoder", __("priorityEncoderComponent"),
				"priencod.gif", "PriorityEncoder"),
		new FactoryDescription("BitSelector", __("bitSelectorComponent"),
				"bitSelector.gif", "BitSelector"),
	};

	private List<Tool> tools = null;

	public Plexers() { }

	@Override
	public String getName() { return "Plexers"; }

	@Override
	public String getDisplayName() { return _("plexerLibrary"); }

	@Override
	public List<Tool> getTools() {
		if (tools == null) {
			tools = FactoryDescription.getTools(Plexers.class, DESCRIPTIONS);
		}
		return tools;
	}

	static void drawTrapezoid(Graphics g, Bounds bds, Direction facing,
			int facingLean) {
		int wid = bds.getWidth();
		int ht = bds.getHeight();
		int x0 = bds.getX(); int x1 = x0 + wid;
		int y0 = bds.getY(); int y1 = y0 + ht;
		int[] xp = { x0, x1, x1, x0 };
		int[] yp = { y0, y0, y1, y1 };
		if (facing == Direction.WEST) {
			yp[0] += facingLean; yp[3] -= facingLean;
		} else if (facing == Direction.NORTH) {
			xp[0] += facingLean; xp[1] -= facingLean;
		} else if (facing == Direction.SOUTH) {
			xp[2] -= facingLean; xp[3] += facingLean;
		} else {
			yp[1] += facingLean; yp[2] -= facingLean;
		}
		GraphicsUtil.switchToWidth(g, 2);
		g.drawPolygon(xp, yp, 4);
	}
	
	static boolean contains(Location loc, Bounds bds, Direction facing) {
		if (bds.contains(loc, 1)) {
			int x = loc.getX();
			int y = loc.getY();
			int x0 = bds.getX();
			int x1 = x0 + bds.getWidth();
			int y0 = bds.getY();
			int y1 = y0 + bds.getHeight();
			if (facing == Direction.NORTH || facing == Direction.SOUTH) {
				if (x < x0 + 5 || x > x1 - 5) {
					if (facing == Direction.SOUTH) {
						return y < y0 + 5;
					} else {
						return y > y1 - 5;
					}
				} else {
					return true;
				}
			} else {
				if (y < y0 + 5 || y > y1 - 5) {
					if (facing == Direction.EAST) {
						return x < x0 + 5;
					} else {
						return x > x1 - 5;
					}
				} else {
					return true;
				}
			}
		} else {
			return false;
		}
	}
}
