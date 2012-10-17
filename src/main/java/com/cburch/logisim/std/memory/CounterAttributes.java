/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.util.List;

import com.cburch.logisim.data.AbstractAttributeSet;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.AttributeSets;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.instance.StdAttr;

class CounterAttributes extends AbstractAttributeSet {
	private AttributeSet base;
	
	public CounterAttributes() {
		base = AttributeSets.fixedSet(new Attribute<?>[] {
				StdAttr.WIDTH, Counter.ATTR_MAX, Counter.ATTR_ON_GOAL,
				StdAttr.EDGE_TRIGGER,
				StdAttr.LABEL, StdAttr.LABEL_FONT
			}, new Object[] {
				BitWidth.create(8), Integer.valueOf(0xFF),
				Counter.ON_GOAL_WRAP,
				StdAttr.TRIG_RISING,
				"", StdAttr.DEFAULT_LABEL_FONT
			});
	}
	
	@Override
	public void copyInto(AbstractAttributeSet dest) {
		((CounterAttributes) dest).base = (AttributeSet) this.base.clone();
	}

	@Override
	public List<Attribute<?>> getAttributes() {
		return base.getAttributes();
	}

	@Override
	public <V> V getValue(Attribute<V> attr) {
		return base.getValue(attr);
	}

	@Override
	public <V> void setValue(Attribute<V> attr, V value) {
		Object oldValue = base.getValue(attr);
		if (oldValue == null ? value == null : oldValue.equals(value)) return;

		Integer newMax = null;
		if (attr == StdAttr.WIDTH) {
			BitWidth oldWidth = base.getValue(StdAttr.WIDTH);
			BitWidth newWidth = (BitWidth) value;
			int oldW = oldWidth.getWidth();
			int newW = newWidth.getWidth();
			Integer oldValObj = base.getValue(Counter.ATTR_MAX);
			int oldVal = oldValObj.intValue();
			base.setValue(StdAttr.WIDTH, newWidth);
			if (newW > oldW) {
				newMax = Integer.valueOf(newWidth.getMask());
			} else {
				int v = oldVal & newWidth.getMask();
				if (v != oldVal) {
					Integer newValObj = Integer.valueOf(v);
					base.setValue(Counter.ATTR_MAX, newValObj);
					fireAttributeValueChanged(Counter.ATTR_MAX, newValObj);
				}
			}
			fireAttributeValueChanged(StdAttr.WIDTH, newWidth);
		} else if (attr == Counter.ATTR_MAX) {
			int oldVal = base.getValue(Counter.ATTR_MAX).intValue();
			BitWidth width = base.getValue(StdAttr.WIDTH);
			int newVal = ((Integer) value).intValue() & width.getMask();
			if (newVal != oldVal) {
				@SuppressWarnings("unchecked")
				V val = (V) Integer.valueOf(newVal);
				value = val;
			}
		}
		base.setValue(attr, value);
		fireAttributeValueChanged(attr, value);
		if (newMax != null) {
			base.setValue(Counter.ATTR_MAX, newMax);
			fireAttributeValueChanged(Counter.ATTR_MAX, newMax);
		}
	}

	@Override
	public boolean containsAttribute(Attribute<?> attr) {
		return base.containsAttribute(attr);
	}

	@Override
	public Attribute<?> getAttribute(String name) {
		return base.getAttribute(name);
	}

	@Override
	public boolean isReadOnly(Attribute<?> attr) {
		return base.isReadOnly(attr);
	}

	@Override
	public void setReadOnly(Attribute<?> attr, boolean value) {
		base.setReadOnly(attr, value);
	}
}
