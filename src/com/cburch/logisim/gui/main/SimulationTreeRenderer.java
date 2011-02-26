/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentFactory;

public class SimulationTreeRenderer extends DefaultTreeCellRenderer {
	private static class RendererIcon implements Icon {
		private ComponentFactory factory;
		private boolean isCurrentView;
		
		RendererIcon(ComponentFactory factory, boolean isCurrentView) {
			this.factory = factory;
			this.isCurrentView = isCurrentView;
		}

		public int getIconHeight() {
			return 20;
		}

		public int getIconWidth() {
			return 20;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			ComponentDrawContext context = new ComponentDrawContext(c,
					null, null, g, g);
			factory.paintIcon(context, x, y, factory.createAttributeSet());

			// draw magnifying glass if appropriate
			if (isCurrentView) {
				int tx = x + 13;
				int ty = y + 13;
				int[] xp = { tx - 1, x + 18, x + 20, tx + 1 };
				int[] yp = { ty + 1, y + 20, y + 18, ty - 1 };
				g.setColor(ProjectExplorer.MAGNIFYING_INTERIOR);
				g.fillOval(x + 5, y + 5, 10, 10);
				g.setColor(Color.BLACK);
				g.drawOval(x + 5, y + 5, 10, 10);
				g.fillPolygon(xp, yp, xp.length);
			}
		}
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		Component ret = super.getTreeCellRendererComponent(tree, value,
				selected, expanded, leaf, row, hasFocus);
		SimulationTreeModel model = (SimulationTreeModel) tree.getModel();
		if (ret instanceof JLabel) {
			JLabel label = (JLabel) ret;
			if (value instanceof SimulationTreeNode) {
				SimulationTreeNode node = (SimulationTreeNode) value;
				ComponentFactory factory = node.getComponentFactory();
				if (factory != null) {
					label.setIcon(new RendererIcon(factory, node.isCurrentView(model)));
				}
			}
		}
		return ret;
	}
}
