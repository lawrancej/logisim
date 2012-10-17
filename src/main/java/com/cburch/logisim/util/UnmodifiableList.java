/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

public class UnmodifiableList<E> extends AbstractList<E> {
	public static <E> List<E> create(E[] data) {
		if (data.length == 0) {
			return Collections.emptyList();
		} else {
			return new UnmodifiableList<E>(data);
		}
	}
	
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
