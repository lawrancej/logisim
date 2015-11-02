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
	HashMap<Port, Integer> connected;// si dos ports tienen el mismo valor estan
										// conectados

	public TunnelProtoboard() {
		super("TunnelProtoboard");

		ports = new ArrayList<Port>();
		connected = new HashMap<Port, Integer>();

		int pinGroup = 1;

		for (int i = 0; i <= 64 * 10; i += 10) {
			Port port = new Port(10 + i, 10, Port.INOUT, 1);
			ports.add(port);
			connected.put(port, pinGroup);

		}
		pinGroup++;
		for (int i = 0; i <= 64 * 10; i += 10) {
			Port port = new Port(10 + i, 20, Port.INOUT, 1);
			ports.add(port);
			connected.put(port, pinGroup);
		}
		pinGroup++;
		for (int i = 0; i <= 64 * 10; i += 10) {
			Port port = new Port(10 + i, 170, Port.INOUT, 1);
			ports.add(port);
			connected.put(port, pinGroup);
		}
		pinGroup++;
		for (int i = 0; i <= 64 * 10; i += 10) {
			Port port = new Port(10 + i, 180, Port.INOUT, 1);
			ports.add(port);
			connected.put(port, pinGroup);
		}
		pinGroup++;

		for (int i = 0; i <= 64 * 10; i += 10) {
			for (int j = 0; j < 4 * 10; j += 10) {
				Port port1 = new Port(10 + i, 50, Port.INOUT, 1);
				Port port2 = new Port(10 + i, 50 + 10, Port.INOUT, 1);
				Port port3 = new Port(10 + i, 50 + 20, Port.INOUT, 1);
				Port port4 = new Port(10 + i, 50 + 30, Port.INOUT, 1);
				Port port5 = new Port(10 + i, 110, Port.INOUT, 1);
				Port port6 = new Port(10 + i, 110 + 10, Port.INOUT, 1);
				Port port7 = new Port(10 + i, 110 + 20, Port.INOUT, 1);
				Port port8 = new Port(10 + i, 110 + 30, Port.INOUT, 1);

				ports.add(port1);
				ports.add(port2);
				ports.add(port3);
				ports.add(port4);
				ports.add(port5);
				ports.add(port6);
				ports.add(port7);
				ports.add(port8);

				connected.put(port1, pinGroup);
				connected.put(port2, pinGroup);
				connected.put(port3, pinGroup);
				connected.put(port4, pinGroup);
				pinGroup++;
				connected.put(port5, pinGroup);
				connected.put(port6, pinGroup);
				connected.put(port7, pinGroup);
				connected.put(port8, pinGroup);
				pinGroup++;
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

		// las lineas tipicas de las proto
		g.setColor(Color.red);
		g.drawLine(x + 10, y + 26, x + 650, y + 26);
		g.drawLine(x + 10, y + 186, x + 650, y + 186);
		g.setColor(Color.blue);
		g.drawLine(x + 10, y + 3, x + 650, y + 3);
		g.drawLine(x + 10, y + 164, x + 650, y + 164);

		painter.drawPorts();
	}

	@Override
	public void propagate(InstanceState state) {
		// TODO Auto-generated method stub
	}

	public HashMap<Port, Integer> getConnected() {
		return connected;
	}

	public AttributeSet createAttributeSet() {
		TunnelProtoboardAttributes atts = new TunnelProtoboardAttributes();
		atts.setConnected(connected);
		atts.setPorts(ports);
		return atts;
	}

}
