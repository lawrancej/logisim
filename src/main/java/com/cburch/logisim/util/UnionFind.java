/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class UnionFind<E extends UnionFindElement<E>> implements Iterable<E> {
	private HashMap<E,Integer> sizes;
	
	public UnionFind(Collection<E> values) {
		this.sizes = new HashMap<E,Integer>();
		Integer one = Integer.valueOf(1);
		for(E elt : values) {
			elt.setUnionFindParent(elt);
			sizes.put(elt, one);
		}
	}
	
	public int getRepresentativeCount() {
		return sizes.size();
	}
	
	public int getSetSize(E value) {
		E repr = findRepresentative(value);
		return sizes.get(repr);
	}
	
	public Iterator<E> iterator() {
		return sizes.keySet().iterator();
	}
	
	public Collection<E> getRepresentatives() {
		return Collections.unmodifiableSet(sizes.keySet());
	}
	
	public E findRepresentative(E value) {
		E parent = value.getUnionFindParent();
		if(parent == value) {
			return value;
		} else {
			parent = findRepresentative(parent);
			value.setUnionFindParent(parent);
			return parent;
		}
	}
	
	public void union(E value0, E value1) {
		E repr0 = findRepresentative(value0);
		E repr1 = findRepresentative(value1);
		if(repr0 != repr1) {
			int size0 = sizes.get(repr0);
			int size1 = sizes.get(repr1);
			if(size0 < size1) {
				sizes.remove(repr0);
				repr0.setUnionFindParent(repr1);
				value0.setUnionFindParent(repr1);
				sizes.put(repr1, size0 + size1);
			} else {
				sizes.remove(repr1);
				repr1.setUnionFindParent(repr0);
				value1.setUnionFindParent(repr0);
				sizes.put(repr0, size0 + size1);
			}
		}
	}
}
