/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.awt.Font;
import java.util.Arrays;
import java.util.List;

import com.cburch.logisim.circuit.appear.CircuitAppearanceEvent;
import com.cburch.logisim.circuit.appear.CircuitAppearanceListener;
import com.cburch.logisim.data.AbstractAttributeSet;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.AttributeSets;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.StdAttr;
import static com.cburch.logisim.util.LocaleString.*;

public class CircuitAttributes extends AbstractAttributeSet {
	public static final Attribute<String> NAME_ATTR
		= Attributes.forString("circuit", __("circuitName"));
	
	public static final Attribute<Direction> LABEL_LOCATION_ATTR
		= Attributes.forDirection("labelloc", __("circuitLabelLocAttr"));

	public static final Attribute<String> CIRCUIT_LABEL_ATTR
		= Attributes.forString("clabel", __("circuitLabelAttr"));
	
	public static final Attribute<Direction> CIRCUIT_LABEL_FACING_ATTR
		= Attributes.forDirection("clabelup", __("circuitLabelDirAttr"));
	
	public static final Attribute<Font> CIRCUIT_LABEL_FONT_ATTR
		= Attributes.forFont("clabelfont", __("circuitLabelFontAttr"));
	
	private static final Attribute<?>[] STATIC_ATTRS = {
		NAME_ATTR, CIRCUIT_LABEL_ATTR, CIRCUIT_LABEL_FACING_ATTR, CIRCUIT_LABEL_FONT_ATTR,
	};
	private static final Object[] STATIC_DEFAULTS = {
		"", "", Direction.EAST, StdAttr.DEFAULT_LABEL_FONT,
	};
	private static final List<Attribute<?>> INSTANCE_ATTRS
		= Arrays.asList(new Attribute<?>[] {
				StdAttr.FACING, StdAttr.LABEL, LABEL_LOCATION_ATTR,
				StdAttr.LABEL_FONT,
				CircuitAttributes.NAME_ATTR, CIRCUIT_LABEL_ATTR,
				CIRCUIT_LABEL_FACING_ATTR, CIRCUIT_LABEL_FONT_ATTR,
			});
	
	private static class StaticListener implements AttributeListener {
		private Circuit source;
		
		private StaticListener(Circuit s) { source = s; }
		
		public void attributeListChanged(AttributeEvent e) { }

		public void attributeValueChanged(AttributeEvent e) {
			if (e.getAttribute() == NAME_ATTR) {
				source.fireEvent(CircuitEvent.ACTION_SET_NAME, e.getValue());
			}
		}
	}
	
	private class MyListener implements AttributeListener, CircuitAppearanceListener {
		public void attributeListChanged(AttributeEvent e) { }

		public void attributeValueChanged(AttributeEvent e) {
			@SuppressWarnings("unchecked")
			Attribute<Object> a = (Attribute<Object>) e.getAttribute();
			fireAttributeValueChanged(a, e.getValue());
		}
		
		public void circuitAppearanceChanged(CircuitAppearanceEvent e) {
			SubcircuitFactory factory;
			factory = (SubcircuitFactory) subcircInstance.getFactory();
			if (e.isConcerning(CircuitAppearanceEvent.PORTS)) {
				factory.computePorts(subcircInstance);
			}
			if (e.isConcerning(CircuitAppearanceEvent.BOUNDS)) {
				subcircInstance.recomputeBounds();
			}
			subcircInstance.fireInvalidated();
		}
	}
	
	static AttributeSet createBaseAttrs(Circuit source, String name) {
		AttributeSet ret = AttributeSets.fixedSet(STATIC_ATTRS, STATIC_DEFAULTS);
		ret.setValue(CircuitAttributes.NAME_ATTR, name);
		ret.addAttributeListener(new StaticListener(source));
		return ret;
	}

	private Circuit source;
	private Instance subcircInstance;
	private Direction facing;
	private String label;
	private Direction labelLocation;
	private Font labelFont;
	private MyListener listener;
	private Instance[] pinInstances;
	
	public CircuitAttributes(Circuit source) {
		this.source = source;
		subcircInstance = null;
		facing = source.getAppearance().getFacing();
		label = "";
		labelLocation = Direction.NORTH;
		labelFont = StdAttr.DEFAULT_LABEL_FONT;
		pinInstances = new Instance[0];
	}
	
	void setSubcircuit(Instance value) {
		subcircInstance = value;
		if (subcircInstance != null && listener == null) {
			listener = new MyListener();
			source.getStaticAttributes().addAttributeListener(listener);
			source.getAppearance().addCircuitAppearanceListener(listener);
		}
	}
	
	Instance[] getPinInstances() {
		return pinInstances;
	}
	
	void setPinInstances(Instance[] value) {
		pinInstances = value;
	}
	
	public Direction getFacing() {
		return facing;
	}

	@Override
	protected void copyInto(AbstractAttributeSet dest) {
		CircuitAttributes other = (CircuitAttributes) dest;
		other.subcircInstance = null;
		other.listener = null;
	}
	
	@Override
	public boolean isToSave(Attribute<?> attr) {
		Attribute<?>[] statics = STATIC_ATTRS;
		for (int i = 0; i < statics.length; i++) {
			if (statics[i] == attr) return false;
		}
		return true;
	}

	@Override
	public List<Attribute<?>> getAttributes() {
		return INSTANCE_ATTRS;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E> E getValue(Attribute<E> attr) {
		if (attr == StdAttr.FACING) return (E) facing;
		if (attr == StdAttr.LABEL) return (E) label;
		if (attr == StdAttr.LABEL_FONT) return (E) labelFont;
		if (attr == LABEL_LOCATION_ATTR) return (E) labelLocation;
		else return source.getStaticAttributes().getValue(attr);
	}

	@Override
	public <E> void setValue(Attribute<E> attr, E value) {
		if (attr == StdAttr.FACING) {
			Direction val = (Direction) value;
			facing = val;
			fireAttributeValueChanged(StdAttr.FACING, val);
			if (subcircInstance != null) subcircInstance.recomputeBounds();
		} else if (attr == StdAttr.LABEL) {
			String val = (String) value;
			label = val;
			fireAttributeValueChanged(StdAttr.LABEL, val);
		} else if (attr == StdAttr.LABEL_FONT) {
			Font val = (Font) value;
			labelFont = val;
			fireAttributeValueChanged(StdAttr.LABEL_FONT, val);
		} else if (attr == LABEL_LOCATION_ATTR) {
			Direction val = (Direction) value;
			labelLocation = val;
			fireAttributeValueChanged(LABEL_LOCATION_ATTR, val);
		} else {
			source.getStaticAttributes().setValue(attr, value);
			if (attr == NAME_ATTR) {
				source.fireEvent(CircuitEvent.ACTION_SET_NAME, value);
			}
		}
	}
}
