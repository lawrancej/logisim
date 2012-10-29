/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.instance;

import java.awt.Font;
import java.awt.Graphics;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentUserEvent;
import com.cburch.logisim.comp.TextField;
import com.cburch.logisim.comp.TextFieldEvent;
import com.cburch.logisim.comp.TextFieldListener;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.tools.Caret;
import com.cburch.logisim.tools.SetAttributeAction;
import com.cburch.logisim.tools.TextEditable;
import static com.cburch.logisim.util.LocaleString.*;

public class InstanceTextField implements AttributeListener, TextFieldListener,
		TextEditable {
	private Canvas canvas;
	private InstanceComponent comp;
	private TextField field;
	private Attribute<String> labelAttr;
	private Attribute<Font> fontAttr;
	private int fieldX;
	private int fieldY;
	private int halign;
	private int valign;
	
	InstanceTextField(InstanceComponent comp) {
		this.comp = comp;
		this.field = null;
		this.labelAttr = null;
		this.fontAttr = null;
	}
	
	void update(Attribute<String> labelAttr, Attribute<Font> fontAttr,
			int x, int y, int halign, int valign) {
		boolean wasReg = shouldRegister();
		this.labelAttr = labelAttr;
		this.fontAttr = fontAttr;
		this.fieldX = x;
		this.fieldY = y;
		this.halign = halign;
		this.valign = valign;
		boolean shouldReg = shouldRegister();
		AttributeSet attrs = comp.getAttributeSet();
		if (!wasReg && shouldReg) attrs.addAttributeListener(this);
		if (wasReg && !shouldReg) attrs.removeAttributeListener(this);
		
		updateField(attrs);
	}
	
	private void updateField(AttributeSet attrs) {
		String text = attrs.getValue(labelAttr);
		if (text == null || text.equals("")) {
			if (field != null) {
				field.removeTextFieldListener(this);
				field = null;
			}
		} else {
			if (field == null) {
				createField(attrs, text);
			} else {
				Font font = attrs.getValue(fontAttr);
				if (font != null) field.setFont(font);
				field.setLocation(fieldX, fieldY, halign, valign);
				field.setText(text);
			}
		}
	}
	
	private void createField(AttributeSet attrs, String text) {
		Font font = attrs.getValue(fontAttr);
		field = new TextField(fieldX, fieldY, halign, valign, font);
		field.setText(text);
		field.addTextFieldListener(this);
	}
	
	private boolean shouldRegister() {
		return labelAttr != null || fontAttr != null;
	}
	
	Bounds getBounds(Graphics g) {
		return field == null ? Bounds.EMPTY_BOUNDS : field.getBounds(g);
	}
	
	void draw(Component comp, ComponentDrawContext context) {
		if (field != null) {
			Graphics g = context.getGraphics().create();
			field.draw(g);
			g.dispose();
		}
	}
	
	public void attributeListChanged(AttributeEvent e) { }
	
	public void attributeValueChanged(AttributeEvent e) {
		Attribute<?> attr = e.getAttribute();
		if (attr == labelAttr) {
			updateField(comp.getAttributeSet());
		} else if (attr == fontAttr) {
			if (field != null) field.setFont((Font) e.getValue());
		}
	}

	public void textChanged(TextFieldEvent e) {
		String prev = e.getOldText();
		String next = e.getText();
		if (!next.equals(prev)) {
			comp.getAttributeSet().setValue(labelAttr, next);
		}
	}
	
	public Action getCommitAction(Circuit circuit, String oldText,
			String newText) {
		SetAttributeAction act = new SetAttributeAction(circuit,
				__("changeLabelAction"));
		act.set(comp, labelAttr, newText);
		return act;
	}

	public Caret getTextCaret(ComponentUserEvent event) {
		canvas = event.getCanvas();
		Graphics g = canvas.getGraphics();

		// if field is absent, create it empty
		// and if it is empty, just return a caret at its beginning
		if (field == null) createField(comp.getAttributeSet(), "");
		String text = field.getText();
		if (text == null || text.equals("")) return field.getCaret(g, 0);

		Bounds bds = field.getBounds(g);
		if (bds.getWidth() < 4 || bds.getHeight() < 4) {
			Location loc = comp.getLocation();
			bds = bds.add(Bounds.create(loc).expand(2));
		}

		int x = event.getX();
		int y = event.getY();
		if (bds.contains(x, y)) return field.getCaret(g, x, y);
		else                    return null;
	}
}
