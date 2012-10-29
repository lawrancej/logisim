/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.util.List;

import com.cburch.logisim.tools.FactoryDescription;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;

import static com.cburch.logisim.util.LocaleString.*;

public class Memory extends Library {
	protected static final int DELAY = 5;
	
	private static FactoryDescription[] DESCRIPTIONS = {
		new FactoryDescription("D Flip-Flop", __("dFlipFlopComponent"),
				"dFlipFlop.gif", "DFlipFlop"),
		new FactoryDescription("T Flip-Flop", __("tFlipFlopComponent"),
				"tFlipFlop.gif", "TFlipFlop"),
		new FactoryDescription("J-K Flip-Flop", __("jkFlipFlopComponent"),
				"jkFlipFlop.gif", "JKFlipFlop"),
		new FactoryDescription("S-R Flip-Flop", __("srFlipFlopComponent"),
				"srFlipFlop.gif", "SRFlipFlop"),
		new FactoryDescription("Register", __("registerComponent"),
				"register.gif", "Register"),
		new FactoryDescription("Counter", __("counterComponent"),
				"counter.gif", "Counter"),
		new FactoryDescription("Shift Register", __("shiftRegisterComponent"),
				"shiftreg.gif", "ShiftRegister"),
		new FactoryDescription("Random", __("randomComponent"),
				"random.gif", "Random"),
		new FactoryDescription("RAM", __("ramComponent"), "ram.gif", "Ram"),
		new FactoryDescription("ROM", __("romComponent"), "rom.gif", "Rom"),
	};
	
	private List<Tool> tools = null;

	public Memory() { }

	@Override
	public String getName() { return "Memory"; }

	@Override
	public String getDisplayName() { return _("memoryLibrary"); }

	@Override
	public List<Tool> getTools() {
		if (tools == null) {
			tools = FactoryDescription.getTools(Memory.class, DESCRIPTIONS);
		}
		return tools;
	}
}
