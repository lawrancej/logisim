package cl.uchile.dcc.cc4401.protosim.components;

import java.awt.Color;
import java.awt.Font;
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
import com.cburch.logisim.util.Icons;

public class OrChip extends InstanceFactory {
    
    public static InstanceFactory FACTORY = new OrChip();
    
    private List<Port> ports;

    public OrChip() {
        super("OR Chip");
        this.setIcon(Icons.getIcon("protosimComponentChipOr.svg")); 
        ports = new ArrayList<Port>();

        // Upper ports
        ports.add(new Port(0, 0, Port.INPUT, Breadboard.PORT_WIDTH));
        ports.add(new Port(10, 0, Port.INPUT, Breadboard.PORT_WIDTH));
        ports.add(new Port(20, 0, Port.OUTPUT, Breadboard.PORT_WIDTH));

        // Lower ports
        ports.add(new Port(0, 30, Port.INPUT, Breadboard.PORT_WIDTH));
        ports.add(new Port(10, 30, Port.INPUT, Breadboard.PORT_WIDTH));
        ports.add(new Port(20, 30, Port.OUTPUT, Breadboard.PORT_WIDTH));

        setPorts(ports);
    }
    
    @Override
    public Bounds getOffsetBounds(AttributeSet attrs) {
        return Bounds.create(0, 0, 20, 30);
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        Location loc = painter.getLocation();
        int x = loc.getX();
        int y = loc.getY();

        Graphics g = painter.getGraphics();

        // Chip
        g.setColor(Color.black);
        g.fillRect(x - 2, y + 5, 24, 20);

        // Text
        g.setColor(Color.white);
        g.setFont(new Font("Courier", Font.BOLD, 9));
        g.drawString("OR", x + 4, y + 17);

        // Pins
        g.setColor(Color.gray);
        g.fillRect(x - 2, y, 4, 5);
        g.fillRect(x + 8, y, 4, 5);
        g.fillRect(x + 18, y, 4, 5);

        g.fillRect(x - 2, y + 25, 4, 5);
        g.fillRect(x + 8, y + 25, 4, 5);
        g.fillRect(x + 18, y + 25, 4, 5);
        
        painter.drawPorts();
    }
    @Override
    public void propagate(InstanceState state) {
        // TODO Auto-generated method stub  
    }

}
