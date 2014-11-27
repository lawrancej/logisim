/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.arith;

import java.util.List;

import com.cburch.logisim.tools.FactoryDescription;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import static com.cburch.logisim.util.LocaleString.*;

public class Arithmetic extends Library {
    private static FactoryDescription[] DESCRIPTIONS = {
        new FactoryDescription("Adder", getFromLocale("adderComponent"),
                "adder.svg", "Adder"),
        new FactoryDescription("Subtractor", getFromLocale("subtractorComponent"),
                "subtractor.svg", "Subtractor"),
        new FactoryDescription("Multiplier", getFromLocale("multiplierComponent"),
                "multiplier.svg", "Multiplier"),
        new FactoryDescription("Divider", getFromLocale("dividerComponent"),
                "divider.svg", "Divider"),
        new FactoryDescription("Negator", getFromLocale("negatorComponent"),
                "negator.svg", "Negator"),
        new FactoryDescription("Comparator", getFromLocale("comparatorComponent"),
                "comparator.svg", "Comparator"),
        new FactoryDescription("Shifter", getFromLocale("shifterComponent"),
                "shifter.svg", "Shifter"),
        new FactoryDescription("BitAdder", getFromLocale("bitAdderComponent"),
                "bitadder.svg", "BitAdder"),
        new FactoryDescription("BitFinder", getFromLocale("bitFinderComponent"),
                "bitfindr.svg", "BitFinder"),
    };

    private List<Tool> tools = null;

    public Arithmetic() { }

    @Override
    public String getName() { return "Arithmetic"; }

    @Override
    public String getDisplayName() { return getFromLocale("arithmeticLibrary"); }

    @Override
    public List<Tool> getTools() {
        if (tools == null) {
            tools = FactoryDescription.getTools(Arithmetic.class, DESCRIPTIONS);
        }
        return tools;
    }
}
