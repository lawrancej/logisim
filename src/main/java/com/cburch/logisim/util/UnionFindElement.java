/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

public interface UnionFindElement<E> {
	public E getUnionFindParent();
	public void setUnionFindParent(E value);
}
