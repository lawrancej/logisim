/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;

public class EventSourceWeakSupport<L> implements Iterable<L> {
    private LinkedList<WeakReference<L>> listeners
        = new LinkedList<WeakReference<L>>();

    public EventSourceWeakSupport() { }
    
    public void add(L listener) {
        listeners.add(new WeakReference<L>(listener));
    }
    
    public void remove(L listener) {
        for (Iterator<WeakReference<L>> it = listeners.iterator(); it.hasNext(); ) {
            L l = it.next().get();
            if (l == null || l == listener) it.remove();
        }
    }
    
    public int size() {
        for (Iterator<WeakReference<L>> it = listeners.iterator(); it.hasNext(); ) {
            L l = it.next().get();
            if (l == null) it.remove();
        }
        return listeners.size();
    }
    
    public Iterator<L> iterator() {
        // copy elements into another list in case any event handlers
        // want to add a listener
        ArrayList<L> ret = new ArrayList<L>(listeners.size());
        for (Iterator<WeakReference<L>> it = listeners.iterator(); it.hasNext(); ) {
            L l = it.next().get();
            if (l == null) {
                it.remove();
            } else {
                ret.add(l);
            }
        }
        return ret.iterator();
    }
}
