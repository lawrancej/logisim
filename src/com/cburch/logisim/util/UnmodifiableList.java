/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.util.AbstractList;

public class UnmodifiableList<E> extends AbstractList<E> {
	private E[] data;
	
	public UnmodifiableList(E[] data) {
		this.data = data;
	}

	@Override
	public E get(int index) {
		return data[index];
	}

	@Override
	public int size() {
		return data.length;
	}
}
