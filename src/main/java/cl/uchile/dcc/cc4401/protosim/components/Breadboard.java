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

public class Breadboard extends InstanceFactory {

	public static final int PORT_WIDTH = 32;
    
    public static InstanceFactory FACTORY = new Breadboard();
    
    private List<Port> ports;

    public Breadboard() {
        super("Breadboard");
        
        ports = new ArrayList<Port>();
        
        // Terminal ports (logic)
        for (int i = 0; i <= 64 * 10; i += 10) {
            for (int j = 0; j < 10; j += 10) {
                ports.add(new Port(10 + i, 10 + j, Port.INOUT, PORT_WIDTH));
                ports.add(new Port(10 + i, 20 + j, Port.INOUT, PORT_WIDTH));
                
                ports.add(new Port(10 + i, 170 + j, Port.INOUT, PORT_WIDTH));
                ports.add(new Port(10 + i, 180 + j, Port.INOUT, PORT_WIDTH));
            }
        }
        
        // Bus ports (power)
        for (int i = 0; i <= 64 * 10; i += 10) {
            for (int j = 0; j < 4 * 10; j += 10) {
                ports.add(new Port(10 + i, 50 + j, Port.INOUT, PORT_WIDTH));
                ports.add(new Port(10 + i, 110 + j, Port.INOUT, PORT_WIDTH));
            }
        }
                
        setPorts(ports);
    }
    
    /*
     * Required bounds to move the component in Logisim canvas
     */
    @Override
    public Bounds getOffsetBounds(AttributeSet attrs) {
        return Bounds.create(0, 0, 660, 190);
    }

    /*
     * Draws the component on Logisim canvas
     */
    @Override
    public void paintInstance(InstancePainter painter) {
        Location loc = painter.getLocation();
        int x = loc.getX();
        int y = loc.getY();

        Graphics g = painter.getGraphics();
        g.setColor(Color.black);
        g.drawRect(x, y, 660, 190);

        painter.drawPorts();
    }

    /*
     * Internal component logic
     */
    @Override
    public void propagate(InstanceState state) {
        // TODO Auto-generated method stub  
    }

}
