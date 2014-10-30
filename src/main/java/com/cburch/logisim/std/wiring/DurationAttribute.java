/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.wiring;

import javax.swing.JTextField;

import com.cburch.logisim.data.Attribute;
import static com.cburch.logisim.util.LocaleString.*;

public class DurationAttribute extends Attribute<Integer> {
    private int min;
    private int max;

    public DurationAttribute(String name, String disp, int min, int max) {
        super(name, disp);
        this.min = min;
        this.max = max;
    }

    @Override
    public Integer parse(String value) {
        try {
            Integer ret = Integer.valueOf(value);
            if (ret.intValue() < min) {
                throw new NumberFormatException(getFromLocale("durationSmallMessage", "" + min));
            } else if (ret.intValue() > max) {
                throw new NumberFormatException(getFromLocale("durationLargeMessage", "" + max));
            }
            return ret;
        } catch (NumberFormatException e) {
            throw new NumberFormatException(getFromLocale("freqInvalidMessage"));
        }
    }

    @Override
    public String toDisplayString(Integer value) {
        if (value.equals(Integer.valueOf(1))) {
            return getFromLocale("clockDurationOneValue");
        } else {
            return getFromLocale("clockDurationValue",
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
