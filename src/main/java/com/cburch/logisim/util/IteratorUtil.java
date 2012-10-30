/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteratorUtil {
	private static class IteratorUnion<E> implements Iterator<E> {
		Iterator<? extends E> cur;
		Iterator<? extends E> next;

		private IteratorUnion(Iterator<? extends E> cur, Iterator<? extends E> next) {
			this.cur = cur;
			this.next = next;
		}

		public E next() {
			if (!cur.hasNext()) {
				if (next == null) throw new NoSuchElementException();
				cur = next;
				if (!cur.hasNext()) throw new NoSuchElementException();
			}
			return cur.next();
		}

		public boolean hasNext() {
			return cur.hasNext() || (next != null && next.hasNext());
		}

		public void remove() {
			cur.remove();
		}
	}

	public static <E> Iterator<E> createJoinedIterator(Iterator<? extends E> i0,
			Iterator<? extends E> i1) {
		if (!i0.hasNext()) {
			@SuppressWarnings("unchecked")
			Iterator<E> ret = (Iterator<E>) i1;
			return ret;
		} else if (!i1.hasNext()) {
			@SuppressWarnings("unchecked")
			Iterator<E> ret = (Iterator<E>) i0;
			return ret;
		} else {
			return new IteratorUnion<E>(i0, i1);
		}
	}

}
