/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.util.HashMap;
import java.util.Map;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.tools.key.KeyConfigurationEvent;
import com.cburch.logisim.tools.key.KeyConfigurationResult;
import static com.cburch.logisim.util.LocaleString.*;

public class ToolAttributeAction extends Action {
	public static Action create(Tool tool, Attribute<?> attr, Object value) {
		AttributeSet attrs = tool.getAttributeSet();
		KeyConfigurationEvent e = new KeyConfigurationEvent(0, attrs, null, null);
		KeyConfigurationResult r = new KeyConfigurationResult(e, attr, value);
		return new ToolAttributeAction(r);
	}

	public static Action create(KeyConfigurationResult results) {
		return new ToolAttributeAction(results);
	}
	
	private KeyConfigurationResult config;
	private Map<Attribute<?>,Object> oldValues;
	
	private ToolAttributeAction(KeyConfigurationResult config) {
		this.config = config;
		this.oldValues = new HashMap<Attribute<?>,Object>(2);
	}
	
	@Override
	public String getName() {
		return _("changeToolAttrAction");
	}

	@Override
	public void doIt(Project proj) {
		AttributeSet attrs = config.getEvent().getAttributeSet();
		Map<Attribute<?>,Object> newValues = config.getAttributeValues();
		Map<Attribute<?>,Object> oldValues = new HashMap<Attribute<?>,Object>(newValues.size());
		for (Map.Entry<Attribute<?>,Object> entry : newValues.entrySet()) {
			@SuppressWarnings("unchecked")
			Attribute<Object> attr = (Attribute<Object>) entry.getKey();
			oldValues.put(attr, attrs.getValue(attr));
			attrs.setValue(attr, entry.getValue());
		}
		this.oldValues = oldValues;
	}
	
	@Override
	public void undo(Project proj) {
		AttributeSet attrs = config.getEvent().getAttributeSet();
		Map<Attribute<?>,Object> oldValues = this.oldValues;
		for (Map.Entry<Attribute<?>,Object> entry : oldValues.entrySet()) {
			@SuppressWarnings("unchecked")
			Attribute<Object> attr = (Attribute<Object>) entry.getKey();
			attrs.setValue(attr, entry.getValue());
		}
	}

}
