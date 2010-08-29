/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.legacy;

import java.util.List;

import com.cburch.logisim.tools.FactoryDescription;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;


public class Legacy extends Library {
    private static FactoryDescription[] DESCRIPTIONS = {
        new FactoryDescription("Logisim 1.0 D Flip-Flop", Strings.getter("dFlipFlopComponent"),
                "dFlipFlop.gif", "DFlipFlop"),
        new FactoryDescription("Logisim 1.0 J-K Flip-Flop", Strings.getter("jkFlipFlopComponent"),
                "jkFlipFlop.gif", "JKFlipFlop"),
        new FactoryDescription("Logisim 1.0 Register", Strings.getter("registerComponent"),
                "register.gif", "Register"),
    };

    private List<Tool> tools = null;

    public Legacy() { }

    @Override
    public String getName() { return "Legacy"; }

    @Override
    public String getDisplayName() { return Strings.get("legacyLibrary"); }

    @Override
    public List<Tool> getTools() {
        if (tools == null) {
            tools = FactoryDescription.getTools(Legacy.class, DESCRIPTIONS);
        }
        return tools;
    }
}
