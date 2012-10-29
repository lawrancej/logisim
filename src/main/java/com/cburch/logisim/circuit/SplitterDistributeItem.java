/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import com.cburch.logisim.proj.Project;
import com.cburch.logisim.util.StringGetter;
import static com.cburch.logisim.util.LocaleString.*;

class SplitterDistributeItem extends JMenuItem implements ActionListener {
	private Project proj;
	private Splitter splitter;
	private int order;
	
	public SplitterDistributeItem(Project proj, Splitter splitter, int order) {
		this.proj = proj;
		this.splitter = splitter;
		this.order = order;
		addActionListener(this);
		
		SplitterAttributes attrs = (SplitterAttributes) splitter.getAttributeSet();
		byte[] actual = attrs.bit_end;
		byte[] desired = SplitterAttributes.computeDistribution(attrs.fanout,
				actual.length, order);
		boolean same = actual.length == desired.length;
		for (int i = 0; same && i < desired.length; i++) {
			if (actual[i] != desired[i]) {
				same = false;
			}
		}
		setEnabled(!same);
		setText(toGetter().toString());
	}
	
	private StringGetter toGetter() {
		if (order > 0) {
			return __("splitterDistributeAscending");
		} else {
			return __("splitterDistributeDescending");
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		SplitterAttributes attrs = (SplitterAttributes) splitter.getAttributeSet();
		byte[] actual = attrs.bit_end;
		byte[] desired = SplitterAttributes.computeDistribution(attrs.fanout,
				actual.length, order);
		CircuitMutation xn = new CircuitMutation(proj.getCircuitState().getCircuit());
		for (int i = 0, n = Math.min(actual.length, desired.length); i < n; i++) {
			if (actual[i] != desired[i]) {
				xn.set(splitter, attrs.getBitOutAttribute(i),
						Integer.valueOf(desired[i]));
			}
		}
		proj.doAction(xn.toAction(toGetter()));
	}
}
