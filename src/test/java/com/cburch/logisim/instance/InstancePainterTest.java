package com.cburch.logisim.instance;

import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.std.gates.GateAttributes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.awt.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by dheid on 03.06.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class InstancePainterTest {

    @Mock
    private ComponentDrawContext context;

    @Mock
    private Graphics2D graphics;

    @Mock
    private InstanceComponent component;

    private InstancePainter painter;

    @Before
    public void setup() {
        when(context.getGraphics()).thenReturn(graphics);
        painter = new InstancePainter(context, component);
    }

    @Test
    public void setBaseColor() throws Exception {
        painter.setBaseColor(Color.GREEN);
        verify(graphics).setColor(Color.GREEN);
    }

    @Test
    public void rotate() throws Exception {
        // given
        GateAttributes attributeSet = new GateAttributes(false);
        attributeSet.facing = Direction.NORTH;
        when(component.getAttributeSet()).thenReturn(attributeSet);

        // when
        painter.rotate();

        // then
        verify(graphics).rotate(-1.5707963267948966);
    }
}