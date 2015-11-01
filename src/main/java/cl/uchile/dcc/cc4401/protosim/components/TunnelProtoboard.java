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

public class TunnelProtoboard extends InstanceFactory {
    
    public static InstanceFactory FACTORY = new Led();
    
    List<Port> ports;

    public TunnelProtoboard() {
        super("TunnelProtoboard");
        
        List<Port> ports = new ArrayList<Port>();
        
        for (int i = 0; i <= 64 * 10; i += 10) {
            for (int j = 0; j < 10; j += 10) {
                ports.add(new Port(10 + i, 10 + j, Port.INOUT, 1));
                ports.add(new Port(10 + i, 20 + j, Port.INOUT, 1));
                
                ports.add(new Port(10 + i, 170 + j, Port.INOUT, 1));
                ports.add(new Port(10 + i, 180 + j, Port.INOUT, 1));
            }
        }
        
        for (int i = 0; i <= 64 * 10; i += 10) {
            for (int j = 0; j < 4 * 10; j += 10) {
                ports.add(new Port(10 + i, 50 + j, Port.INOUT, 1));
                ports.add(new Port(10 + i, 110 + j, Port.INOUT, 1));
            }
        }
                
        setPorts(ports);
    }
    
    @Override
    public Bounds getOffsetBounds(AttributeSet attrs) {
        return Bounds.create(0, 0, 660, 190);
    }

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

    @Override
    public void propagate(InstanceState state) {
        
    }

}
