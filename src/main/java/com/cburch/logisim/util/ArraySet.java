/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArraySet<E> extends AbstractSet<E> {
	private static final Object[] EMPTY_ARRAY = new Object[0];
	
	private class ArrayIterator implements Iterator<E> {
		int itVersion = version;
		int pos = 0; // position of next item to return
		boolean hasNext = values.length > 0;
		boolean removeOk = false;
		
		public boolean hasNext() {
			return hasNext;
		}
		
		public E next() {
			if (itVersion != version) {
				throw new ConcurrentModificationException();
			} else if (!hasNext) {
				throw new NoSuchElementException();
			} else {
				@SuppressWarnings("unchecked")
				E ret = (E) values[pos];
				++pos;
				hasNext = pos < values.length;
				removeOk = true;
				return ret;
			}
		}
		
		public void remove() {
			if (itVersion != version) {
				throw new ConcurrentModificationException();
			} else if (!removeOk) {
				throw new IllegalStateException();
			} else if (values.length == 1) {
				values = EMPTY_ARRAY;
				++version;
				itVersion = version;
				removeOk = false;
			} else {
				Object[] newValues = new Object[values.length - 1];
				if (pos > 1) {
					System.arraycopy(values, 0, newValues, 0, pos - 1);
				}
				if (pos < values.length) {
					System.arraycopy(values, pos, newValues, pos - 1, values.length - pos);
				}
				values = newValues;
				--pos;
				++version;
				itVersion = version;
				removeOk = false;
			}
		}
	}
	
	private int version = 0;
	private Object[] values = EMPTY_ARRAY;
	
	public ArraySet() { }
	
	@Override
	public Object[] toArray() {
		return values;
	}
	
	@Override
	public Object clone() {
		ArraySet<E> ret = new ArraySet<E>();
		if (this.values == EMPTY_ARRAY) {
			ret.values = EMPTY_ARRAY;
		} else {
			ret.values = this.values.clone();
		}
		return ret;
	}
	
	@Override
	public void clear() {
		values = EMPTY_ARRAY;
		++version;
	}
	
	@Override
	public boolean isEmpty() {
		return values.length == 0;
	}

	@Override
	public int size() {
		return values.length;
	}
	
	@Override
	public boolean add(Object value) {
		int n = values.length;
		for (int i = 0; i < n; i++) {
			if (values[i].equals(value)) return false;
		}
		
		Object[] newValues = new Object[n + 1];
		System.arraycopy(values, 0, newValues, 0, n);
		newValues[n] = value;
		values = newValues;
		++version;
		return true;
	}
	
	@Override
	public boolean contains(Object value) {
		for (int i = 0, n = values.length; i < n; i++) {
			if (values[i].equals(value)) return true;
		}
		return false;
	}

	@Override
	public Iterator<E> iterator() {
		return new ArrayIterator();
	}
	
	public static void main(String[] args) throws java.io.IOException {
		ArraySet<String> set = new ArraySet<String>();
		java.io.BufferedReader in = new java.io.BufferedReader(
				new java.io.InputStreamReader(System.in));
		while (true) {
			System.out.print(set.size() + ":"); //OK
			for (String str : set) {
				System.out.print(" " + str); //OK
			}
			System.out.println(); //OK
			System.out.print("> "); //OK
			String cmd = in.readLine();
			if (cmd == null) break;
			cmd = cmd.trim();
			if (cmd.equals("")) {
				;
			} else if (cmd.startsWith("+")) {
				set.add(cmd.substring(1));
			} else if (cmd.startsWith("-")) {
				set.remove(cmd.substring(1));
			} else if (cmd.startsWith("?")) {
				boolean ret = set.contains(cmd.substring(1));
				System.out.println("  " + ret); //OK
			} else {
				System.out.println("unrecognized command"); //OK
			}
		}
	}
}
