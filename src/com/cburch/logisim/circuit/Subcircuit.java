/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentEvent;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentUserEvent;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.comp.ManagedComponent;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.base.Pin;
import com.cburch.logisim.tools.MenuExtender;
import com.cburch.logisim.tools.ToolTipMaker;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringUtil;

public class Subcircuit extends ManagedComponent
        implements MenuExtender, ToolTipMaker {
    private class ViewItem extends JMenuItem implements ActionListener {
        Subcircuit comp;
        Project proj;

        ViewItem(Subcircuit comp, Project proj) {
            super(StringUtil.format(Strings.get("subcircuitViewItem"),
                        comp.source.getName()));
            this.comp = comp;
            this.proj = proj;
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            CircuitState superState = proj.getCircuitState();
            if (superState == null) return;

            CircuitState subState = comp.getSubstate(superState);
            if (subState == null) return;
            proj.setCircuitState(subState);
        }
    }

    private class Listener implements CircuitPinListener {
        public void pinAdded() {
            source.configureComponent(Subcircuit.this);
        }
        public void pinRemoved() {
            source.configureComponent(Subcircuit.this);
        }
        public void pinChanged() {
            source.configureComponent(Subcircuit.this);
        }
    }

    static int lastId = 0;
    int id = lastId++;

    private Listener listener = new Listener();
    private Circuit source;

    public Subcircuit(Location loc, Circuit source, AttributeSet attrs) {
        super(loc, attrs, source.pins.getPins().size());
        this.source = source;
        
        CircuitAttributes circAttrs = (CircuitAttributes) attrs;
        circAttrs.setSubcircuit(this);
        source.configureComponent(this);
        source.addPinListener(listener);
    }

    @Override
    public String toString() {
        return "Subcircuit" + id + "[" + source.getName() + "]";
    }

    public Circuit getSubcircuit() {
        return source;
    }

    public CircuitState getSubstate(CircuitState superState) {
        CircuitState subState = (CircuitState) superState.getData(this);
        if (subState == null) {
            subState = new CircuitState(superState.getProject(), source);
            superState.setData(this, subState);
            fireComponentInvalidated(new ComponentEvent(this));
        }
        return subState;
    }

    //
    // abstract ManagedComponent methods
    //
    @Override
    public ComponentFactory getFactory() {
        return source;
    }

    @Override
    public void propagate(CircuitState superState) {
        CircuitState subState = getSubstate(superState);

        int i = 0;
        for (EndData end : getEnds()) {
            Component pin = source.pins.getSubcircuitPin(i);
            Location super_loc = end.getLocation();
            Location sub_loc = pin.getLocation();
            if (Circuit.isInput(pin)) {
                Value new_val = superState.getValue(super_loc);
                InstanceState pinState = subState.getInstanceState(pin);
                Value old_val = Pin.FACTORY.getValue(pinState);
                if (!new_val.equals(old_val)) {
                    Pin.FACTORY.setValue(pinState, new_val);
                    pin.propagate(subState);
                }
            } else { // it is output-only
                Value val = subState.getValue(sub_loc);
                superState.setValue(super_loc, val, this, 1);
            }
            i++;
        }
    }
    
    //
    // user interface features
    //
    public void draw(ComponentDrawContext context) {
        java.awt.Graphics g = context.getGraphics();
        Bounds bds = getBounds();

        g.setColor(Color.GRAY);
        GraphicsUtil.switchToWidth(g, 2);
        Direction facing = ((CircuitAttributes) getAttributeSet()).getFacing();
        int ax;
        int ay;
        int an;
        if (facing == Direction.SOUTH) {
            ax = bds.getX() + bds.getWidth() - 1;
            ay = bds.getY() + bds.getHeight() / 2;
            an = 90;
        } else if (facing == Direction.NORTH) {
            ax = bds.getX() + 1;
            ay = bds.getY() + bds.getHeight() / 2;
            an = -90;
        } else if (facing == Direction.WEST) {
            ax = bds.getX() + bds.getWidth() / 2;
            ay = bds.getY() + bds.getHeight() - 1;
            an = 0;
        } else {
            ax = bds.getX() + bds.getWidth() / 2;
            ay = bds.getY() + 1;
            an = 180;
        }
        g.drawArc(ax - 4, ay - 4, 8, 8, an, 180);
        g.setColor(Color.BLACK);
        
        context.drawBounds(this);
        drawLabel(context, bds, facing);
        context.drawPins(this);
    }
    
    private void drawLabel(ComponentDrawContext context, Bounds bds,
            Direction facing) {
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
            Graphics g = context.getGraphics().create();
            double angle = Math.PI / 2 - up.toRadians() - facing.toRadians();
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
    
    @Override
    public Object getFeature(Object key) {
        if (key == MenuExtender.class) return this;
        if (key == ToolTipMaker.class) return this;
        else return super.getFeature(key);
    }

    public void configureMenu(JPopupMenu menu, Project proj) {
        menu.add(new ViewItem(this, proj));
    }

    public String getToolTip(ComponentUserEvent e) {
        for (int i = getEnds().size() - 1; i >= 0; i--) {
            if (getEndLocation(i).manhattanDistanceTo(e.getX(), e.getY()) < 10) {
                Component pin = source.pins.getSubcircuitPin(i);
                String label = pin.getAttributeSet().getValue(StdAttr.LABEL);
                return label != null && label.length() > 0 ? label : null;
            }
        }
        return StringUtil.format(Strings.get("subcircuitCircuitTip"), source.getDisplayName());
    }

    @Override
    protected void recomputeBounds() { // to make method visible within this package
        super.recomputeBounds();
    }
}
