package com.cburch.logisim.std.gates;

import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.prefs.AppPreferences;
import org.junit.Test;

import java.awt.*;
import java.awt.geom.GeneralPath;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by dheid on 02.06.16.
 */
public class OrGateTest {

    private OrGate orGate = OrGate.FACTORY;

    @Test
    public void paintInstance() throws Exception {
        InstancePainter painter = mock(InstancePainter.class);

        GateAttributes gateAttributes = new GateAttributes(false);
        gateAttributes.facing = Direction.NORTH;
        gateAttributes.negated = 1;
        when(painter.getAttributeSet()).thenReturn(gateAttributes);

        Bounds bounds = Bounds.create(0, 0, 100, 100);
        when(painter.getOffsetBounds()).thenReturn(bounds);

        Graphics2D graphics2D = mock(Graphics2D.class);
        when(painter.getGraphics()).thenReturn(graphics2D);

        when(graphics2D.getColor()).thenReturn(Color.GREEN);

        Location location = mock(Location.class);
        when(painter.getLocation()).thenReturn(location);

        int locX = 10;
        when(location.getX()).thenReturn(locX);
        int locY = 20;
        when(location.getY()).thenReturn(locY);

        when(location.translate(anyInt(), anyInt())).thenReturn(location);
        when(location.translate(eq(Direction.NORTH), anyInt())).thenReturn(location);

        when(painter.getGateShape()).thenReturn(AppPreferences.SHAPE_SHAPED);

        orGate.setPaintInputLines(true);
        orGate.setNegateOutput(true);
        orGate.paintInstance(painter);

        verify(painter).drawDongle(5, 0);
        verify(painter).drawLabel();
        verify(painter).setBaseColor(Color.GREEN);
        verify(painter).rotate();

        verify(graphics2D, times(2)).setStroke(any(BasicStroke.class));
        verify(graphics2D).translate(-10, 0);
        verify(graphics2D, times(2)).draw(any(GeneralPath.class));

    }

}