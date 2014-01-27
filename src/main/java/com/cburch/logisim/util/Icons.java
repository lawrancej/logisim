/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import com.cburch.draw.tools.SVGIcon;
import com.cburch.logisim.data.Direction;

public class Icons {
    private static final String path = "logisim/icons";

    private Icons() { }

    public static SVGIcon getIcon(String name) {
        return new SVGIcon(name);
    }

    public static void paintRotated(Graphics g, int x, int y, Direction dir, Icon icon, Component dest) {
        if (!(g instanceof Graphics2D) || dir == Direction.EAST) {
            icon.paintIcon(dest, g, x, y);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        double cx = x + icon.getIconWidth() / 2.0;
        double cy = y + icon.getIconHeight() / 2.0;
        if (dir == Direction.WEST) {
            g2.rotate( Math.PI, cx, cy);
        } else if (dir == Direction.NORTH) {
            g2.rotate(-Math.PI / 2.0, cx, cy);
        } else if (dir == Direction.SOUTH) {
            g2.rotate( Math.PI / 2.0, cx, cy);
        } else {
            g2.translate(-x, -y);
        }
        icon.paintIcon(dest, g2, x, y);
        g2.dispose();
    }
}
