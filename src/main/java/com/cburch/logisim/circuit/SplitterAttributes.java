/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cburch.logisim.data.AbstractAttributeSet;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.instance.StdAttr;
import static com.cburch.logisim.util.LocaleString.*;

class SplitterAttributes extends AbstractAttributeSet {
	public static final AttributeOption APPEAR_LEGACY
		= new AttributeOption("legacy", __("splitterAppearanceLegacy"));
	public static final AttributeOption APPEAR_LEFT
		= new AttributeOption("left", __("splitterAppearanceLeft"));
	public static final AttributeOption APPEAR_RIGHT
		= new AttributeOption("right", __("splitterAppearanceRight"));
	public static final AttributeOption APPEAR_CENTER
		= new AttributeOption("center", __("splitterAppearanceCenter"));
	
	public static final Attribute<AttributeOption> ATTR_APPEARANCE
		= Attributes.forOption("appear", __("splitterAppearanceAttr"),
				new AttributeOption[] { APPEAR_LEFT, APPEAR_RIGHT, APPEAR_CENTER,
					APPEAR_LEGACY});
	
	public static final Attribute<BitWidth> ATTR_WIDTH
		= Attributes.forBitWidth("incoming", __("splitterBitWidthAttr"));
	public static final Attribute<Integer> ATTR_FANOUT
		= Attributes.forIntegerRange("fanout", __("splitterFanOutAttr"), 1, 32);

	private static final List<Attribute<?>> INIT_ATTRIBUTES
		= Arrays.asList(new Attribute<?>[] {
			StdAttr.FACING, ATTR_FANOUT, ATTR_WIDTH, ATTR_APPEARANCE,
		});

	private static final String unchosen_val = "none";

	private static class BitOutOption {
		int value;
		boolean isVertical;
		boolean isLast;

		BitOutOption(int value, boolean isVertical, boolean isLast) {
			this.value = value;
			this.isVertical = isVertical;
			this.isLast = isLast;
		}

		@Override
		public String toString() {
			if (value < 0) {
				return _("splitterBitNone");
			} else {
				String ret = "" + value;
				Direction noteDir;
				if (value == 0) {
					noteDir = isVertical ? Direction.NORTH : Direction.EAST;
				} else if (isLast) {
					noteDir = isVertical ? Direction.SOUTH : Direction.WEST;
				} else {
					noteDir = null;
				}
				if (noteDir != null) {
					ret += " (" + noteDir.toVerticalDisplayString() + ")";
				}
				return ret;
			}
		}
	}

	static class BitOutAttribute extends Attribute<Integer> {
		int which;
		BitOutOption[] options;

		private BitOutAttribute(int which, BitOutOption[] options) {
			super("bit" + which, __("splitterBitAttr", "" + which));
			this.which = which;
			this.options = options;
		}
		
		private BitOutAttribute createCopy() {
			return new BitOutAttribute(which, options);
		}
		
		public Object getDefault() {
			return Integer.valueOf(which + 1);
		}

		@Override
		public Integer parse(String value) {
			if (value.equals(unchosen_val)) {
				return Integer.valueOf(0);
			} else {
				return Integer.valueOf(1 + Integer.parseInt(value));
			}
		}

		@Override
		public String toDisplayString(Integer value) {
			int index = value.intValue();
			return options[index].toString();
		}

		@Override
		public String toStandardString(Integer value) {
			int index = value.intValue();
			if (index == 0) {
				return unchosen_val;
			} else {
				return "" + (index - 1);
			}
		}

		@Override
		public java.awt.Component getCellEditor(Integer value) {
			int index = value.intValue();
			javax.swing.JComboBox combo = new javax.swing.JComboBox(options);
			combo.setSelectedIndex(index);
			return combo;
		}
	}

	private ArrayList<Attribute<?>> attrs = new ArrayList<Attribute<?>>(INIT_ATTRIBUTES);
	private SplitterParameters parameters;
	AttributeOption appear = APPEAR_LEFT;
	Direction facing = Direction.EAST;
	byte fanout = 2;                 // number of ends this splits into
	byte[] bit_end = new byte[2];    // how each bit maps to an end (0 if nowhere);
									 //   other values will be between 1 and fanout
	BitOutOption[] options = null;

	SplitterAttributes() {
		configureOptions();
		configureDefaults();
		parameters = new SplitterParameters(this);
	}
	
	Attribute<?> getBitOutAttribute(int index) {
		return attrs.get(INIT_ATTRIBUTES.size() + index);
	}

	@Override
	protected void copyInto(AbstractAttributeSet destObj) {
		SplitterAttributes dest = (SplitterAttributes) destObj;
		dest.parameters = this.parameters;
		dest.attrs = new ArrayList<Attribute<?>>(this.attrs.size());
		dest.attrs.addAll(INIT_ATTRIBUTES);
		for (int i = INIT_ATTRIBUTES.size(), n = this.attrs.size(); i < n; i++) {
			BitOutAttribute attr = (BitOutAttribute) this.attrs.get(i);
			dest.attrs.add(attr.createCopy());
		}

		dest.facing = this.facing;
		dest.fanout = this.fanout;
		dest.appear = this.appear;
		dest.bit_end = this.bit_end.clone();
		dest.options = this.options;
	}
	
	public SplitterParameters getParameters() {
		SplitterParameters ret = parameters;
		if (ret == null) {
			ret = new SplitterParameters(this);
			parameters = ret;
		}
		return ret;
	}

