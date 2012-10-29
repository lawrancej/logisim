/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.io;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceDataSingleton;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import static com.cburch.logisim.util.LocaleString.*;

public class SevenSegment extends InstanceFactory {
	static Bounds[] SEGMENTS = null;
	static Color DEFAULT_OFF = new Color(220, 220, 220);
	
	public SevenSegment() {
		super("7-Segment Display", __("sevenSegmentComponent"));
		setAttributes(new Attribute[] { Io.ATTR_ON_COLOR, Io.ATTR_OFF_COLOR,
					Io.ATTR_BACKGROUND, Io.ATTR_ACTIVE },
				new Object[] { new Color(240, 0, 0), DEFAULT_OFF,
					Io.DEFAULT_BACKGROUND, Boolean.TRUE });
		setOffsetBounds(Bounds.create(-5, 0, 40, 60));
		setIconName("7seg.gif");
		setPorts(new Port[] {
				new Port(20,  0, Port.INPUT, 1),
				new Port(30,  0, Port.INPUT, 1),
				new Port(20, 60, Port.INPUT, 1),
				new Port(10, 60, Port.INPUT, 1),
				new Port( 0, 60, Port.INPUT, 1),
				new Port(10,  0, Port.INPUT, 1),
				new Port( 0,  0, Port.INPUT, 1),
				new Port(30, 60, Port.INPUT, 1),
			});
	}

	@Override
	public void propagate(InstanceState state) {
		int summary = 0;
		for (int i = 0; i < 8; i++) {
			Value val = state.getPort(i);
			if (val == Value.TRUE) summary |= 1 << i;
		}
		Object value = Integer.valueOf(summary);
		InstanceDataSingleton data = (InstanceDataSingleton) state.getData();
		if (data == null) {
			state.setData(new InstanceDataSingleton(value));
		} else {
			data.setValue(value);
		}
	}
	
	@Override
	public void paintInstance(InstancePainter painter) {
		drawBase(painter);
	}
	
	static void drawBase(InstancePainter painter) {
		ensureSegments();
		InstanceDataSingleton data = (InstanceDataSingleton) painter.getData();
		int summ = (data == null ? 0 : ((Integer) data.getValue()).intValue());
		Boolean active = painter.getAttributeValue(Io.ATTR_ACTIVE);
		int desired = active == null || active.booleanValue() ? 1 : 0;
		
		Bounds bds = painter.getBounds();
		int x = bds.getX() + 5;
		int y = bds.getY();

		Graphics g = painter.getGraphics();
		Color onColor = painter.getAttributeValue(Io.ATTR_ON_COLOR);
		Color offColor = painter.getAttributeValue(Io.ATTR_OFF_COLOR);
		Color bgColor = painter.getAttributeValue(Io.ATTR_BACKGROUND);
		if (painter.shouldDrawColor() && bgColor.getAlpha() != 0) {
			g.setColor(bgColor);
			g.fillRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
			g.setColor(Color.BLACK);
		}
		painter.drawBounds();
		g.setColor(Color.DARK_GRAY);
		for (int i = 0; i <= 7; i++) {
			if (painter.getShowState()) {
				g.setColor(((summ >> i) & 1) == desired ? onColor : offColor);
			}
			if (i < 7) {
				Bounds seg = SEGMENTS[i];
				g.fillRect(x + seg.getX(), y + seg.getY(), seg.getWidth(), seg.getHeight());
			} else {
				g.fillOval(x + 28, y + 48, 5, 5); // draw decimal point
			}
		}
		painter.drawPorts();
	}
	
	static void ensureSegments() {
		if (SEGMENTS == null) {
			SEGMENTS = new Bounds[] {
					Bounds.create( 3,  8, 19,  4),
					Bounds.create(23, 10,  4, 19),
					Bounds.create(23, 30,  4, 19),
					Bounds.create( 3, 47, 19,  4),
					Bounds.create(-2, 30,  4, 19),
					Bounds.create(-2, 10,  4, 19),
					Bounds.create( 3, 28, 19,  4)
			};
		}
	}
}
