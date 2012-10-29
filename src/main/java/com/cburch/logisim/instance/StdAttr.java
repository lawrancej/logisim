/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.instance;

import java.awt.Font;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Direction;
import static com.cburch.logisim.util.LocaleString.*;

public interface StdAttr {
	public static final Attribute<Direction> FACING
		= Attributes.forDirection("facing", __("stdFacingAttr"));

	public static final Attribute<BitWidth> WIDTH
		= Attributes.forBitWidth("width", __("stdDataWidthAttr"));

	public static final AttributeOption TRIG_RISING
		= new AttributeOption("rising", __("stdTriggerRising"));
	public static final AttributeOption TRIG_FALLING
		= new AttributeOption("falling", __("stdTriggerFalling"));
	public static final AttributeOption TRIG_HIGH
		= new AttributeOption("high", __("stdTriggerHigh"));
	public static final AttributeOption TRIG_LOW
		= new AttributeOption("low", __("stdTriggerLow"));
	public static final Attribute<AttributeOption> TRIGGER
		= Attributes.forOption("trigger", __("stdTriggerAttr"),
			new AttributeOption[] {
				TRIG_RISING, TRIG_FALLING, TRIG_HIGH, TRIG_LOW
			});
	public static final Attribute<AttributeOption> EDGE_TRIGGER
		= Attributes.forOption("trigger", __("stdTriggerAttr"),
			new AttributeOption[] { TRIG_RISING, TRIG_FALLING });

	public static final Attribute<String> LABEL
		= Attributes.forString("label", __("stdLabelAttr"));

	public static final Attribute<Font> LABEL_FONT
		= Attributes.forFont("labelfont", __("stdLabelFontAttr"));
	public static final Font DEFAULT_LABEL_FONT
		= new Font("SansSerif", Font.PLAIN, 12);
}
