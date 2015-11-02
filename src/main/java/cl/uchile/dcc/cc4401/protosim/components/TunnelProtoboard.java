package cl.uchile.dcc.cc4401.protosim.components;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;

public class TunnelProtoboard extends InstanceFactory {
    
    public static InstanceFactory FACTORY = new TunnelProtoboard();
    List<Port> ports;
    HashMap<Port,Integer> connected;
    
    public TunnelProtoboard() {
        super("TunnelProtoboard");
        
        ports = new ArrayList<Port>();
        connected=new HashMap<Port,Integer>();
        for (int i = 0; i <= 64 * 10; i += 10) {
            for (int j = 0; j < 10; j += 10) {
            	Port port1=new Port(10 + i, 10 + j, Port.INOUT, 1);
            	Port port2=new Port(10 + i, 20 + j, Port.INOUT, 1);
                connected.put(port1,new Integer(1));
                connected.put(port2,new Integer(2));
                ports.add(port1);
                ports.add(port2);
                
                Port port3=new Port(10 + i, 170 + j, Port.INOUT, 1);
            	Port port4=new Port(10 + i, 180 + j, Port.INOUT, 1);
                connected.put(port3,new Integer(3));
                connected.put(port4,new Integer(4));
                ports.add(port3);
                ports.add(port4);
            }
        }
        
        //falta agregar estos ports al Hash con el mismo valor int si es que estan conectados
        //
        for (int i = 0; i <= 64 * 10; i += 10) {
            for (int j = 0; j < 4 * 10; j += 10) {
                ports.add(new Port(10 + i, 50 + j, Port.INOUT, 1));
                ports.add(new Port(10 + i, 110 + j, Port.INOUT, 1));
            }
        }
        setPorts(ports);
        createAttributeSet();
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
        
        //las lineas tipicas de las proto
        g.setColor(Color.red);
        g.drawLine(x+10,y+26, x+650, y+26);
        g.drawLine(x+10,y+186, x+650, y+186);
        g.setColor(Color.blue);
        g.drawLine(x+10,y+3, x+650, y+3);
        g.drawLine(x+10,y+164, x+650, y+164);
        
        painter.drawPorts();
    }

    @Override
    public void propagate(InstanceState state) {
        // TODO Auto-generated method stub  
    }
    
    public HashMap<Port,Integer> getConnected(){
    	return connected;
    }
    
    public AttributeSet createAttributeSet() {
        TunnelProtoboardAttributes atts= new TunnelProtoboardAttributes();
        atts.setConnected(connected);
        atts.setPorts(ports);
        return atts;
    }

}
