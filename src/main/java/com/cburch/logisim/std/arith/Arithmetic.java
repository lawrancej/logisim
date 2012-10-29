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
		new FactoryDescription("Adder", __("adderComponent"),
				"adder.gif", "Adder"),
		new FactoryDescription("Subtractor", __("subtractorComponent"),
				"subtractor.gif", "Subtractor"),
		new FactoryDescription("Multiplier", __("multiplierComponent"),
				"multiplier.gif", "Multiplier"),
		new FactoryDescription("Divider", __("dividerComponent"),
				"divider.gif", "Divider"),
		new FactoryDescription("Negator", __("negatorComponent"),
				"negator.gif", "Negator"),
		new FactoryDescription("Comparator", __("comparatorComponent"),
				"comparator.gif", "Comparator"),
		new FactoryDescription("Shifter", __("shifterComponent"),
				"shifter.gif", "Shifter"),
		new FactoryDescription("BitAdder", __("bitAdderComponent"),
				"bitadder.gif", "BitAdder"),
		new FactoryDescription("BitFinder", __("bitFinderComponent"),
				"bitfindr.gif", "BitFinder"),
	};
	
	private List<Tool> tools = null;

	public Arithmetic() { }

	@Override
	public String getName() { return "Arithmetic"; }

	@Override
	public String getDisplayName() { return _("arithmeticLibrary"); }

	@Override
	public List<Tool> getTools() {
		if (tools == null) {
			tools = FactoryDescription.getTools(Arithmetic.class, DESCRIPTIONS);
		}
		return tools;
	}
}
