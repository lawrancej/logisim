/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections15.iterators.IteratorChain;

public class CollectionUtil {
	private static class UnionSet<E> extends AbstractSet<E> {
		private Set<? extends E> a;
		private Set<? extends E> b;

		UnionSet(Set<? extends E> a, Set<? extends E> b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public int size() {
			return a.size() + b.size();
		}

		@Override
		public Iterator<E> iterator() {
			return new IteratorChain<E>(a.iterator(), b.iterator());
		}
	}

	private CollectionUtil() { }

	public static <E> Set<E> createUnmodifiableSetUnion(Set<? extends E> a,
			Set<? extends E> b) {
		return new UnionSet<E>(a, b);
	}
}
