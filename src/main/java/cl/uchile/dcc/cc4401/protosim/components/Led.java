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

public class Led extends InstanceFactory {
    
    public static InstanceFactory FACTORY = new Led();
    
    List<Port> ports;

    public Led() {
        super("Led");
        this.setIcon(Icons.getIcon("protosimComponentLed.svg"));
        
        ports = new ArrayList<Port>();

        // Lower ports
        ports.add(new Port(0, 20, Port.INPUT, Breadboard.PORT_WIDTH));
        ports.add(new Port(10, 20, Port.INPUT, Breadboard.PORT_WIDTH));


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
        g.fillRect(x - 5, y + 5, 20, 5);


        // Pins
        g.setColor(Color.gray);
 
        g.fillRect(x - 2, y + 10, 4, 10);
        g.fillRect(x + 8, y + 10, 4, 10);

        painter.drawPorts();
    }

    @Override
    public void propagate(InstanceState state) {
        // TODO Auto-generated method stub  
    }

}
