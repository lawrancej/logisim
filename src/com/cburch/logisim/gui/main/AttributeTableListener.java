/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;

public interface AttributeTableListener {
    public void valueChangeRequested(AttributeTable table, AttributeSet attrs,
            Attribute<?> attr, Object value);
}
