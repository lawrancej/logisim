/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools.key;

public interface KeyConfigurator {
	public KeyConfigurator clone();
	public KeyConfigurationResult keyEventReceived(KeyConfigurationEvent event);
}
