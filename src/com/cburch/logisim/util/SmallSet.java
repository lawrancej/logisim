/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SmallSet<E> extends AbstractSet<E> {
	private static final int HASH_POINT = 4;

	private class ArrayIterator implements Iterator<E> {
		int itVersion = version;
		Object myValues;
		int pos = 0; // position of next item to return
		boolean hasNext = true;
		boolean removeOk = false;
		
		private ArrayIterator() {
			myValues = values;
		}
		
		public boolean hasNext() {
			return hasNext;
		}
		
		public E next() {
			if (itVersion != version) {
				throw new ConcurrentModificationException();
			} else if (!hasNext) {
				throw new NoSuchElementException();
			} else if (size == 1) {
				pos = 1;
				hasNext = false;
				removeOk = true;
				@SuppressWarnings("unchecked")
				E ret = (E) myValues;
				return ret;
			} else {
				@SuppressWarnings("unchecked")
				E ret = ((E[]) myValues)[pos];
				++pos;
				hasNext = pos < size;
				removeOk = true;
				return ret;
			}
		}
		
		public void remove() {
			if (itVersion != version) {
				throw new ConcurrentModificationException();
			} else if (!removeOk) {
				throw new IllegalStateException();
			} else if (size == 1) {
				values = null;
				size = 0;
				++version;
				itVersion = version;
				removeOk = false;
			} else {
				Object[] vals = (Object[]) values;
				if (size == 2) {
					myValues = (pos == 2 ? vals[0] : vals[1]);
					values = myValues;
					size = 1;
				} else {
					for (int i = pos; i < size; i++) {
						vals[i - 1] = vals[i];
					}
					--pos;
					--size;
					vals[size] = null;
				}
				++version;
				itVersion = version;
				removeOk = false;
			}
		}
	}
	
	private int size = 0;
	private int version = 0;
	private Object values = null;
	
	public SmallSet() { }
	
	@Override
	public SmallSet<E> clone() {
		SmallSet<E> ret = new SmallSet<E>();
		ret.size = this.size;
		if (size == 1) {
			ret.values = this.values;
		} else if (size <= HASH_POINT) {
			Object[] oldVals = (Object[]) this.values;
			Object[] retVals = new Object[size];
			for (int i = size - 1; i >= 0; i--) retVals[i] = oldVals[i];
		} else {
			@SuppressWarnings("unchecked")
			HashSet<E> oldVals = (HashSet<E>) this.values;
			values = oldVals.clone();
		}
		return ret;
	}
	
	@Override
	public Object[] toArray() {
		Object vals = values;
		int sz = size;
		if (sz == 1) {
			return new Object[] { vals };
		} else if (sz <= HASH_POINT) {
			Object[] ret = new Object[sz];
			System.arraycopy(vals, 0, ret, 0, sz);
			return ret;
		} else {
			HashSet<?> hash = (HashSet<?>) vals;
			return hash.toArray();
		}
	}
	
	@Override
	public void clear() {
		size = 0;
		values = null;
		++version;
	}
	
	@Override
	public boolean isEmpty() {
		if (size <= HASH_POINT) {
			return size == 0;
		} else {
			return ((HashSet<?>) values).isEmpty();
		}
	}

	@Override
	public int size() {
		if (size <= HASH_POINT) {
			return size;
		} else {
			return ((HashSet<?>) values).size();
		}
	}
	
	@Override
	public boolean add(E value) {
		int oldSize = size;
		Object oldValues = values;
		int newVersion = version + 1;
		
		if (oldSize < 2) {
			if (oldSize == 0) {
				values = value;
				size = 1;
				version = newVersion;
				return true;
			} else {
				Object curValue = oldValues;
				if (curValue.equals(value)) {
					return false;
				} else {
					Object[] newValues = new Object[HASH_POINT];
					newValues[0] = values;
					newValues[1] = value;
					values = newValues;
					size = 2;
					version = newVersion;
					return true;
				}
			}
		} else if (oldSize <= HASH_POINT) {
			@SuppressWarnings("unchecked")
			E[] vals = (E[]) oldValues;
			for (int i = 0; i < oldSize; i++) {
				Object val = vals[i];
				boolean same = val == null ? value == null : val.equals(value);
				if (same) return false;
			}
			if (oldSize < HASH_POINT) {
				vals[oldSize] = value;
				size = oldSize + 1;
				version = newVersion;
				return true;
			} else {
				HashSet<E> newValues = new HashSet<E>();
				for (int i = 0; i < oldSize; i++) newValues.add(vals[i]);
				newValues.add(value);
				values = newValues;
				size = oldSize + 1;
				version = newVersion;
				return true;
			}
		} else {
			@SuppressWarnings("unchecked")
			HashSet<E> vals = (HashSet<E>) oldValues;
			if (vals.add(value)) {
				version = newVersion;
				return true;
			} else {
				return false;
			}
		}
	}
	
	@Override
	public boolean contains(Object value) {
		if (size <= 2) {
			if (size == 0) {
				return false;
			} else {
				return values.equals(value);
			}
		} else if (size <= HASH_POINT) {
			Object[] vals = (Object[]) values;
			for (int i = 0; i < size; i++) {
				if (vals[i].equals(value)) return true;
			}
			return false;
		} else {
			@SuppressWarnings("unchecked")
			HashSet<E> vals = (HashSet<E>) values;
			return vals.contains(value);
		}
	}

	@Override
	public Iterator<E> iterator() {
		if (size <= HASH_POINT) {
			if (size == 0) {
				return IteratorUtil.emptyIterator();
			} else {
				return new ArrayIterator();
			}
		} else {
			@SuppressWarnings("unchecked")
			HashSet<E> set = (HashSet<E>) values;
			return set.iterator();
		}
	}
	
	public static void main(String[] args) throws java.io.IOException {
		SmallSet<String> set = new SmallSet<String>();
		java.io.BufferedReader in = new java.io.BufferedReader(
				new java.io.InputStreamReader(System.in));
		while (true) {
			System.out.print(set.size() + ":"); //OK
			for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
				System.out.print(" " + it.next()); //OK
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
