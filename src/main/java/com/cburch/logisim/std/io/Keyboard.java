/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.io;

import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import static com.cburch.logisim.util.LocaleString.getFromLocale;

public class Keyboard extends InstanceFactory {
    private static final int CLR = 0;
    private static final int CK = 1;
    private static final int RE = 2;
    private static final int AVL = 3;
    private static final int OUT = 4;

    private static final int DELAY0 = 9;
    private static final int DELAY1 = 11;

    static final int WIDTH = 145;
    private static final int HEIGHT = 25;

    private static final Font DEFAULT_FONT = new Font("monospaced", Font.PLAIN, 12);
    // control-L
    private static final char FORM_FEED = '\u000c';

    private static final Attribute<Integer> ATTR_BUFFER
        = Attributes.forIntegerRange("buflen",
                getFromLocale("keybBufferLengthAttr"), 1, 256);

    public Keyboard() {
        super("Keyboard", getFromLocale("keyboardComponent"));
        setAttributes(new Attribute[] { ATTR_BUFFER, StdAttr.EDGE_TRIGGER },
                new Object[] { Integer.valueOf(32), StdAttr.TRIG_RISING });
        setOffsetBounds(Bounds.create(0, -15, WIDTH, HEIGHT));
        setIconName("keyboard.svg");
        setInstancePoker(Poker.class);

        Port[] ps = new Port[5];
        ps[CLR] = new Port( 20, 10, Port.INPUT, 1);
        ps[CK]  = new Port(  0,  0, Port.INPUT, 1);
        ps[RE]  = new Port( 10, 10, Port.INPUT, 1);
        ps[AVL] = new Port(130, 10, Port.OUTPUT, 1);
        ps[OUT] = new Port(140, 10, Port.OUTPUT, 7);
        ps[CLR].setToolTip(getFromLocale("keybClearTip"));
        ps[CK].setToolTip(getFromLocale("keybClockTip"));
        ps[RE].setToolTip(getFromLocale("keybEnableTip"));
        ps[AVL].setToolTip(getFromLocale("keybAvailTip"));
        ps[OUT].setToolTip(getFromLocale("keybOutputTip"));
        setPorts(ps);
    }

    @Override
    public void propagate(InstanceState circState) {
        Object trigger = circState.getAttributeValue(StdAttr.EDGE_TRIGGER);
        KeyboardData state = getKeyboardState(circState);
        Value clear = circState.getPort(CLR);
        Value clock = circState.getPort(CK);
        Value enable = circState.getPort(RE);
        char c;

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
                if (go) state.dequeue();
            }

