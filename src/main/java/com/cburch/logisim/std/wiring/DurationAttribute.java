/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.wiring;

import javax.swing.JTextField;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.util.StringGetter;
import static com.cburch.logisim.util.LocaleString.*;

public class DurationAttribute extends Attribute<Integer> {
	private int min;
	private int max;
	
	public DurationAttribute(String name, StringGetter disp, int min, int max) {
		super(name, disp);
		this.min = min;
		this.max = max;
	}

	@Override
	public Integer parse(String value) {
		try {
			Integer ret = Integer.valueOf(value);
			if (ret.intValue() < min) {
				throw new NumberFormatException(_("durationSmallMessage", "" + min));
			} else if (ret.intValue() > max) {
				throw new NumberFormatException(_("durationLargeMessage", "" + max));
			}
			return ret;
		} catch (NumberFormatException e) {
			throw new NumberFormatException(_("freqInvalidMessage"));
		}
	}

	@Override
	public String toDisplayString(Integer value) {
		if (value.equals(Integer.valueOf(1))) {
			return _("clockDurationOneValue");
		} else {
			return _("clockDurationValue",
					value.toString());
		}
	}

	@Override
	public java.awt.Component getCellEditor(Integer value) {
		JTextField field = new JTextField();
		field.setText(value.toString());
		return field;
	}

}
