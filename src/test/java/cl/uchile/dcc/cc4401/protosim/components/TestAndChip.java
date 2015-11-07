package cl.uchile.dcc.cc4401.protosim.components;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceState;

public class TestAndChip {

    private AndChip chip;

    @Before
    public void setUp() {
        chip = new AndChip();
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("AND Chip", chip.getDisplayName());
    }

    @Test
    public void testGetOffsetBounds() {
        Bounds bounds = chip.getOffsetBounds(null);
        assertEquals(0, bounds.getX());
        assertEquals(0, bounds.getY());
        assertEquals(20, bounds.getWidth());
        assertEquals(30, bounds.getHeight());
    }

    @Test
    public void testPropagateInputPortsWithUnknownValues() {
        // All unknown values
        InstanceState state = new StubInstanceState(new Value[] {
                Value.UNKNOWN,
                Value.UNKNOWN,
                Value.UNKNOWN,
                Value.UNKNOWN,
                Value.UNKNOWN,
                Value.UNKNOWN,
        });

        chip.propagate(state);

        assertEquals(0, state.getPort(2).toIntValue());
        assertEquals(0, state.getPort(5).toIntValue());

        // Port A unknown
        state = new StubInstanceState(new Value[] {
                Value.UNKNOWN,
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.UNKNOWN,
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
        });

        chip.propagate(state);
        
        assertEquals(0, state.getPort(2).toIntValue());
        assertEquals(0, state.getPort(5).toIntValue());


        // Port B unknown
        state = new StubInstanceState(new Value[] {
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.UNKNOWN,
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.UNKNOWN,
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
        });

        chip.propagate(state);
        
        assertEquals(0, state.getPort(2).toIntValue());
        assertEquals(0, state.getPort(5).toIntValue());
    }

    @Test
    public void testPropagateEmptyPortsValues() {
        InstanceState state = new StubInstanceState(new Value[] {
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 0),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 0),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 0),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 0),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 0),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 0),
        });

        chip.propagate(state);
        
        assertEquals(0, state.getPort(2).toIntValue());
        assertEquals(0, state.getPort(5).toIntValue());
    }
    
    @Test
    public void testPropagateOneInputPortWithValue() {
        // Port A
        InstanceState state = new StubInstanceState(new Value[] {
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 0),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 0),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 0),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 0),
        });

        chip.propagate(state);
        
        assertEquals(0, state.getPort(2).toIntValue());
        assertEquals(0, state.getPort(5).toIntValue());


        // Port B
        state = new StubInstanceState(new Value[] {
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 0),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 0),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 0),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 0),
        });

        chip.propagate(state);
        
        assertEquals(0, state.getPort(2).toIntValue());
        assertEquals(0, state.getPort(5).toIntValue());
    }

    @Test
    public void testPropagateBothInputPortsWithValue() {
        InstanceState state = new StubInstanceState(new Value[] {
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
                Value.createKnown(BitWidth.create(Breadboard.PORT_WIDTH), 1),
        });

        chip.propagate(state);
        
        assertEquals(1, state.getPort(2).toIntValue());
        assertEquals(1, state.getPort(5).toIntValue());
    }

}
