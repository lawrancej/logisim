/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.base.Pin;
import com.cburch.logisim.tools.MenuExtender;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringGetter;
import com.cburch.logisim.util.StringUtil;

public class SubcircuitFactory extends InstanceFactory {
	private class CircuitFeature implements StringGetter, MenuExtender, ActionListener {
		private Instance instance;
		private Project proj;
		
		public CircuitFeature(Instance instance) {
			this.instance = instance;
		}
		
		public String get() {
			return source.getName();
		}

		public void configureMenu(JPopupMenu menu, Project proj) {
			this.proj = proj;
			String name = instance.getFactory().getDisplayName();
			String text = Strings.get("subcircuitViewItem", name);
			JMenuItem item = new JMenuItem(text);
			item.addActionListener(this);
			menu.add(item);
		}

		public void actionPerformed(ActionEvent e) {
			CircuitState superState = proj.getCircuitState();
			if (superState == null) return;

			CircuitState subState = getSubstate(superState, instance);
			if (subState == null) return;
			proj.setCircuitState(subState);
		}
	}

	private Circuit source;

	public SubcircuitFactory(Circuit source) {
		super("", null);
		this.source = source;
		setFacingAttribute(StdAttr.FACING);
		setDefaultToolTip(new CircuitFeature(null));
		setInstancePoker(SubcircuitPoker.class);
	}

	public Circuit getSubcircuit() {
		return source;
	}
	
	@Override
	public String getName() {
		return source.getName();
	}

