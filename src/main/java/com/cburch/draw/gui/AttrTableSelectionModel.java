/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.gui;

import java.util.HashMap;
import java.util.Map;

import com.cburch.draw.actions.ModelChangeAttributeAction;
import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.canvas.Selection;
import com.cburch.draw.canvas.SelectionEvent;
import com.cburch.draw.canvas.SelectionListener;
import com.cburch.draw.model.AttributeMapKey;
import com.cburch.draw.model.CanvasModel;
import com.cburch.draw.model.CanvasObject;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.gui.generic.AttrTableSetException;
import com.cburch.logisim.gui.generic.AttributeSetTableModel;
import static com.cburch.logisim.util.LocaleString.*;

class AttrTableSelectionModel extends AttributeSetTableModel
		implements SelectionListener {
	private Canvas canvas;
	
	public AttrTableSelectionModel(Canvas canvas) {
		super(new SelectionAttributes(canvas.getSelection()));
		this.canvas = canvas;
		canvas.getSelection().addSelectionListener(this);
	}
	
	@Override
	public String getTitle() {
		Selection sel = canvas.getSelection();
		Class<? extends CanvasObject> commonClass = null;
		int commonCount = 0;
		CanvasObject firstObject = null;
		int totalCount = 0;
		for (CanvasObject obj : sel.getSelected()) {
			if (firstObject == null) {
				firstObject = obj;
				commonClass = obj.getClass();
				commonCount = 1;
			} else if (obj.getClass() == commonClass) {
				commonCount++;
			} else {
				commonClass = null;
			}
			totalCount++;
		}
		
		if (firstObject == null) {
			return null;
		} else if (commonClass == null) {
			return _("selectionVarious", "" + totalCount);
		} else if (commonCount == 1) {
			return _("selectionOne", firstObject.getDisplayName());
		} else {
			return _("selectionMultiple", firstObject.getDisplayName(),
					"" + commonCount);
		}
	}

	@Override
	public void setValueRequested(Attribute<Object> attr, Object value)
			throws AttrTableSetException {
		SelectionAttributes attrs = (SelectionAttributes) getAttributeSet();
		HashMap<AttributeMapKey, Object> oldVals;
		oldVals = new HashMap<AttributeMapKey, Object>();
		HashMap<AttributeMapKey, Object> newVals;
		newVals = new HashMap<AttributeMapKey, Object>();
		for (Map.Entry<AttributeSet, CanvasObject> ent : attrs.entries()) {
			AttributeMapKey key = new AttributeMapKey(attr, ent.getValue());
			oldVals.put(key, ent.getKey().getValue(attr));
			newVals.put(key, value);
		}
		CanvasModel model = canvas.getModel();
		canvas.doAction(new ModelChangeAttributeAction(model, oldVals, newVals));
	}

	//
	// SelectionListener method
	//
	public void selectionChanged(SelectionEvent e) {
		fireTitleChanged();
	}
}
