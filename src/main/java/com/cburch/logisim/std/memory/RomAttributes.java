/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

import com.cburch.logisim.data.AbstractAttributeSet;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.gui.hex.HexFrame;
import com.cburch.logisim.proj.Project;

class RomAttributes extends AbstractAttributeSet {
	private static List<Attribute<?>> ATTRIBUTES = Arrays.asList(new Attribute<?>[] {
			Mem.ADDR_ATTR, Mem.DATA_ATTR, Rom.CONTENTS_ATTR
		});
	
	private static WeakHashMap<MemContents,RomContentsListener> listenerRegistry
		= new WeakHashMap<MemContents,RomContentsListener>();
	private static WeakHashMap<MemContents,HexFrame> windowRegistry
		= new WeakHashMap<MemContents,HexFrame>();

	static void register(MemContents value, Project proj) {
		if (proj == null || listenerRegistry.containsKey(value)) return;
		RomContentsListener l = new RomContentsListener(proj);
		value.addHexModelListener(l);
		listenerRegistry.put(value, l);
	}
	
	static HexFrame getHexFrame(MemContents value, Project proj) {
		synchronized(windowRegistry) {
			HexFrame ret = windowRegistry.get(value);
			if (ret == null) {
				ret = new HexFrame(proj, value);
				windowRegistry.put(value, ret);
			}
			return ret;
		}
	}

	private BitWidth addrBits = BitWidth.create(8);
	private BitWidth dataBits = BitWidth.create(8);
	private MemContents contents;
	
	RomAttributes() {
		contents = MemContents.create(addrBits.getWidth(), dataBits.getWidth());
	}
	
	void setProject(Project proj) {
		register(contents, proj);
	}
	
	@Override
	protected void copyInto(AbstractAttributeSet dest) {
		RomAttributes d = (RomAttributes) dest;
		d.addrBits = addrBits;
		d.dataBits = dataBits;
		d.contents = contents.clone();
	}
	
	@Override
	public List<Attribute<?>> getAttributes() {
		return ATTRIBUTES;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <V> V getValue(Attribute<V> attr) {
		if (attr == Mem.ADDR_ATTR) return (V) addrBits;
		if (attr == Mem.DATA_ATTR) return (V) dataBits;
		if (attr == Rom.CONTENTS_ATTR) return (V) contents;
		return null;
	}
	
	@Override
	public <V> void setValue(Attribute<V> attr, V value) {
		if (attr == Mem.ADDR_ATTR) {
			addrBits = (BitWidth) value;
			contents.setDimensions(addrBits.getWidth(), dataBits.getWidth());
		} else if (attr == Mem.DATA_ATTR) {
			dataBits = (BitWidth) value;
			contents.setDimensions(addrBits.getWidth(), dataBits.getWidth());
		} else if (attr == Rom.CONTENTS_ATTR) {
			contents = (MemContents) value;
		}
		fireAttributeValueChanged(attr, value);
	}
}
