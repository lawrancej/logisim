/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.data;

public class AttributeOption implements AttributeOptionInterface {
    private Object value;
    private String name;
    private String desc;

    public AttributeOption(Object value, String desc) {
        this.value = value;
        this.name = value.toString();
        this.desc = desc;
    }

    public AttributeOption(Object value, String name, String desc) {
        this.value = value;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public Object getValue() { return value; }

    @Override
    public String toString() { return name; }

    @Override
    public String toDisplayString() { return desc.toString(); }
}
