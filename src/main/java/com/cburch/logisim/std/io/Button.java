/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.io;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceDataSingleton;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;


import static com.cburch.logisim.util.LocaleString.*;

public class Button extends InstanceFactory {
    private static final int DEPTH = 3;

    static final AttributeOption OUTPUT_01
    	= new AttributeOption("01", getFromLocale("gateOutput01"));
    static final AttributeOption OUTPUT_0Z
    	= new AttributeOption("0Z", getFromLocale("gateOutput0Z"));
    static final AttributeOption OUTPUT_Z1
    	= new AttributeOption("Z1", getFromLocale("gateOutputZ1"));
    static final AttributeOption OUTPUT_Z0
		= new AttributeOption("Z0", getFromLocale("gateOutputZ0"));
    static final AttributeOption OUTPUT_10
		= new AttributeOption("10", getFromLocale("gateOutput10"));
    static final AttributeOption OUTPUT_1Z
		= new AttributeOption("1Z", getFromLocale("gateOutput1Z"));
    public static final Attribute<AttributeOption> ATTR_OUTPUT
    	= Attributes.forOption("out", getFromLocale("gateOutputAttr"),
    			new AttributeOption[] { OUTPUT_01, OUTPUT_10, OUTPUT_0Z, OUTPUT_Z1, OUTPUT_Z0, OUTPUT_1Z  });

    
    public Button() {
        super("Button", getFromLocale("buttonComponent"));
        setAttributes(new Attribute[] {
                StdAttr.FACING, Io.ATTR_COLOR,
                StdAttr.LABEL, Io.ATTR_LABEL_LOC,
                StdAttr.LABEL_FONT, Io.ATTR_LABEL_COLOR, Button.ATTR_OUTPUT
            }, new Object[] {
                Direction.EAST, Color.WHITE,
                "", Io.LABEL_CENTER,
                StdAttr.DEFAULT_LABEL_FONT, Color.BLACK, Button.OUTPUT_01
            });
        setFacingAttribute(StdAttr.FACING);
        setIconName("button.svg");
        setPorts(new Port[] { new Port(0, 0, Port.OUTPUT, 1) });
        setInstancePoker(Poker.class);
        setInstanceLogger(Logger.class);
    }

    @Override
    public Bounds getOffsetBounds(AttributeSet attrs) {
        Direction facing = attrs.getValue(StdAttr.FACING);
        return Bounds.create(-20, -10, 20, 20).rotate(Direction.EAST, facing, 0, 0);
    }

    @Override
    protected void configureNewInstance(Instance instance) {
        instance.addAttributeListener();
        computeTextField(instance);
    }

