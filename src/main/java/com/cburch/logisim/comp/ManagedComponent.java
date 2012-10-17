/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.comp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.util.EventSourceWeakSupport;

public abstract class ManagedComponent extends AbstractComponent {
	private EventSourceWeakSupport<ComponentListener> listeners
		= new EventSourceWeakSupport<ComponentListener>();
	private Location loc;
	private AttributeSet attrs;
	private ArrayList<EndData> ends;
	private List<EndData> endsView;
	private Bounds bounds = null;

	public ManagedComponent(Location loc, AttributeSet attrs, int num_ends) {
		this.loc = loc;
		this.attrs = attrs;
		this.ends = new ArrayList<EndData>(num_ends);
		this.endsView = Collections.unmodifiableList(ends);
	}

	//
	// abstract AbstractComponent methods
	//
	@Override
	public abstract ComponentFactory getFactory();

	public void addComponentListener(ComponentListener l) {
		listeners.add(l);
	}

	public void removeComponentListener(ComponentListener l) {
		listeners.remove(l);
	}

	protected void fireEndChanged(ComponentEvent e) {
		ComponentEvent copy = null;
		for (ComponentListener l : listeners) {
			if (copy == null) {
				copy = new ComponentEvent(e.getSource(),
						Collections.singletonList(e.getOldData()),
						Collections.singletonList(e.getData()));
			}
			l.endChanged(copy);
		}
	}

	protected void fireEndsChanged(List<EndData> oldEnds, List<EndData> newEnds) {
		ComponentEvent e = null;
		for (ComponentListener l : listeners) {
			if (e == null) e = new ComponentEvent(this, oldEnds, newEnds);
			l.endChanged(e);
		}
	}

	protected void fireComponentInvalidated(ComponentEvent e) {
		for (ComponentListener l : listeners) {
			l.componentInvalidated(e);
		}
	}

	@Override
	public Location getLocation() {
		return loc;
	}

	public AttributeSet getAttributeSet() {
		return attrs;
	}

	@Override
	public Bounds getBounds() {
		if (bounds == null) {
			Location loc = getLocation();
			Bounds offBounds = getFactory().getOffsetBounds(getAttributeSet());
			bounds = offBounds.translate(loc.getX(), loc.getY());
		}
		return bounds;
	}
	
	protected void recomputeBounds() {
		bounds = null;
	}

	@Override
	public List<EndData> getEnds() {
		return endsView;
	}
	
	public int getEndCount() {
		return ends.size();
	}

	@Override
	public abstract void propagate(CircuitState state);

	//
	// methods for altering data
	//
	public void clearManager() {
		for (EndData end : ends) {
			fireEndChanged(new ComponentEvent(this, end, null));
		}
		ends.clear();
		bounds = null;
	}

	public void setBounds(Bounds bounds) {
		this.bounds = bounds;
	}
	
	public void setAttributeSet(AttributeSet value) {
		attrs = value;
	}
	
	public void removeEnd(int index) {
		ends.remove(index);
	}

	public void setEnd(int i, EndData data) {
		if (i == ends.size()) {
			ends.add(data);
			fireEndChanged(new ComponentEvent(this, null, data));
		} else {
			EndData old = ends.get(i);
			if (old == null || !old.equals(data)) {
				ends.set(i, data);
				fireEndChanged(new ComponentEvent(this, old, data));
			}
		}
	}

	public void setEnd(int i, Location end, BitWidth width, int type) {
		setEnd(i, new EndData(end, width, type));
	}

	public void setEnd(int i, Location end, BitWidth width, int type, boolean exclusive) {
		setEnd(i, new EndData(end, width, type, exclusive));
	}
	
	public void setEnds(EndData[] newEnds) {
		List<EndData> oldEnds = ends;
		int minLen = Math.min(oldEnds.size(), newEnds.length);
		ArrayList<EndData> changesOld = new ArrayList<EndData>();
		ArrayList<EndData> changesNew = new ArrayList<EndData>();
		for (int i = 0; i < minLen; i++) {
			EndData old = oldEnds.get(i);
			if (newEnds[i] != null && !newEnds[i].equals(old)) {
				changesOld.add(old);
				changesNew.add(newEnds[i]);
				oldEnds.set(i, newEnds[i]);
			}
		}
		for (int i = oldEnds.size() - 1; i >= minLen; i--) {
			changesOld.add(oldEnds.remove(i));
			changesNew.add(null);
		}
		for (int i = minLen; i < newEnds.length; i++) {
			oldEnds.add(newEnds[i]);
			changesOld.add(null);
			changesNew.add(newEnds[i]);
		}
		fireEndsChanged(changesOld, changesNew);
	}

	public Location getEndLocation(int i) {
		return getEnd(i).getLocation();
	}

	//
	// user interface methods
	//
	public void expose(ComponentDrawContext context) {
		Bounds bounds = getBounds();
		java.awt.Component dest = context.getDestination();
		if (bounds != null) {
			dest.repaint(bounds.getX() - 5, bounds.getY() - 5,
				bounds.getWidth() + 10, bounds.getHeight() + 10);
		}
	}
	
	public Object getFeature(Object key) {
		return null;
	}
}
