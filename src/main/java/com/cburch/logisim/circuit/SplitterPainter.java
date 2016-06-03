/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.util.GraphicsUtil;

import java.awt.*;

final class SplitterPainter {
    private static final int SPINE_WIDTH = Wire.WIDTH + 2;
    private static final int SPINE_DOT = Wire.WIDTH + 4;

    private SplitterPainter() {
    }

    static void drawLines(ComponentDrawContext context,
            SplitterAttributes attrs, Location origin) {
        boolean showState = context.getShowState();
        CircuitState state = showState ? context.getCircuitState() : null;
        if (state == null) {
            showState = false;
        }


        SplitterParameters parms = attrs.getParameters();
        int x0 = origin.getX();
        int y0 = origin.getY();
        int x = x0 + parms.getEnd0X();
        int y = y0 + parms.getEnd0Y();
        int dx = parms.getEndToEndDeltaX();
        int dy = parms.getEndToEndDeltaY();
        int dxEndSpine = parms.getEndToSpineDeltaX();
        int dyEndSpine = parms.getEndToSpineDeltaY();

        Graphics g = context.getGraphics();
        Color oldColor = g.getColor();
        GraphicsUtil.switchToWidth(g, Wire.WIDTH);
        for (int i = 0, n = (int) attrs.fanout; i < n; i++) {
            if (showState) {
                Value val = state.getValue(Location.create(x, y));
                g.setColor(val.getColor());
            }
            g.drawLine(x, y, x + dxEndSpine, y + dyEndSpine);
            x += dx;
            y += dy;
        }
        GraphicsUtil.switchToWidth(g, SPINE_WIDTH);
        g.setColor(oldColor);
        int spine0x = x0 + parms.getSpine0X();
        int spine0y = y0 + parms.getSpine0Y();
        int spine1x = x0 + parms.getSpine1X();
        int spine1y = y0 + parms.getSpine1Y();
        // centered
        if (spine0x == spine1x && spine0y == spine1y) {
            int fanout = (int) attrs.fanout;
            spine0x = x0 + parms.getEnd0X() + parms.getEndToSpineDeltaX();
            spine0y = y0 + parms.getEnd0Y() + parms.getEndToSpineDeltaY();
            spine1x = spine0x + (fanout - 1) * parms.getEndToEndDeltaX();
            spine1y = spine0y + (fanout - 1) * parms.getEndToEndDeltaY();
            // vertical spine
            if (parms.getEndToEndDeltaX() == 0) {
                if (spine0y < spine1y) {
                    spine0y++;
                    spine1y--;
                } else {
                    spine0y--;
                    spine1y++;
                }
                g.drawLine(x0 + parms.getSpine1X() / 4, y0, spine0x, y0);
            } else {
                if (spine0x < spine1x) {
                    spine0x++;
                    spine1x--;
                } else {
                    spine0x--;
                    spine1x++;
                }
                g.drawLine(x0, y0 + parms.getSpine1Y() / 4, x0, spine0y);
            }
            // spine is empty
            if (fanout <= 1) {
                int diam = SPINE_DOT;
                g.fillOval(spine0x - diam / 2, spine0y - diam / 2, diam, diam);
            } else {
                g.drawLine(spine0x, spine0y, spine1x, spine1y);
            }
        } else {
            int[] xSpine = { spine0x, spine1x, x0 + parms.getSpine1X() / 4 };
            int[] ySpine = { spine0y, spine1y, y0 + parms.getSpine1Y() / 4 };
            g.drawPolyline(xSpine, ySpine, 3);
        }
    }

