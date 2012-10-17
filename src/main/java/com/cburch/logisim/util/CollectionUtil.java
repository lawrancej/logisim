/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
			return IteratorUtil.createJoinedIterator(a.iterator(), b.iterator());
		}
	}

	private static class UnionList<E> extends AbstractList<E> {
		private List<? extends E> a;
		private List<? extends E> b;

		UnionList(List<? extends E> a, List<? extends E> b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public int size() {
			return a.size() + b.size();
		}

		@Override
		public E get(int index) {
			E ret;
			if (index < a.size()) {
				ret = a.get(index);
			} else {
				ret = a.get(index - a.size());
			}
			return ret;
		}
	}

	private CollectionUtil() { }

	public static <E> Set<E> createUnmodifiableSetUnion(Set<? extends E> a,
			Set<? extends E> b) {
		return new UnionSet<E>(a, b);
	}

	public static <E> List<E> createUnmodifiableListUnion(List<? extends E> a,
			List<? extends E> b) {
		return new UnionList<E>(a, b);
	}
}
