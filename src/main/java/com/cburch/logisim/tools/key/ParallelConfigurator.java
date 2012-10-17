/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools.key;

import java.util.HashMap;

import com.cburch.logisim.data.Attribute;

public class ParallelConfigurator implements KeyConfigurator, Cloneable {
	public static ParallelConfigurator create(KeyConfigurator a, KeyConfigurator b) {
		return new ParallelConfigurator(new KeyConfigurator[] { a, b });
	}

	public static ParallelConfigurator create(KeyConfigurator[] configs) {
		return new ParallelConfigurator(configs);
	}
	
	private KeyConfigurator[] handlers;
	
	private ParallelConfigurator(KeyConfigurator[] handlers) {
		this.handlers = handlers;
	}
	
	@Override
	public ParallelConfigurator clone() {
		ParallelConfigurator ret;
		try {
			ret = (ParallelConfigurator) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		int len = this.handlers.length;
		ret.handlers = new KeyConfigurator[len];
		for (int i = 0; i < len; i++) {
			ret.handlers[i] = this.handlers[i].clone();
		}
		return ret;
	}
	
	public KeyConfigurationResult keyEventReceived(KeyConfigurationEvent event) {
		KeyConfigurator[] hs = handlers;
		if (event.isConsumed()) {
			return null;
		}
		KeyConfigurationResult first = null;
		HashMap<Attribute<?>,Object> map = null;
		for (int i = 0; i < hs.length; i++) {
			KeyConfigurationResult result = hs[i].keyEventReceived(event);
			if (result != null) {
				if (first == null) {
					first = result;
				} else if (map == null) {
					map = new HashMap<Attribute<?>,Object>(first.getAttributeValues());
					map.putAll(result.getAttributeValues());
				} else {
					map.putAll(result.getAttributeValues());
				}
			}
		}
		if (map != null) {
			return new KeyConfigurationResult(event, map);
		} else {
			return first;
		}
	}
}