	@Override
	public List<Attribute<?>> getAttributes() {
		return attrs;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V getValue(Attribute<V> attr) {
		if (attr == StdAttr.FACING) {
			return (V) facing;
		} else if (attr == ATTR_FANOUT) {
			return (V) Integer.valueOf(fanout);
		} else if (attr == ATTR_WIDTH) {
			return (V) BitWidth.create(bit_end.length);
		} else if (attr == ATTR_APPEARANCE) {
			return (V) appear;
		} else if (attr instanceof BitOutAttribute) {
			BitOutAttribute bitOut = (BitOutAttribute) attr;
			return (V) Integer.valueOf(bit_end[bitOut.which]);
		} else {
			return null;
		}
	}

	@Override
	public <V> void setValue(Attribute<V> attr, V value) {
		if (attr == StdAttr.FACING) {
			facing = (Direction) value;
			configureOptions();
			parameters = null;
		} else if (attr == ATTR_FANOUT) {
			int newValue = ((Integer) value).intValue();
			byte[] bits = bit_end;
			for (int i = 0; i < bits.length; i++) {
				if (bits[i] >= newValue) bits[i] = (byte) (newValue - 1);
			}
			fanout = (byte) newValue;
			configureOptions();
			configureDefaults();
			parameters = null;
		} else if (attr == ATTR_WIDTH) {
			BitWidth width = (BitWidth) value;
			bit_end = new byte[width.getWidth()];
			configureOptions();
			configureDefaults();
		} else if (attr == ATTR_APPEARANCE) {
			appear = (AttributeOption) value;
			parameters = null;
		} else if (attr instanceof BitOutAttribute) {
			BitOutAttribute bitOutAttr = (BitOutAttribute) attr;
			int val;
			if (value instanceof Integer) {
				val = ((Integer) value).intValue();
			} else {
				val= ((BitOutOption) value).value + 1;
			}
			if (val >= 0 && val <= fanout) {
				bit_end[bitOutAttr.which] = (byte) val;
			}
		} else {
			throw new IllegalArgumentException("unknown attribute " + attr);
		}
		fireAttributeValueChanged(attr, value);
	}

	private void configureOptions() {
		// compute the set of options for BitOutAttributes
		options = new BitOutOption[fanout + 1];
		boolean isVertical = facing == Direction.EAST || facing == Direction.WEST;
		for (int i = -1; i < fanout; i++) {
			options[i + 1] = new BitOutOption(i, isVertical, i == fanout - 1);
		}

		// go ahead and set the options for the existing attributes
		int offs = INIT_ATTRIBUTES.size();
		int curNum = attrs.size() - offs;
		for (int i = 0; i < curNum; i++) {
			BitOutAttribute attr = (BitOutAttribute) attrs.get(offs + i);
			attr.options = options;
		}
	}
	
	private void configureDefaults() {
		int offs = INIT_ATTRIBUTES.size();
		int curNum = attrs.size() - offs;

		// compute default values
		byte[] dflt = computeDistribution(fanout, bit_end.length, 1);

		boolean changed = curNum != bit_end.length;
		
		// remove excess attributes
		while (curNum > bit_end.length) {
			curNum--;
			attrs.remove(offs + curNum);
		}

		// set existing attributes
		for (int i = 0; i < curNum; i++) {
			if (bit_end[i] != dflt[i]) {
				BitOutAttribute attr = (BitOutAttribute) attrs.get(offs + i);
				bit_end[i] = dflt[i];
				fireAttributeValueChanged(attr, Integer.valueOf(bit_end[i]));
			}
		}

		// add new attributes
		for (int i = curNum; i < bit_end.length; i++) {
			BitOutAttribute attr = new BitOutAttribute(i, options);
			bit_end[i] = dflt[i];
			attrs.add(attr);
		}
		
		if (changed) fireAttributeListChanged();
	}
	
	static byte[] computeDistribution(int fanout, int bits, int order) {
		byte[] ret = new byte[bits];
		if (order >= 0) {
			if (fanout >= bits) {
				for (int i = 0; i < bits; i++) ret[i] = (byte) (i + 1);
			} else {
				int threads_per_end = bits / fanout;
				int ends_with_extra = bits % fanout;
				int cur_end = -1; // immediately increments
				int left_in_end = 0;
				for (int i = 0; i < bits; i++) {
					if (left_in_end == 0) {
						++cur_end;
						left_in_end = threads_per_end;
						if (ends_with_extra > 0) {
							++left_in_end;
							--ends_with_extra;
						}
					}
					ret[i] = (byte) (1 + cur_end);
					--left_in_end;
				}
			}
		} else {
			if (fanout >= bits) {
				for (int i = 0; i < bits; i++) ret[i] = (byte) (fanout - i);
			} else {
				int threads_per_end = bits / fanout;
				int ends_with_extra = bits % fanout;
				int cur_end = -1;
				int left_in_end = 0;
				for (int i = bits - 1; i >= 0; i--) {
					if (left_in_end == 0) {
						++cur_end;
						left_in_end = threads_per_end;
						if (ends_with_extra > 0) {
							++left_in_end;
							--ends_with_extra;
						}
					}
					ret[i] = (byte) (1 + cur_end);
					--left_in_end;
				}
			}
		}
		return ret;
	}
}
