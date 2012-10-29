/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.opts;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.file.MouseMappings;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.Tool;
import static com.cburch.logisim.util.LocaleString.*;

class OptionsActions {
	private OptionsActions() { }

	public static Action setAttribute(AttributeSet attrs, Attribute<?> attr, Object value) {
		Object oldValue = attrs.getValue(attr);
		if (!oldValue.equals(value)) {
			return new SetAction(attrs, attr, value);
		} else {
			return null;
		}
	}
	
	public static Action setMapping(MouseMappings mm, Integer mods,
			Tool tool) {
		return new SetMapping(mm, mods, tool);
	}

	public static Action removeMapping(MouseMappings mm, Integer mods) {
		return new RemoveMapping(mm, mods);
	}
	
	private static class SetAction extends Action {
		private AttributeSet attrs;
		private Attribute<Object> attr;
		private Object newval;
		private Object oldval;

		SetAction(AttributeSet attrs, Attribute<?> attr,
				Object value) {
			@SuppressWarnings("unchecked")
			Attribute<Object> a = (Attribute<Object>) attr;
			this.attrs = attrs;
			this.attr = a;
			this.newval = value;
		}

		@Override
		public String getName() {
			return _("setOptionAction", attr.getDisplayName());
		}

		@Override
		public void doIt(Project proj) {
			oldval = attrs.getValue(attr);
			attrs.setValue(attr, newval);
		}

		@Override
		public void undo(Project proj) {
			attrs.setValue(attr, oldval);
		}
	}

	private static class SetMapping extends Action {
		MouseMappings mm;
		Integer mods;
		Tool oldtool;
		Tool tool;

		SetMapping(MouseMappings mm, Integer mods, Tool tool) {
			this.mm = mm;
			this.mods = mods;
			this.tool = tool;
		}

		@Override
		public String getName() {
			return _("addMouseMappingAction");
		}

		@Override
		public void doIt(Project proj) {
			oldtool = mm.getToolFor(mods);
			mm.setToolFor(mods, tool);
		}

		@Override
		public void undo(Project proj) {
			mm.setToolFor(mods, oldtool);
		}
	}

	private static class RemoveMapping extends Action {
		MouseMappings mm;
		Integer mods;
		Tool oldtool;

		RemoveMapping(MouseMappings mm, Integer mods) {
			this.mm = mm;
			this.mods = mods;
		}

		@Override
		public String getName() {
			return _("removeMouseMappingAction");
		}

		@Override
		public void doIt(Project proj) {
			oldtool = mm.getToolFor(mods);
			mm.setToolFor(mods, null);
		}

		@Override
		public void undo(Project proj) {
			mm.setToolFor(mods, oldtool);
		}
	}
}
