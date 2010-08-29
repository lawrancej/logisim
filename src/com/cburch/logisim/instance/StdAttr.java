/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.instance;

import java.awt.Font;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Direction;

public interface StdAttr {
	public static final Attribute<Direction> FACING
		= Attributes.forDirection("facing", Strings.getter("stdFacingAttr"));

	public static final Attribute<BitWidth> WIDTH
		= Attributes.forBitWidth("width", Strings.getter("stdDataWidthAttr"));

	public static final AttributeOption TRIG_RISING
		= new AttributeOption("rising", Strings.getter("stdTriggerRising"));
	public static final AttributeOption TRIG_FALLING
		= new AttributeOption("falling", Strings.getter("stdTriggerFalling"));
	public static final AttributeOption TRIG_HIGH
		= new AttributeOption("high", Strings.getter("stdTriggerHigh"));
	public static final AttributeOption TRIG_LOW
		= new AttributeOption("low", Strings.getter("stdTriggerLow"));
	public static final Attribute<AttributeOption> TRIGGER
		= Attributes.forOption("trigger", Strings.getter("stdTriggerAttr"),
			new AttributeOption[] {
				TRIG_RISING, TRIG_FALLING, TRIG_HIGH, TRIG_LOW
			});
	public static final Attribute<AttributeOption> EDGE_TRIGGER
		= Attributes.forOption("trigger", Strings.getter("stdTriggerAttr"),
			new AttributeOption[] { TRIG_RISING, TRIG_FALLING });

	public static final Attribute<String> LABEL
		= Attributes.forString("label", Strings.getter("stdLabelAttr"));

	public static final Attribute<Font> LABEL_FONT
		= Attributes.forFont("labelfont", Strings.getter("stdLabelFontAttr"));
	public static final Font DEFAULT_LABEL_FONT
		= new Font("SansSerif", Font.PLAIN, 12);
}
