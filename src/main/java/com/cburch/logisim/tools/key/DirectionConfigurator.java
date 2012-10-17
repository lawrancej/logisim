/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools.key;

import java.awt.event.KeyEvent;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Direction;

public class DirectionConfigurator implements KeyConfigurator, Cloneable {
	private Attribute<Direction> attr;
	private int modsEx;
	
	public DirectionConfigurator(Attribute<Direction> attr, int modifiersEx) {
		this.attr = attr;
		this.modsEx = modifiersEx;
	}
	
	@Override
	public DirectionConfigurator clone() {
		try {
			return (DirectionConfigurator) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public KeyConfigurationResult keyEventReceived(KeyConfigurationEvent event) {
		if (event.getType() == KeyConfigurationEvent.KEY_PRESSED) {
			KeyEvent e = event.getKeyEvent();
			if (e.getModifiersEx() == modsEx) {
				Direction value = null;
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP: value = Direction.NORTH; break;
				case KeyEvent.VK_DOWN: value = Direction.SOUTH; break;
				case KeyEvent.VK_LEFT: value = Direction.WEST; break;
				case KeyEvent.VK_RIGHT: value = Direction.EAST; break;
				}
				if (value != null) {
					event.consume();
					return new KeyConfigurationResult(event, attr, value);
				}
			}
		}
		return null;
	}
}
