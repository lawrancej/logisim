/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentHashQueue<E> {
	private static final int DONE_MARKER = Integer.MIN_VALUE / 2;
	
	private ConcurrentHashMap<E,Boolean> members;
	private ConcurrentLinkedQueue<E> queue;
	private AtomicInteger removeCount;

	public ConcurrentHashQueue() {
		members = new ConcurrentHashMap<E,Boolean>();
		queue = new ConcurrentLinkedQueue<E>();
		removeCount = new AtomicInteger(0);
	}
	
	public void setDone() {
		int num = removeCount.getAndSet(DONE_MARKER);
		if(num >= 0) {
			for(int i = 0; i < num; i++) {
				queue.add(null);
			}
		}
	}

	public void add(E value) {
		if(value == null) {
			throw new IllegalArgumentException("Cannot add null into ConcurrentHashQueue");
		}
		if(members.putIfAbsent(value, Boolean.TRUE) == null) {
			queue.add(value);
		}
	}
	
	public E remove() {
		int val = removeCount.getAndIncrement();
		if(val < 0) {
			removeCount.set(DONE_MARKER);
			return null;
		} else {
			E ret = queue.remove();
			if(ret == null) {
				return null;
			} else {
				removeCount.getAndDecrement();
				members.remove(ret);
				return ret;
			}
		}
	}
}
