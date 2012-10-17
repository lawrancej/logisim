/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.util.Collection;
import java.util.HashSet;

import com.cburch.logisim.comp.Component;

class SelectionSave {
	public static SelectionSave create(Selection sel) {
		SelectionSave save = new SelectionSave();
		
		Collection<Component> lifted = sel.getFloatingComponents();
		if (!lifted.isEmpty()) {
			save.floating = lifted.toArray(new Component[lifted.size()]);
		}

		Collection<Component> selected = sel.getAnchoredComponents();
		if (!selected.isEmpty()) {
			save.anchored = selected.toArray(new Component[selected.size()]);
		}
		
		return save;
	}
	
	private Component[] floating;
	private Component[] anchored;
	
	private SelectionSave() { }
	
	public Component[] getFloatingComponents() {
		return floating;
	}
	
	public Component[] getAnchoredComponents() {
		return anchored;
	}
	
	public boolean isSame(Selection sel) {
		return isSame(floating, sel.getFloatingComponents())
			&& isSame(anchored, sel.getAnchoredComponents());
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof SelectionSave) {
			SelectionSave o = (SelectionSave) other;
			return isSame(this.floating, o.floating)
				&& isSame(this.anchored, o.anchored);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		int ret = 0;
		if (floating != null) {
			for (Component c : floating) ret += c.hashCode();
		}
		if (anchored != null) {
			for (Component c : anchored) ret += c.hashCode();
		}
		return ret;
	}
	
	private static boolean isSame(Component[] save, Collection<Component> sel) {
		if (save == null) {
			return sel.isEmpty();
		} else {
			return toSet(save).equals(sel);
		}
	}
	
	private static boolean isSame(Component[] a, Component[] b) {
		if (a == null || a.length == 0) {
			return b == null || b.length == 0;
		} else if (b == null || b.length == 0) {
			return false;
		} else if (a.length != b.length) {
			return false;
		} else {
			return toSet(a).equals(toSet(b));
		}
	}
	
	private static HashSet<Component> toSet(Component[] comps) {
		HashSet<Component> ret = new HashSet<Component>(comps.length);
		for (Component c : comps) ret.add(c);
		return ret;
	}
}
