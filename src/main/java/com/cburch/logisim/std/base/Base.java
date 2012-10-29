/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.base;

import java.util.Arrays;
import java.util.List;

import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.MenuTool;
import com.cburch.logisim.tools.PokeTool;
import com.cburch.logisim.tools.SelectTool;
import com.cburch.logisim.tools.TextTool;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.EditTool;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.tools.WiringTool;
import static com.cburch.logisim.util.LocaleString.*;

public class Base extends Library {
	private List<Tool> tools = null;

	public Base() {
		SelectTool select = new SelectTool();
		WiringTool wiring = new WiringTool();
		
		tools = Arrays.asList(new Tool[] {
			new PokeTool(),
			new EditTool(select, wiring),
			select,
			wiring,
			new TextTool(),
			new MenuTool(),
			new AddTool(Text.FACTORY),
		});
	}

	@Override
	public String getName() { return "Base"; }

	@Override
	public String getDisplayName() { return _("baseLibrary"); }

	@Override
	public List<Tool> getTools() {
		return tools;
	}
}
