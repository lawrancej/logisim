/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools.key;

public class JoinedConfigurator implements KeyConfigurator, Cloneable {
	public static JoinedConfigurator create(KeyConfigurator a, KeyConfigurator b) {
		return new JoinedConfigurator(new KeyConfigurator[] { a, b });
	}

	public static JoinedConfigurator create(KeyConfigurator[] configs) {
		return new JoinedConfigurator(configs);
	}
	
	private KeyConfigurator[] handlers;
	
	private JoinedConfigurator(KeyConfigurator[] handlers) {
		this.handlers = handlers;
	}
	
	@Override
	public JoinedConfigurator clone() {
		JoinedConfigurator ret;
		try {
			ret = (JoinedConfigurator) super.clone();
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
		for (int i = 0; i < hs.length; i++) {
			KeyConfigurationResult result = hs[i].keyEventReceived(event);
			if (result != null || event.isConsumed()) {
				return result;
			}
		}
		return null;
	}
}
