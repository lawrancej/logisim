/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import java.util.List;

import com.cburch.logisim.tools.FactoryDescription;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;

public class Memory extends Library {
	protected static final int DELAY = 5;
	
	private static FactoryDescription[] DESCRIPTIONS = {
		new FactoryDescription("D Flip-Flop", Strings.getter("dFlipFlopComponent"),
				"dFlipFlop.gif", "DFlipFlop"),
		new FactoryDescription("T Flip-Flop", Strings.getter("tFlipFlopComponent"),
				"tFlipFlop.gif", "TFlipFlop"),
		new FactoryDescription("J-K Flip-Flop", Strings.getter("jkFlipFlopComponent"),
				"jkFlipFlop.gif", "JKFlipFlop"),
		new FactoryDescription("S-R Flip-Flop", Strings.getter("srFlipFlopComponent"),
				"srFlipFlop.gif", "SRFlipFlop"),
		new FactoryDescription("Register", Strings.getter("registerComponent"),
				"register.gif", "Register"),
		new FactoryDescription("Counter", Strings.getter("counterComponent"),
				"counter.gif", "Counter"),
		new FactoryDescription("Shift Register", Strings.getter("shiftRegisterComponent"),
				"shiftreg.gif", "ShiftRegister"),
		new FactoryDescription("Random", Strings.getter("randomComponent"),
				"random.gif", "Random"),
		new FactoryDescription("RAM", Strings.getter("ramComponent"),
				"ram.gif", "Ram"),
		new FactoryDescription("ROM", Strings.getter("romComponent"),
				"rom.gif", "Rom"),
	};
	
	private List<Tool> tools = null;

	public Memory() { }

	@Override
	public String getName() { return "Memory"; }

	@Override
	public String getDisplayName() { return Strings.get("memoryLibrary"); }

	@Override
	public List<Tool> getTools() {
		if (tools == null) {
			tools = FactoryDescription.getTools(Memory.class, DESCRIPTIONS);
		}
		return tools;
	}
}