            c = state.getChar(0);
        }
        Value out = Value.createKnown(BitWidth.create(7), (int) c & 0x7F);
        circState.setPort(OUT, out, DELAY0);
        circState.setPort(AVL, (int) c != (int) '\0' ? Value.TRUE : Value.FALSE, DELAY1);
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        boolean showState = painter.getShowState();
        Graphics g = painter.getGraphics();
        painter.drawClock(CK, Direction.EAST);
        painter.drawBounds();
        painter.drawPort(CLR);
        painter.drawPort(RE);
        painter.drawPort(AVL);
        painter.drawPort(OUT);

        if (showState) {
            String str;
            int dispStart;
            int dispEnd;
            ArrayList<Integer> specials = new ArrayList<>();
            FontMetrics fm = null;
            KeyboardData state = getKeyboardState(painter);
            synchronized(state) {
                str = state.toString();
                for (int i = state.getNextSpecial(0); i >= 0; i = state.getNextSpecial(i + 1)) {
                    char c = state.getChar(i);
                    specials.add(c << 16 | i);
                }
                if (!state.isDisplayValid()) {
                    fm = g.getFontMetrics(DEFAULT_FONT);
                    state.updateDisplay(fm);
                }
                dispStart = state.getDisplayStart();
                dispEnd = state.getDisplayEnd();
            }

            if (str.length() > 0) {
                Bounds bds = painter.getBounds();
                drawBuffer(g, fm, str, dispStart, dispEnd, specials, bds);
            }
        } else {
            Bounds bds = painter.getBounds();
            int len = getBufferLength(painter.getAttributeValue(ATTR_BUFFER));
            String str = getFromLocale("keybDesc", String.valueOf(len));
            FontMetrics fm = g.getFontMetrics();
            int x = bds.getX() + (WIDTH - fm.stringWidth(str)) / 2;
            int y = bds.getY() + (HEIGHT + fm.getAscent()) / 2;
            g.drawString(str, x, y);
        }
    }

    private static void drawDots(Graphics g, int x, int y, int width) {
        int r = width / 10;
        if (r < 1) r = 1;
        int d = 2 * r;
        if (2 * r + 1 * d <= width) g.fillOval(x + r, y - d, d, d);
        if (3 * r + 2 * d <= width) g.fillOval(x + 2 * r + d, y - d, d, d);
        if (5 * r + 3 * d <= width) g.fillOval(x + 3 * r + 2 * d, y - d, d, d);
    }

    private void drawBuffer(Graphics g, FontMetrics fm, String str,
                            int dispStart, int dispEnd, ArrayList<Integer> specials, Bounds bds) {
        FontMetrics fm1 = fm;
        int x = bds.getX();
        int y = bds.getY();

        g.setFont(DEFAULT_FONT);
        if (fm1 == null) fm1 = g.getFontMetrics();
        int asc = fm1.getAscent();
        int x0 = x + 8;
        int ys = y + (HEIGHT + asc) / 2;
        int dotsWidth = fm1.stringWidth("m");
        int xs;
        if (dispStart > 0) {
            g.drawString(str.substring(0, 1), x0, ys);
            xs = x0 + fm1.stringWidth(str.charAt(0) + "m");
            drawDots(g, xs - dotsWidth, ys, dotsWidth);
            String sub = str.substring(dispStart, dispEnd);
            g.drawString(sub, xs, ys);
            if (dispEnd < str.length()) {
                drawDots(g, xs + fm1.stringWidth(sub), ys, dotsWidth);
            }
        } else if (dispEnd < str.length()) {
            String sub = str.substring(dispStart, dispEnd);
            xs = x0;
            g.drawString(sub, xs, ys);
            drawDots(g, xs + fm1.stringWidth(sub), ys, dotsWidth);
        } else {
            xs = x0;
            g.drawString(str, xs, ys);
        }

        if (specials.size() > 0) {
            drawSpecials(specials, x0, xs, ys, asc, g, fm1,
                    str, dispStart, dispEnd);
        }
    }

    private static void drawSpecials(Iterable<Integer> specials, int x0, int xs, int ys,
                                     int asc, Graphics g, FontMetrics fm,
                                     String str, int dispStart, int dispEnd) {
        int[] px = new int[3];
        int[] py = new int[3];
        for (Integer special : specials) {
            int code = special;
            int pos = code & 0xFF;
            int w0;
            int w1;
            if (pos == 0) {
                w0 = x0;
                w1 = x0 + fm.stringWidth(str.substring(0, 1));
            } else if (pos >= dispStart && pos < dispEnd) {
                w0 = xs + fm.stringWidth(str.substring(dispStart, pos));
                w1 = xs + fm.stringWidth(str.substring(dispStart, pos + 1));
            } else {
                // this character is not in current view
                continue;
            }
            w0++;
            w1--;

            int key = code >> 16;
            if (key == (int) '\b') {
                int y1 = ys - asc / 2;
                g.drawLine(w0, y1, w1, y1);
                px[0] = w0 + 3; py[0] = y1 - 3;
                px[1] = w0;     py[1] = y1;
                px[2] = w0 + 3; py[2] = y1 + 3;
                g.drawPolyline(px, py, 3);
            } else if (key == (int) '\n') {
                int y1 = ys - 3;
                px[0] = w1; py[0] = ys - asc;
                px[1] = w1; py[1] = y1;
                px[2] = w0; py[2] = y1;
                g.drawPolyline(px, py, 3);
                px[0] = w0 + 3; py[0] = y1 - 3;
                px[1] = w0;     py[1] = y1;
                px[2] = w0 + 3; py[2] = y1 + 3;
                g.drawPolyline(px, py, 3);
            } else if (key == (int) FORM_FEED) {
                g.drawRect(w0, ys - asc, w1 - w0, asc);
            }
        }
    }

    private static int getBufferLength(Object bufferAttr) {
        if (bufferAttr instanceof Integer) return (Integer) bufferAttr;
        else {
            return 32;
        }

    }

    private static KeyboardData getKeyboardState(InstanceState state) {
        int bufLen = getBufferLength(state.getAttributeValue(ATTR_BUFFER));
        KeyboardData ret = (KeyboardData) state.getData();
        if (ret == null) {
            ret = new KeyboardData(bufLen);
            state.setData(ret);
        } else {
            ret.updateBufferLength(bufLen);
        }
        return ret;
    }

    public static void addToBuffer(InstanceState state, char... newChars) {
        KeyboardData keyboardData = getKeyboardState(state);
        for (char newChar : newChars) {
            keyboardData.insert(newChar);
        }
    }

    private static class Poker extends InstancePoker {
        @Override
        public void keyPressed(InstanceState state, KeyEvent e) {
            KeyboardData data = getKeyboardState(state);
            boolean changed = false;
            boolean used = true;
            synchronized(data) {
                switch (e.getKeyCode()) {
                case KeyEvent.VK_DELETE: changed = data.delete(); break;
                case KeyEvent.VK_LEFT:   data.moveCursorBy(-1); break;
                case KeyEvent.VK_RIGHT:  data.moveCursorBy(1); break;
                case KeyEvent.VK_HOME:   data.setCursor(0); break;
                case KeyEvent.VK_END:    data.setCursor(Integer.MAX_VALUE); break;
                default: used = false;
                }
            }
            if (used) e.consume();
            if (changed) state.getInstance().fireInvalidated();
        }

        @Override
        public void keyTyped(InstanceState state, KeyEvent e) {
            KeyboardData data = getKeyboardState(state);
            char ch = e.getKeyChar();
            boolean changed = false;
            if ((int) ch != (int) KeyEvent.CHAR_UNDEFINED) {
                if (!Character.isISOControl(ch) || (int) ch == (int) '\b' || (int) ch == (int) '\n'
                        || (int) ch == (int) FORM_FEED) {
                    synchronized(data) { changed = data.insert(ch); }
                    e.consume();
                }
            }
            if (changed) state.getInstance().fireInvalidated();
        }

        public static void draw(InstancePainter painter) {
            KeyboardData data = getKeyboardState(painter);
            Bounds bds = painter.getInstance().getBounds();
            Graphics g = painter.getGraphics();
            FontMetrics fm = g.getFontMetrics(DEFAULT_FONT);

            String str;
            int cursor;
            int dispStart;
            synchronized(data) {
                str = data.toString();
                cursor = data.getCursorPosition();
                if (!data.isDisplayValid()) data.updateDisplay(fm);
                dispStart = data.getDisplayStart();
            }

            int asc = fm.getAscent();
            int x = bds.getX() + 8;
            if (dispStart > 0) {
                x += fm.stringWidth(str.charAt(0) + "m");
                x += fm.stringWidth(str.substring(dispStart, cursor));
            } else if (cursor >= str.length()) {
                x += fm.stringWidth(str);
            } else {
                x += fm.stringWidth(str.substring(0, cursor));
            }
            int y = bds.getY() + (bds.getHeight() + asc) / 2;
            g.drawLine(x, y - asc, x, y);
        }
    }
}