    @Override
    protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
        if (attr == StdAttr.FACING) {
            instance.recomputeBounds();
            computeTextField(instance);
        } else if (attr == Io.ATTR_LABEL_LOC) {
            computeTextField(instance);
        }
    }

    private void computeTextField(Instance instance) {
        Direction facing = instance.getAttributeValue(StdAttr.FACING);
        Object labelLoc = instance.getAttributeValue(Io.ATTR_LABEL_LOC);

        Bounds bds = instance.getBounds();
        int x = bds.getX() + bds.getWidth() / 2;
        int y = bds.getY() + bds.getHeight() / 2;
        int halign = GraphicsUtil.H_CENTER;
        int valign = GraphicsUtil.V_CENTER;
        if (labelLoc == Io.LABEL_CENTER) {
            x = bds.getX() + (bds.getWidth() - DEPTH) / 2;
            y = bds.getY() + (bds.getHeight() - DEPTH) / 2;
        } else if (labelLoc == Direction.NORTH) {
            y = bds.getY() - 2;
            valign = GraphicsUtil.V_BOTTOM;
        } else if (labelLoc == Direction.SOUTH) {
            y = bds.getY() + bds.getHeight() + 2;
            valign = GraphicsUtil.V_TOP;
        } else if (labelLoc == Direction.EAST) {
            x = bds.getX() + bds.getWidth() + 2;
            halign = GraphicsUtil.H_LEFT;
        } else if (labelLoc == Direction.WEST) {
            x = bds.getX() - 2;
            halign = GraphicsUtil.H_RIGHT;
        }
        if (labelLoc == facing) {
            if (labelLoc == Direction.NORTH || labelLoc == Direction.SOUTH) {
                x += 2;
                halign = GraphicsUtil.H_LEFT;
            } else {
                y -= 2;
                valign = GraphicsUtil.V_BOTTOM;
            }
        }

        instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT,
                x, y, halign, valign);
    }

    @Override
    public void propagate(InstanceState state) {
        InstanceDataSingleton data = (InstanceDataSingleton) state.getData();
        Value val = data == null ? Value.FALSE : (Value) data.getValue();
        state.setPort(0, val, 1);
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        Bounds bds = painter.getBounds();
        int x = bds.getX();
        int y = bds.getY();
        int w = bds.getWidth();
        int h = bds.getHeight();

        Value val;
        if (painter.getShowState()) {
            InstanceDataSingleton data = (InstanceDataSingleton) painter.getData();
            val = data == null ? Value.FALSE : (Value) data.getValue();
        } else {
            val = Value.FALSE;
        }

        Color color = painter.getAttributeValue(Io.ATTR_COLOR);
        if (!painter.shouldDrawColor()) {
            int hue = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
            color = new Color(hue, hue, hue);
        }

        Graphics g = painter.getGraphics();
        int depress;
        if (val == ButtonStateToValue(true,painter.getAttributeValue(Button.ATTR_OUTPUT))) {
            x += DEPTH;
            y += DEPTH;
            Object labelLoc = painter.getAttributeValue(Io.ATTR_LABEL_LOC);
            if (labelLoc == Io.LABEL_CENTER || labelLoc == Direction.NORTH
                    || labelLoc == Direction.WEST) {
                depress = DEPTH;
            } else {
                depress = 0;
            }

            Object facing = painter.getAttributeValue(StdAttr.FACING);
            if (facing == Direction.NORTH || facing == Direction.WEST) {
                Location p = painter.getLocation();
                int px = p.getX();
                int py = p.getY();
                GraphicsUtil.switchToWidth(g, Wire.WIDTH);
                g.setColor(Value.TRUE_COLOR);
                if (facing == Direction.NORTH) {
                    g.drawLine(px, py, px, py + 10);
                }

                else {
                                             g.drawLine(px, py, px + 10, py);
                }

                GraphicsUtil.switchToWidth(g, 1);
            }

            g.setColor(color);
            g.fillRect(x, y, w - DEPTH, h - DEPTH);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, w - DEPTH, h - DEPTH);
        } else {
            depress = 0;
            int[] xp = new int[] { x, x + w - DEPTH, x + w, x + w, x + DEPTH, x };
            int[] yp = new int[] { y, y, y + DEPTH, y + h, y + h, y + h - DEPTH };
            g.setColor(color.darker());
            g.fillPolygon(xp, yp, xp.length);
            g.setColor(color);
            g.fillRect(x, y, w - DEPTH, h - DEPTH);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, w - DEPTH, h - DEPTH);
            g.drawLine(x + w - DEPTH, y + h - DEPTH, x + w, y + h);
            g.drawPolygon(xp, yp, xp.length);
        }

        g.translate(depress, depress);
        g.setColor(painter.getAttributeValue(Io.ATTR_LABEL_COLOR));
        painter.drawLabel();
        g.translate(-depress, -depress);
        painter.drawPorts();
    }
    
    private static Value ButtonStateToValue(Boolean pressed, AttributeOption a){
    	if(pressed==true){
        	if((a==Button.OUTPUT_01) | (a==Button.OUTPUT_Z1)){
        		return Value.TRUE;
        	}
        	else if((a==Button.OUTPUT_10) | (a==Button.OUTPUT_Z0)){
        		return Value.FALSE;
        	}
        	else{
        		return Value.NIL;
        	}
    	}
    	else{
        	if((a==Button.OUTPUT_10) | (a==Button.OUTPUT_1Z)){
        		return Value.TRUE;
        	}
        	else if((a==Button.OUTPUT_01) | (a==Button.OUTPUT_0Z)){
        		return Value.FALSE;
        	}
        	else{
        		return Value.NIL;
        	}    		
    	}
    }

    public static class Poker extends InstancePoker {
        @Override
        public void mousePressed(InstanceState state, MouseEvent e) {
                setValue(state, ButtonStateToValue(true,state.getAttributeValue(Button.ATTR_OUTPUT)));        		    		
        }

        @Override
        public void mouseReleased(InstanceState state, MouseEvent e) {
            setValue(state, ButtonStateToValue(false,state.getAttributeValue(Button.ATTR_OUTPUT)));        		    		
        }

        private void setValue(InstanceState state, Value val) {
            InstanceDataSingleton data = (InstanceDataSingleton) state.getData();
            if (data == null) {
                state.setData(new InstanceDataSingleton(val));
            } else {
                data.setValue(val);
            }
            state.getInstance().fireInvalidated();
        }
    }

    public static class Logger extends InstanceLogger {
        @Override
        public String getLogName(InstanceState state, Object option) {
            return state.getAttributeValue(StdAttr.LABEL);
        }

        @Override
        public Value getLogValue(InstanceState state, Object option) {
            InstanceDataSingleton data = (InstanceDataSingleton) state.getData();
            return data == null ? Value.FALSE : (Value) data.getValue();
        }
    }
}
