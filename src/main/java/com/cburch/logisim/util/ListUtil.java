/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class ListUtil {
	private static class JoinedList<E> extends AbstractList<E> {
		List<? extends E> a;
		List<? extends E> b;

		JoinedList(List<? extends E> a, List<? extends E> b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public int size() {
			return a.size() + b.size();
		}

		@Override
		public E get(int index) {
			if (index < a.size())   return a.get(index);
			else                    return b.get(index - a.size());
		}

		@Override
		public Iterator<E> iterator() {
			return IteratorUtil.createJoinedIterator(a.iterator(),
				b.iterator());
		}
				
	}

	private ListUtil() { }

	public static <E> List<E> joinImmutableLists(List<? extends E> a,
			List<? extends E> b) {
		return new JoinedList<E>(a, b);
	}
}
