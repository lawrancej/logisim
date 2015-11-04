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

public class AndGate extends InstanceFactory {
	
	private List<Port> ports;
	
	public AndGate() {
		super("AndGate");
		setIconName("p_and.svg");
		
		/*ports = new ArrayList<Port>();
		ports.add(new Port(0, 0, Port.INPUT, 1));
		ports.add(new Port(10, 0, Port.INPUT, 1));
		ports.add(new Port(20, 0, Port.OUTPUT, 1));
		ports.add(new Port(0, 30, Port.INPUT, 1));
		ports.add(new Port(10, 30, Port.INPUT, 1));
		ports.add(new Port(20, 30, Port.OUTPUT, 1));
		setPorts(ports);
		*/
	}

	@Override
	public void paintInstance(InstancePainter painter) {
        Location loc = painter.getLocation();
        int x = loc.getX();
        int y = loc.getY();

        Graphics g = painter.getGraphics();
        g.setColor(Color.black);
        g.fillRect(x, y+5, 20, 20);
        g.setColor(Color.WHITE);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 10));
        g.drawString(" and", x, y+15);
        g.setColor(Color.GRAY);
        g.fillRect(x, y, 4, 5);
        g.fillRect(x+8, y, 4, 5);
        g.fillRect(x+16, y, 4, 5);
        g.fillRect(x, y+25, 4, 5);
        g.fillRect(x+8, y+25, 4, 5);
        g.fillRect(x+16, y+25, 4, 5);
        
        painter.drawPorts();
	}
	
    @Override
    public Bounds getOffsetBounds(AttributeSet attrs) {
        return Bounds.create(0, 0, 20, 30);
    }

	@Override
	public void propagate(InstanceState state) {
		// TODO Auto-generated method stub
	}

}