	@Override
	public StringGetter getDisplayGetter() {
		return StringUtil.constantGetter(source.getName());
	}

	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction facing = attrs.getValue(StdAttr.FACING);
		Direction defaultFacing = source.getAppearance().getFacing();
		Bounds bds = source.getAppearance().getOffsetBounds();
		return bds.rotate(defaultFacing, facing, 0, 0);
	}

	@Override
	public AttributeSet createAttributeSet() {
		return new CircuitAttributes(source);
	}
	
	//
	// methods for configuring instances
	//
	@Override
	public void configureNewInstance(Instance instance) {
		CircuitAttributes attrs = (CircuitAttributes) instance.getAttributeSet();
		attrs.setSubcircuit(instance);
		
		instance.addAttributeListener();
		computePorts(instance);
	}
	
	@Override
	public void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == StdAttr.FACING) {
			computePorts(instance);
		}
	}
	
	@Override
	public Object getInstanceFeature(Instance instance, Object key) {
		if (key == MenuExtender.class) return new CircuitFeature(instance);
		return super.getInstanceFeature(instance, key);
	}
	
	void computePorts(Instance instance) {
		Direction facing = instance.getAttributeValue(StdAttr.FACING);
		Map<Location, Instance> portLocs = source.getAppearance().getPortOffsets(facing);
		Port[] ports = new Port[portLocs.size()];
		Instance[] pins = new Instance[portLocs.size()];
		int i = -1;
		for (Map.Entry<Location, Instance> portLoc : portLocs.entrySet()) {
			i++;
			Location loc = portLoc.getKey();
			Instance pin = portLoc.getValue();
			String type = Pin.FACTORY.isInputPin(pin) ? Port.INPUT : Port.OUTPUT;
			BitWidth width = pin.getAttributeValue(StdAttr.WIDTH);
			ports[i] = new Port(loc.getX(), loc.getY(), type, width);
			pins[i] = pin;
			
			String label = pin.getAttributeValue(StdAttr.LABEL);
			if (label != null && label.length() > 0) {
				ports[i].setToolTip(StringUtil.constantGetter(label));
			}
		}
		
		CircuitAttributes attrs = (CircuitAttributes) instance.getAttributeSet();
		attrs.setPinInstances(pins);
		instance.setPorts(ports);
	}

	//
	// propagation-oriented methods
	//
	public CircuitState getSubstate(CircuitState superState, Instance instance) {
		return getSubstate(createInstanceState(superState, instance));
	}
	
	public CircuitState getSubstate(CircuitState superState, Component comp) {
		return getSubstate(createInstanceState(superState, comp));
	}
	
	private CircuitState getSubstate(InstanceState instanceState) {
		CircuitState subState = (CircuitState) instanceState.getData();
		if (subState == null) {
			subState = new CircuitState(instanceState.getProject(), source);
			instanceState.setData(subState);
			instanceState.fireInvalidated();
		}
		return subState;
	}

	@Override
	public void propagate(InstanceState superState) {
		CircuitState subState = getSubstate(superState);

		CircuitAttributes attrs = (CircuitAttributes) superState.getAttributeSet();
		Instance[] pins = attrs.getPinInstances();
		for (int i = 0; i < pins.length; i++) {
			Instance pin = pins[i];
			InstanceState pinState = subState.getInstanceState(pin);
			if (Pin.FACTORY.isInputPin(pin)) {
				Value newVal = superState.getPort(i);
				Value oldVal = Pin.FACTORY.getValue(pinState);
				if (!newVal.equals(oldVal)) {
					Pin.FACTORY.setValue(pinState, newVal);
					Pin.FACTORY.propagate(pinState);
				}
			} else { // it is output-only
				Value val = pinState.getPort(0);
				superState.setPort(i, val, 1);
			}
		}
	}
	
	//
	// user interface features
	//	
	@Override
	public void paintGhost(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		CircuitAttributes attrs = (CircuitAttributes) painter.getAttributeSet();
		Direction facing = attrs.getFacing();
		Direction defaultFacing = source.getAppearance().getFacing();
		Location loc = painter.getLocation();
		g.translate(loc.getX(), loc.getY());
		source.getAppearance().paintSubcircuit(g, facing);
		drawLabel(painter, getOffsetBounds(attrs), facing, defaultFacing);
		g.translate(-loc.getX(), -loc.getY());
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		paintGhost(painter);
		painter.drawPorts();
	}
	
	private void drawLabel(InstancePainter painter, Bounds bds,
			Direction facing, Direction defaultFacing) {
		AttributeSet staticAttrs = source.getStaticAttributes();
		String label = staticAttrs.getValue(StdAttr.LABEL);
		if (label != null && !label.equals("")) {
			Direction up = staticAttrs.getValue(CircuitAttributes.LABEL_UP_ATTR);
			Font font = staticAttrs.getValue(StdAttr.LABEL_FONT);

			int back = label.indexOf('\\');
			int lines = 1;
			boolean backs = false;
			while (back >= 0 && back <= label.length() - 2) {
				char c = label.charAt(back + 1);
				if (c == 'n') lines++;
				else if (c == '\\') backs = true;
				back = label.indexOf('\\', back + 2);
			}
			
			int x = bds.getX() + bds.getWidth() / 2;
			int y = bds.getY() + bds.getHeight() / 2;
			Graphics g = painter.getGraphics().create();
			double angle = Math.PI / 2 - (up.toRadians() - defaultFacing.toRadians()) - facing.toRadians();
			if (g instanceof Graphics2D && Math.abs(angle) > 0.01) {
				Graphics2D g2 = (Graphics2D) g;
				g2.rotate(angle, x, y);
			}
			g.setFont(font);
			if (lines == 1 && !backs) {
				GraphicsUtil.drawCenteredText(g, label, x, y);
			} else {
				FontMetrics fm = g.getFontMetrics();
				int height = fm.getHeight();
				y = y - (height * lines - fm.getLeading()) / 2 + fm.getAscent(); 
				back = label.indexOf('\\');
				while (back >= 0 && back <= label.length() - 2) {
					char c = label.charAt(back + 1);
					if (c == 'n') {
						String line = label.substring(0, back);
						GraphicsUtil.drawText(g, line, x, y,
						        GraphicsUtil.H_CENTER, GraphicsUtil.V_BASELINE);
						y += height;
						label = label.substring(back + 2);
						back = label.indexOf('\\');
					} else if (c == '\\') {
						label = label.substring(0, back) + label.substring(back + 1);
						back = label.indexOf('\\', back + 1);
					} else {
						back = label.indexOf('\\', back + 2);
					}
				}
				GraphicsUtil.drawText(g, label, x, y,
						GraphicsUtil.H_CENTER, GraphicsUtil.V_BASELINE);
			}
			g.dispose();
		}
	}

	/* TODO
	public String getToolTip(ComponentUserEvent e) {
		return StringUtil.format(Strings.get("subcircuitCircuitTip"), source.getDisplayName());
	} */
}