    static void drawLabels(ComponentDrawContext context,
            SplitterAttributes attrs, Location origin) {
        // compute labels
        String[] ends = new String[(int) attrs.fanout + 1];
        int curEnd = -1;
        int cur0 = 0;
        for (int i = 0, n = attrs.bit_end.length; i <= n; i++) {
            int bit = (int) (i == n ? (byte) -1 : attrs.bit_end[i]);
            if (bit != curEnd) {
                int cur1 = i - 1;
                String toAdd;
                if (curEnd <= 0) {
                    toAdd = null;
                } else if (cur0 == cur1) {
                    toAdd = String.valueOf(cur0);
                } else {
                    toAdd = cur0 + "-" + cur1;
                }
                if (toAdd != null) {
                    String old = ends[curEnd];
                    if (old == null) {
                        ends[curEnd] = toAdd;
                    } else {
                        ends[curEnd] = old + ',' + toAdd;
                    }
                }
                curEnd = bit;
                cur0 = i;
            }
        }

        Graphics g = context.getGraphics().create();
        Font font = g.getFont();
        g.setFont(font.deriveFont(7.0f));

        SplitterParameters parms = attrs.getParameters();
        int x = origin.getX() + parms.getEnd0X() + parms.getEndToSpineDeltaX();
        int y = origin.getY() + parms.getEnd0Y() + parms.getEndToSpineDeltaY();
        int dx = parms.getEndToEndDeltaX();
        int dy = parms.getEndToEndDeltaY();
        if (parms.getTextAngle() != 0) {
            ((Graphics2D) g).rotate(Math.PI / 2.0);
            int t;
            t = -x; x = y; y = t;
            t = -dx; dx = dy; dy = t;
        }
        int halign = parms.getTextHorzAlign();
        int valign = parms.getTextVertAlign();
        x += (halign == GraphicsUtil.H_RIGHT ? -1 : 1) * (SPINE_WIDTH / 2 + 1);
        y += valign == GraphicsUtil.V_TOP ? 0 : -3;
        for (int i = 0, n = (int) attrs.fanout; i < n; i++) {
            String text = ends[i + 1];
            if (text != null) {
                GraphicsUtil.drawText(g, text, x, y, halign, valign);
            }
            x += dx;
            y += dy;
        }

        g.dispose();
    }

    static void drawLegacy(ComponentDrawContext context, SplitterAttributes attrs,
            Location origin) {
        Graphics g = context.getGraphics();
        CircuitState state = context.getCircuitState();
        Direction facing = attrs.facing;
        int fanout = (int) attrs.fanout;
        SplitterParameters parms = attrs.getParameters();

        g.setColor(Color.BLACK);
        int x0 = origin.getX();
        int y0 = origin.getY();
        int x1 = x0 + parms.getEnd0X();
        int y1 = y0 + parms.getEnd0Y();
        int dx = parms.getEndToEndDeltaX();
        int dy = parms.getEndToEndDeltaY();
        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            int ySpine = (y0 + y1) / 2;
            GraphicsUtil.switchToWidth(g, Wire.WIDTH);
            g.drawLine(x0, y0, x0, ySpine);
            int xi = x1;
            int yi = y1;
            for (int i = 1; i <= fanout; i++) {
                if (context.getShowState()) {
                    g.setColor(state.getValue(Location.create(xi, yi)).getColor());
                }
                int xSpine = xi + (xi == x0 ? 0 : (xi < x0 ? 10 : -10));
                g.drawLine(xi, yi, xSpine, ySpine);
                xi += dx;
                yi += dy;
            }
            if (fanout > 3) {
                GraphicsUtil.switchToWidth(g, SPINE_WIDTH);
                g.setColor(Color.BLACK);
                g.drawLine(x1 + dx, ySpine, x1 + (fanout - 2) * dx, ySpine);
            } else {
                g.setColor(Color.BLACK);
                g.fillOval(x0 - SPINE_DOT / 2, ySpine - SPINE_DOT / 2,
                        SPINE_DOT, SPINE_DOT);
            }
        } else {
            int xSpine = (x0 + x1) / 2;
            GraphicsUtil.switchToWidth(g, Wire.WIDTH);
            g.drawLine(x0, y0, xSpine, y0);
            int xi = x1;
            int yi = y1;
            for (int i = 1; i <= fanout; i++) {
                if (context.getShowState()) {
                    g.setColor(state.getValue(Location.create(xi, yi)).getColor());
                }
                int ySpine = yi + (yi == y0 ? 0 : (yi < y0 ? 10 : -10));
                g.drawLine(xi, yi, xSpine, ySpine);
                xi += dx;
                yi += dy;
            }
            if (fanout >= 3) {
                GraphicsUtil.switchToWidth(g, SPINE_WIDTH);
                g.setColor(Color.BLACK);
                g.drawLine(xSpine, y1 + dy, xSpine, y1 + (fanout - 2) * dy);
            } else {
                g.setColor(Color.BLACK);
                g.fillOval(xSpine - SPINE_DOT / 2, y0 - SPINE_DOT / 2,
                        SPINE_DOT, SPINE_DOT);
            }
        }
        GraphicsUtil.switchToWidth(g, 1);
    }
}