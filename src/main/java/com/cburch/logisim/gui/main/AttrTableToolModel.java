/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.gui.generic.AttributeSetTableModel;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.Tool;
import static com.cburch.logisim.util.LocaleString.*;

public class AttrTableToolModel extends AttributeSetTableModel {
	Project proj;
	Tool tool;
	
	public AttrTableToolModel(Project proj, Tool tool) {
		super(tool.getAttributeSet());
		this.proj = proj;
		this.tool = tool;
	}
	
	@Override
	public String getTitle() {
		return _("toolAttrTitle", tool.getDisplayName());
	}
	
	public Tool getTool() {
		return tool;
	}
	
	@Override
	public void setValueRequested(Attribute<Object> attr, Object value) {
		proj.doAction(ToolAttributeAction.create(tool, attr, value));
	}
}
