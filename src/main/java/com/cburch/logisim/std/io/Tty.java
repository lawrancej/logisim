/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.io;

import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.*;
import com.cburch.logisim.util.GraphicsUtil;

import java.awt.*;

import static com.cburch.logisim.util.LocaleString.getFromLocale;

public class Tty extends InstanceFactory {
    private static final int CLR = 0;
    private static final int CK = 1;
    private static final int WE = 2;
    private static final int IN = 3;

    private static final int BORDER = 5;
    private static final int ROW_HEIGHT = 15;
    private static final int COL_WIDTH = 7;
    private static final Color DEFAULT_BACKGROUND = new Color(0, 0, 0, 64);

    private static final Font DEFAULT_FONT = new Font("monospaced", Font.PLAIN, 12);

    private static final Attribute<Integer> ATTR_COLUMNS
        = Attributes.forIntegerRange("cols",
            getFromLocale("ttyColsAttr"), 1, 120);
    private static final Attribute<Integer> ATTR_ROWS
        = Attributes.forIntegerRange("rows",
            getFromLocale("ttyRowsAttr"), 1, 48);

    public Tty() {
        super("TTY", getFromLocale("ttyComponent"));
        setAttributes(new Attribute[] {
                ATTR_ROWS, ATTR_COLUMNS, StdAttr.EDGE_TRIGGER,
                Io.ATTR_COLOR, Io.ATTR_BACKGROUND
            }, new Object[] {
                Integer.valueOf(8), Integer.valueOf(32), StdAttr.TRIG_RISING,
                Color.BLACK, DEFAULT_BACKGROUND
            });
        setIconName("tty.svg");

        Port[] ps = new Port[4];
        ps[CLR] = new Port(20,  10, Port.INPUT, 1);
        ps[CK]  = new Port( 0,   0, Port.INPUT, 1);
        ps[WE]  = new Port(10,  10, Port.INPUT, 1);
        ps[IN]  = new Port( 0, -10, Port.INPUT, 7);
        ps[CLR].setToolTip(getFromLocale("ttyClearTip"));
        ps[CK].setToolTip(getFromLocale("ttyClockTip"));
        ps[WE].setToolTip(getFromLocale("ttyEnableTip"));
        ps[IN].setToolTip(getFromLocale("ttyInputTip"));
        setPorts(ps);
    }

    @Override
    public Bounds getOffsetBounds(AttributeSet attrs) {
        int rows = getRowCount(attrs.getValue(ATTR_ROWS));
        int cols = getColumnCount(attrs.getValue(ATTR_COLUMNS));
        int width = 2 * BORDER + cols * COL_WIDTH;
        int height = 2 * BORDER + rows * ROW_HEIGHT;
        if (width < 30) width = 30;
        if (height < 30) height = 30;
        return Bounds.create(0, 10 - height, width, height);
    }

    @Override
    protected void configureNewInstance(Instance instance) {
        instance.addAttributeListener();
    }

    @Override
    protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
        if (attr == ATTR_ROWS || attr == ATTR_COLUMNS) {
            instance.recomputeBounds();
        }
    }

    @Override
    public void propagate(InstanceState circState) {
        Object trigger = circState.getAttributeValue(StdAttr.EDGE_TRIGGER);
        TtyState state = getTtyState(circState);
        Value clear = circState.getPort(CLR);
        Value clock = circState.getPort(CK);
        Value enable = circState.getPort(WE);
        Value in = circState.getPort(IN);

        synchronized(state) {
            Value lastClock = state.setLastClock(clock);
            if (clear == Value.TRUE) {
                state.clear();
            } else if (enable != Value.FALSE) {
                boolean go;
                if (trigger == StdAttr.TRIG_FALLING) {
                    go = lastClock == Value.TRUE && clock == Value.FALSE;
                } else {
                    go = lastClock == Value.FALSE && clock == Value.TRUE;
                }
                if (go) state.add(in.isFullyDefined() ? (char) in.toIntValue() : '?');
            }
        }
    }

    @Override
    public void paintGhost(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        GraphicsUtil.switchToWidth(g, 2);
        Bounds bds = painter.getBounds();
        g.drawRoundRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(),
                10, 10);
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        boolean showState = painter.getShowState();
        Graphics g = painter.getGraphics();
        Bounds bds = painter.getBounds();
        painter.drawClock(CK, Direction.EAST);
        if (painter.shouldDrawColor()) {
            g.setColor(painter.getAttributeValue(Io.ATTR_BACKGROUND));
            g.fillRoundRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(),
                    10, 10);
        }
        GraphicsUtil.switchToWidth(g, 2);
        g.setColor(Color.BLACK);
        g.drawRoundRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight(),
                2 * BORDER, 2 * BORDER);
        GraphicsUtil.switchToWidth(g, 1);
        painter.drawPort(CLR);
        painter.drawPort(WE);
        painter.drawPort(IN);

        int rows = getRowCount(painter.getAttributeValue(ATTR_ROWS));
        int cols = getColumnCount(painter.getAttributeValue(ATTR_COLUMNS));

        if (showState) {
            String[] rowData = new String[rows];
            int curRow;
            int curCol;
            TtyState state = getTtyState(painter);
            synchronized(state) {
                for (int i = 0; i < rows; i++) {
                    rowData[i] = state.getRowString(i);
                }
                curRow = state.getCursorRow();
                curCol = state.getCursorColumn();
            }

            g.setFont(DEFAULT_FONT);
            g.setColor(painter.getAttributeValue(Io.ATTR_COLOR));
            FontMetrics fm = g.getFontMetrics();
            int x = bds.getX() + BORDER;
            int y = bds.getY() + BORDER + (ROW_HEIGHT + fm.getAscent()) / 2;
            for (int i = 0; i < rows; i++) {
                g.drawString(rowData[i], x, y);
                if (i == curRow) {
                    int x0 = x + fm.stringWidth(rowData[i].substring(0, curCol));
                    g.drawLine(x0, y - fm.getAscent(), x0, y);
                }
                y += ROW_HEIGHT;
            }
        } else {
            String str = getFromLocale("ttyDesc", String.valueOf(rows), String.valueOf(cols));
            FontMetrics fm = g.getFontMetrics();
            int strWidth = fm.stringWidth(str);
            if (strWidth + BORDER > bds.getWidth()) {
                str = getFromLocale("ttyDescShort");
                strWidth = fm.stringWidth(str);
            }
            int x = bds.getX() + (bds.getWidth() - strWidth) / 2;
            int y = bds.getY() + (bds.getHeight() + fm.getAscent()) / 2;
            g.drawString(str, x, y);
        }
    }

    private static TtyState getTtyState(InstanceState state) {
        int rows = getRowCount(state.getAttributeValue(ATTR_ROWS));
        int cols = getColumnCount(state.getAttributeValue(ATTR_COLUMNS));
        TtyState ret = (TtyState) state.getData();
        if (ret == null) {
            ret = new TtyState(rows, cols);
            state.setData(ret);
        } else {
            ret.updateSize(rows, cols);
        }
        return ret;
    }

    public static void sendToStdout(InstanceState state) {
        TtyState tty = getTtyState(state);
        tty.setSendStdout(true);
    }

    private static int getRowCount(Object val) {
        if (val instanceof Integer) return (Integer) val;
        else {
            return 4;
        }

    }

    private static int getColumnCount(Object val) {
        if (val instanceof Integer) return (Integer) val;
        else {
            return 16;
        }

    }
}
