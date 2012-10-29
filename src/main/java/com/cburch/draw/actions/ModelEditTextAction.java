/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.actions;

import java.util.Collection;
import java.util.Collections;

import com.cburch.draw.model.CanvasModel;
import com.cburch.draw.model.CanvasObject;
import com.cburch.draw.shapes.Text;
import static com.cburch.logisim.util.LocaleString.*;

public class ModelEditTextAction extends ModelAction {
	private Text text;
	private String oldValue;
	private String newValue;
	
	public ModelEditTextAction(CanvasModel model, Text text, String newValue) {
		super(model);
		this.text = text;
		this.oldValue = text.getText();
		this.newValue = newValue;
	}
	
	@Override
	public Collection<CanvasObject> getObjects() {
		return Collections.singleton((CanvasObject) text);
	}

	@Override
	public String getName() {
		return _("actionEditText");
	}
	
	@Override
	void doSub(CanvasModel model) {
		model.setText(text, newValue);
	}
	
	@Override
	void undoSub(CanvasModel model) {
		model.setText(text, oldValue);
	}
}
