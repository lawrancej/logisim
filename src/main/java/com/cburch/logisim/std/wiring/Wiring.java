/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.wiring;

import java.util.ArrayList;
import java.util.List;

import com.cburch.logisim.circuit.SplitterFactory;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.FactoryDescription;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;

public class Wiring extends Library {

	static final AttributeOption GATE_TOP_LEFT
		= new AttributeOption("tl", Strings.getter("wiringGateTopLeftOption"));
	static final AttributeOption GATE_BOTTOM_RIGHT
		= new AttributeOption("br", Strings.getter("wiringGateBottomRightOption"));
	static final Attribute<AttributeOption> ATTR_GATE = Attributes.forOption("gate",
			Strings.getter("wiringGateAttr"),
			new AttributeOption[] { GATE_TOP_LEFT, GATE_BOTTOM_RIGHT });

	private static Tool[] ADD_TOOLS = {
		new AddTool(SplitterFactory.instance),
		new AddTool(Pin.FACTORY),
		new AddTool(Probe.FACTORY),
		new AddTool(Tunnel.FACTORY),
		new AddTool(PullResistor.FACTORY),
		new AddTool(Clock.FACTORY),
		new AddTool(Constant.FACTORY),
	};
	
	private static FactoryDescription[] DESCRIPTIONS = {
		new FactoryDescription("Power", Strings.getter("powerComponent"),
				"power.gif", "Power"),
		new FactoryDescription("Ground", Strings.getter("groundComponent"),
				"ground.gif", "Ground"),
		new FactoryDescription("Transistor", Strings.getter("transistorComponent"),
				"trans0.gif", "Transistor"),
		new FactoryDescription("Transmission Gate", Strings.getter("transmissionGateComponent"),
				"transmis.gif", "TransmissionGate"),
		new FactoryDescription("Bit Extender", Strings.getter("extenderComponent"),
				"extender.gif", "BitExtender"),
	};

	private List<Tool> tools = null;

	public Wiring() { }

	@Override
	public String getName() { return "Wiring"; }

	@Override
	public String getDisplayName() { return Strings.get("wiringLibrary"); }

	@Override
	public List<Tool> getTools() {
		if (tools == null) {
			List<Tool> ret = new ArrayList<Tool>(ADD_TOOLS.length + DESCRIPTIONS.length);
			for (Tool a : ADD_TOOLS) {
				ret.add(a);
			}
			ret.addAll(FactoryDescription.getTools(Wiring.class, DESCRIPTIONS));
			tools = ret;
		}
		return tools;
	}
}
