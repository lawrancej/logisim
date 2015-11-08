package cl.uchile.dcc.cc4401.protosim.components;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;

public class Resistor extends InstanceFactory {
	
	public static InstanceFactory FACTORY = new Resistor();

    private List<Port> ports;

    public Resistor() {
        super("Resistor");
        setIconName("protosimComponentResistor.svg");

        ports = new ArrayList<Port>();

        ports.add(new Port(0, 0, Port.INPUT, Breadboard.PORT_WIDTH));
        ports.add(new Port(40, 0, Port.OUTPUT, Breadboard.PORT_WIDTH));


        setPorts(ports);
    }

    @Override
    public String getDisplayName() {
        // TODO: l10n this
        // return getFromLocale("Resistance");
        return "Resistor";
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        Location loc = painter.getLocation();
        int x = loc.getX();
        int y = loc.getY();

        Graphics g = painter.getGraphics();

        g.setColor(new Color(170, 126, 57));
        g.fillRect(x+6, y - 4, 28, 8);
        g.setColor(Color.red);
        g.fillRect(x+10, y-4, 5, 8);
        g.setColor(Color.black);
        g.fillRect(x+18, y-4, 5, 8);
        g.setColor(Color.gray);
        g.fillRect(x, y-2, 6, 4);
        g.fillRect(x+34, y-2, 6, 4);

        painter.drawPorts();
    }

    @Override
    public Bounds getOffsetBounds(AttributeSet attrs) {
        return Bounds.create(0, -5, 40, 10);
    }

    @Override
    public void propagate(InstanceState state) {
        // TODO Auto-generated method stub
    }
}