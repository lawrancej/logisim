/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.comp;

import java.awt.Graphics;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.LinkedList;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.util.GraphicsUtil;

public class TextField {
	public static final int H_LEFT    = GraphicsUtil.H_LEFT;
	public static final int H_CENTER    = GraphicsUtil.H_CENTER;
	public static final int H_RIGHT  = GraphicsUtil.H_RIGHT;
	public static final int V_TOP      = GraphicsUtil.V_TOP;
	public static final int V_CENTER    = GraphicsUtil.V_CENTER;
	public static final int V_CENTER_OVERALL    = GraphicsUtil.V_CENTER_OVERALL;
	public static final int V_BASELINE  = GraphicsUtil.V_BASELINE;
	public static final int V_BOTTOM    = GraphicsUtil.V_BOTTOM;

	private int x;
	private int y;
	private int halign;
	private int valign;
	private Font font;
	private String text = "";
	private LinkedList<TextFieldListener> listeners = new LinkedList<TextFieldListener>();

	public TextField(int x, int y, int halign, int valign) {
		this(x, y, halign, valign, null);
	}

	public TextField(int x, int y, int halign, int valign,
			Font font) {
		this.x = x;
		this.y = y;
		this.halign = halign;
		this.valign = valign;
		this.font = font;
	}

	//
	// listener methods
	//
	public void addTextFieldListener(TextFieldListener l) {
		listeners.add(l);
	}

	public void removeTextFieldListener(TextFieldListener l) {
		listeners.remove(l);
	}

	public void fireTextChanged(TextFieldEvent e) {
		for (TextFieldListener l : new ArrayList<TextFieldListener>(listeners)) {
			l.textChanged(e);
		}
	}

	//
	// access methods
	//
	public int getX() { return x; }
	public int getY() { return y; }
	public int getHAlign() { return halign; }
	public int getVAlign() { return valign; }
	public Font getFont() { return font; }
	public String getText() { return text; }

	public TextFieldCaret getCaret(Graphics g, int pos) {
		return new TextFieldCaret(this, g, pos);
	}

	//
	// modification methods
	//
	public void setText(String text) {
		if (!text.equals(this.text)) {
			TextFieldEvent e = new TextFieldEvent(this, this.text, text);
			this.text = text;
			fireTextChanged(e);
		}
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setLocation(int x, int y, int halign, int valign) {
		this.x = x;
		this.y = y;
		this.halign = halign;
		this.valign = valign;
	}

	public void setAlign(int halign, int valign) {
		this.halign = halign;
		this.valign = valign;
	}

	public void setHorzAlign(int halign) {
		this.halign = halign;
	}

	public void setVertAlign(int valign) {
		this.valign = valign;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	//
	// graphics methods
	//
	public TextFieldCaret getCaret(Graphics g, int x, int y) {
		return new TextFieldCaret(this, g, x, y);
	}

	public Bounds getBounds(Graphics g) {
		int x = this.x;
		int y = this.y;
		FontMetrics fm;
		if (font == null)   fm = g.getFontMetrics();
		else                fm = g.getFontMetrics(font);
		int width = fm.stringWidth(text);
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		switch (halign) {
		case TextField.H_CENTER:    x -= width / 2; break;
		case TextField.H_RIGHT:  x -= width; break;
		default:                    break;
		}
		switch (valign) {
		case TextField.V_TOP:      y += ascent; break;
		case TextField.V_CENTER:    y += ascent/ 2; break;
		case TextField.V_CENTER_OVERALL: y += (ascent - descent) / 2; break;
		case TextField.V_BOTTOM:    y -= descent; break;
		default:                    break;
		}
		return Bounds.create(x, y - ascent, width, ascent + descent);
	}

	public void draw(Graphics g) {
		Font old = g.getFont();
		if (font != null) g.setFont(font);

		int x = this.x;
		int y = this.y;
		FontMetrics fm = g.getFontMetrics();
		int width = fm.stringWidth(text);
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		switch (halign) {
		case TextField.H_CENTER:    x -= width / 2; break;
		case TextField.H_RIGHT:  x -= width; break;
		default:                    break;
		}
		switch (valign) {
		case TextField.V_TOP:      y += ascent; break;
		case TextField.V_CENTER:    y += ascent/ 2; break;
		case TextField.V_CENTER_OVERALL: y += (ascent - descent) / 2; break;
		case TextField.V_BOTTOM:    y -= descent; break;
		default:                    break;
		}
		g.drawString(text, x, y);
		g.setFont(old);
	}

}
