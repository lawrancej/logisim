/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.data;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class AttributeSetImpl extends AbstractAttributeSet {
	private static class Node {
		Attribute<?> attr;
		Object value;
		boolean is_read_only;
		Node next;

		Node(Attribute<?> attr, Object value, boolean is_read_only,
				Node next) {
			this.attr = attr;
			this.value = value;
			this.is_read_only = is_read_only;
			this.next = next;
		}

		Node(Node other) {
			this.attr = other.attr;
			this.value = other.value;
			this.is_read_only = other.is_read_only;
			this.next = other.next;
		}
	}

	private class AttrIterator implements Iterator<Attribute<?>> {
		Node n;

		AttrIterator(Node n) { this.n = n; }

		public boolean hasNext() {
			return n != null;
		}

		public Attribute<?> next() {
			Node ret = n;
			n = n.next;
			return ret.attr;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private class AttrList extends AbstractList<Attribute<?>> {
		@Override
		public Iterator<Attribute<?>> iterator() {
			return new AttrIterator(head);
		}

		@Override
		public Attribute<?> get(int i) {
			Node n = head;
			int remaining = i;
			while (remaining != 0 && n != null) {
				n = n.next;
				--remaining;
			}
			if (remaining != 0 || n == null) {
				throw new IndexOutOfBoundsException(i + " not in list "
					+ " [" + count + " elements]");
			}
			return n.attr;
		}

		@Override
		public boolean contains(Object o) {
			return indexOf(o) != -1;
		}

		@Override
		public int indexOf(Object o) {
			Node n = head;
			int ret = 0;
			while (n != null) {
				if (o.equals(n.attr)) return ret;
				n = n.next;
				++ret;
			}
			return -1;
		}

		@Override
		public int size() {
			return count;
		}
	}

	private AttrList list = new AttrList();
	private Node head = null;
	private Node tail = null;
	private int count = 0;

	public AttributeSetImpl() { }

	public AttributeSetImpl(Attribute<Object>[] attrs, Object[] values) {
		if (attrs.length != values.length) {
			throw new IllegalArgumentException("arrays must have same length");
		}

		for (int i = 0; i < attrs.length; i++) {
			addAttribute(attrs[i], values[i]);
		}
	}

	@Override
	protected void copyInto(AbstractAttributeSet destObj) {
		AttributeSetImpl dest = (AttributeSetImpl) destObj;
		if (this.head != null) {
			dest.head = new Node(head);
			Node copy_prev = dest.head;
			Node cur = this.head.next;
			while (cur != null) {
				Node copy_cur = new Node(cur);
				copy_prev.next = copy_cur;
				copy_prev = copy_cur;
				cur = cur.next;
			}
			dest.tail = copy_prev;
			dest.count = this.count;
		}
	}

	//
	// attribute access methods
	//
	@Override
	public List<Attribute<?>> getAttributes() {
		return list;
	}

	public <V> void addAttribute(Attribute<? super V> attr, V value) {
		if (attr == null) {
			throw new IllegalArgumentException("Adding null attribute");
		}
		if (findNode(attr) != null) {
			throw new IllegalArgumentException("Attribute " + attr
				+ " already created");
		}

		Node n = new Node(attr, value, false, null);
		if (head == null) head = n;
		else             tail.next = n;
		tail = n;
		++count;
		fireAttributeListChanged();
	}

	public void removeAttribute(Attribute<?> attr) {
		Node prev = null;
		Node n = head;
		while (n != null) {
			if (n.attr.equals(attr)) {
				if (tail == n)    tail = prev;
				if (prev == null) head = n.next;
				else             prev.next = n.next;
				--count;
				fireAttributeListChanged();
				return;
			}
			prev = n;
			n = n.next;
		}
		throw new IllegalArgumentException("Attribute " + attr
			+ " absent");
	}

	//
	// read-only methods
	//
	@Override
	public boolean isReadOnly(Attribute<?> attr) {
		Node n = findNode(attr);
		if (n == null) {
			throw new IllegalArgumentException("Unknown attribute "
				+ attr);
		}
		return n.is_read_only;
	}

	@Override
	public void setReadOnly(Attribute<?> attr, boolean value) {
		Node n = findNode(attr);
		if (n == null) {
			throw new IllegalArgumentException("Unknown attribute "
				+ attr);
		}
		n.is_read_only = value;
	}

	//
	// value access methods
	//
	@Override
	public <V> V getValue(Attribute<V> attr) {
		Node n = findNode(attr);
		if (n == null) {
			throw new IllegalArgumentException("Unknown attribute "
				+ attr);
		}
		@SuppressWarnings("unchecked")
		V ret = (V) n.value;
		return ret;
	}

	@Override
	public <V> void setValue(Attribute<V> attr, V value) {
		if (value instanceof String) {
			value = attr.parse((String) value);
		}

		Node n = findNode(attr);
		if (n == null) {
			throw new IllegalArgumentException("Unknown attribute "
				+ attr);
		}
		if (n.is_read_only) {
			throw new IllegalArgumentException("Attribute "
				+ attr + " is read-only");
		}
		if (value.equals(n.value)) {
			; // do nothing - why change what's already there?
		} else {
			n.value = value;
			fireAttributeValueChanged(attr, value);
		}
	}

	//
	// private helper methods
	//
	private Node findNode(Attribute<?> attr) {
		for (Node n = head; n != null; n = n.next) {
			if (n.attr.equals(attr)) return n;
		}
		return null;
	}
}
