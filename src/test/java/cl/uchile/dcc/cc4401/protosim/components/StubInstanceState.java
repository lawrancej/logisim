package cl.uchile.dcc.cc4401.protosim.components;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.proj.Project;

public class StubInstanceState implements InstanceState {
	
	private Value[] values;

	public StubInstanceState(Value[] values) {
		this.values = values;
	}

	@Override
	public Instance getInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InstanceFactory getFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Project getProject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AttributeSet getAttributeSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> E getAttributeValue(Attribute<E> attr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value getPort(int portIndex) {
		return values[portIndex];
	}

	@Override
	public boolean isPortConnected(int portIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPort(int portIndex, Value value, int delay) {
		values[portIndex] = value;		
	}

	@Override
	public InstanceData getData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setData(InstanceData value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fireInvalidated() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCircuitRoot() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getTickCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
