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
                "dFlipFlop.svg", "DFlipFlop"),
        new FactoryDescription("T Flip-Flop", __("tFlipFlopComponent"),
                "tFlipFlop.svg", "TFlipFlop"),
        new FactoryDescription("J-K Flip-Flop", __("jkFlipFlopComponent"),
                "jkFlipFlop.svg", "JKFlipFlop"),
        new FactoryDescription("S-R Flip-Flop", __("srFlipFlopComponent"),
                "srFlipFlop.svg", "SRFlipFlop"),
        new FactoryDescription("Register", __("registerComponent"),
                "register.svg", "Register"),
        new FactoryDescription("Counter", __("counterComponent"),
                "counter.svg", "Counter"),
        new FactoryDescription("Shift Register", __("shiftRegisterComponent"),
                "shiftreg.svg", "ShiftRegister"),
        new FactoryDescription("Random", __("randomComponent"),
                "random.svg", "Random"),
        new FactoryDescription("RAM", __("ramComponent"), "ram.svg", "Ram"),
        new FactoryDescription("ROM", __("romComponent"), "rom.svg", "Rom"),
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
