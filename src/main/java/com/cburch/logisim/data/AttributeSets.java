/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AttributeSets {
	private AttributeSets() { }
	
	public static final AttributeSet EMPTY = new AttributeSet() {
		@Override
		public Object clone() { return this; }
		public void addAttributeListener(AttributeListener l) { }
		public void removeAttributeListener(AttributeListener l) { }

		public List<Attribute<?>> getAttributes() { return Collections.emptyList(); }
		public boolean containsAttribute(Attribute<?> attr) { return false; }
		public Attribute<?> getAttribute(String name) { return null; }

		public boolean isReadOnly(Attribute<?> attr) { return true; }
		public void setReadOnly(Attribute<?> attr, boolean value) {
			throw new UnsupportedOperationException();
		}
		public boolean isToSave(Attribute<?> attr) { return true; }

		public <V> V getValue(Attribute<V> attr) { return null; }
		public <V> void setValue(Attribute<V> attr, V value) { }
	};
	
	public static <V> AttributeSet fixedSet(Attribute<V> attr, V initValue) {
		return new SingletonSet(attr, initValue);
	}

	public static AttributeSet fixedSet(Attribute<?>[] attrs, Object[] initValues) {
		if (attrs.length > 1) {
			return new FixedSet(attrs, initValues);
		} else if (attrs.length == 1) {
			return new SingletonSet(attrs[0], initValues[0]);
		} else {
			return EMPTY;
		}
	}
	
	public static void copy(AttributeSet src, AttributeSet dst) {
		if (src == null || src.getAttributes() == null) return;
		for (Attribute<?> attr : src.getAttributes()) {
			@SuppressWarnings("unchecked")
			Attribute<Object> attrObj = (Attribute<Object>) attr;
			Object value = src.getValue(attr);
			dst.setValue(attrObj, value);
		}
	}

	private static class SingletonSet extends AbstractAttributeSet {
		private List<Attribute<?>> attrs;
		private Object value;
		private boolean readOnly = false;
		
		SingletonSet(Attribute<?> attr, Object initValue) {
			this.attrs = new ArrayList<Attribute<?>>(1);
			this.attrs.add(attr);
			this.value = initValue;
		}

		@Override
		protected void copyInto(AbstractAttributeSet destSet) {
			SingletonSet dest = (SingletonSet) destSet;
			dest.attrs = this.attrs;
			dest.value = this.value;
			dest.readOnly = this.readOnly;
		}

		@Override
		public List<Attribute<?>> getAttributes() {
			return attrs;
		}
		
		@Override
		public boolean isReadOnly(Attribute<?> attr) {
			return readOnly;
		}
		
		@Override
		public void setReadOnly(Attribute<?> attr, boolean value) {
			int index = attrs.indexOf(attr);
			if (index < 0) throw new IllegalArgumentException("attribute " + attr.getName() + " absent");
			readOnly = value;
		}

		@Override
		public <V> V getValue(Attribute<V> attr) {
			int index = attrs.indexOf(attr);
			@SuppressWarnings("unchecked")
			V ret = (V) (index >= 0 ? value : null);
			return ret;
		}

		@Override
		public <V> void setValue(Attribute<V> attr, V value) {
			int index = attrs.indexOf(attr);
			if (index < 0) throw new IllegalArgumentException("attribute " + attr.getName() + " absent");
			if (readOnly) throw new IllegalArgumentException("read only");
			this.value = value;
			fireAttributeValueChanged(attr, value);
		}
	}
	
	private static class FixedSet extends AbstractAttributeSet {
		private List<Attribute<?>> attrs;
		private Object[] values;
		private int readOnly = 0;
		
		FixedSet(Attribute<?>[] attrs, Object[] initValues) {
			if (attrs.length != initValues.length) {
				throw new IllegalArgumentException("attribute and value arrays must have same length");
			}
			if (attrs.length > 32) {
				throw new IllegalArgumentException("cannot handle more than 32 attributes");
			}
			this.attrs = Arrays.asList(attrs);
			this.values = initValues.clone();
		}

		@Override
		protected void copyInto(AbstractAttributeSet destSet) {
			FixedSet dest = (FixedSet) destSet;
			dest.attrs = this.attrs;
			dest.values = this.values.clone();
			dest.readOnly = this.readOnly;
		}

		@Override
		public List<Attribute<?>> getAttributes() {
			return attrs;
		}
		
		@Override
		public boolean isReadOnly(Attribute<?> attr) {
			int index = attrs.indexOf(attr);
			if (index < 0) return true;
			return isReadOnly(index);
		}
		
		@Override
		public void setReadOnly(Attribute<?> attr, boolean value) {
			int index = attrs.indexOf(attr);
			if (index < 0) throw new IllegalArgumentException("attribute " + attr.getName() + " absent");
			
			if (value) readOnly |= (1 << index);
			else readOnly &= ~(1 << index);
		}

		@Override
		public <V> V getValue(Attribute<V> attr) {
			int index = attrs.indexOf(attr);
			if (index < 0) {
				return null;
			} else {
				@SuppressWarnings("unchecked")
				V ret = (V) values[index];
				return ret;
			}
		}

		@Override
		public <V> void setValue(Attribute<V> attr, V value) {
			int index = attrs.indexOf(attr);
			if (index < 0) throw new IllegalArgumentException("attribute " + attr.getName() + " absent");
			if (isReadOnly(index)) throw new IllegalArgumentException("read only");
			values[index] = value;
			fireAttributeValueChanged(attr, value);
		}
		
		private boolean isReadOnly(int index) {
			return ((readOnly >> index) & 1) == 1;
		}
	}

}
