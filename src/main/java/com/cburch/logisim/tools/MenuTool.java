/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;


import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.gui.main.Selection;
import com.cburch.logisim.gui.main.SelectionActions;
import com.cburch.logisim.proj.Project;

import java.util.Collection;
import static com.cburch.logisim.util.LocaleString.*;

public class MenuTool extends Tool {
	private class MenuComponent extends JPopupMenu
			implements ActionListener {
		Project proj;
		Circuit circ;
		Component comp;
		JMenuItem del = new JMenuItem(_("compDeleteItem"));
		JMenuItem attrs = new JMenuItem(_("compShowAttrItem"));

		MenuComponent(Project proj, Circuit circ, Component comp) {
			this.proj = proj;
			this.circ = circ;
			this.comp = comp;
			boolean canChange = proj.getLogisimFile().contains(circ);

			add(del); del.addActionListener(this);
			del.setEnabled(canChange);
			add(attrs); attrs.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if (src == del) {
				Circuit circ = proj.getCurrentCircuit();
				CircuitMutation xn = new CircuitMutation(circ);
				xn.remove(comp);
				proj.doAction(xn.toAction(__("removeComponentAction", comp.getFactory().getDisplayGetter())));
			} else if (src == attrs) {
				proj.getFrame().viewComponentAttributes(circ, comp);
			}
		}
	}

	private class MenuSelection extends JPopupMenu
			implements ActionListener {
		Project proj;
		JMenuItem del = new JMenuItem(_("selDeleteItem"));
		JMenuItem cut = new JMenuItem(_("selCutItem"));
		JMenuItem copy = new JMenuItem(_("selCopyItem"));

		MenuSelection(Project proj) {
			this.proj = proj;
			boolean canChange = proj.getLogisimFile().contains(proj.getCurrentCircuit());
			add(del); del.addActionListener(this);
			del.setEnabled(canChange);
			add(cut); cut.addActionListener(this);
			cut.setEnabled(canChange);
			add(copy); copy.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			Selection sel = proj.getSelection();
			if (src == del) {
				proj.doAction(SelectionActions.clear(sel));
			} else if (src == cut) {
				proj.doAction(SelectionActions.cut(sel));
			} else if (src == copy) {
				proj.doAction(SelectionActions.copy(sel));
			}
		}

		public void show(JComponent parent, int x, int y) {
			super.show(this, x, y);
		}
	}

	public MenuTool() { }
	
	@Override
	public boolean equals(Object other) {
		return other instanceof MenuTool;
	}
	
	@Override
	public int hashCode() {
		return MenuTool.class.hashCode();
	}

	@Override
	public String getName() { return "Menu Tool"; }

	@Override
	public String getDisplayName() { return _("menuTool"); }

	@Override
	public String getDescription() { return _("menuToolDesc"); }

	@Override
	public void mousePressed(Canvas canvas, Graphics g, MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		Location pt = Location.create(x, y);

		JPopupMenu menu;
		Project proj = canvas.getProject();
		Selection sel = proj.getSelection();
		Collection<Component> in_sel = sel.getComponentsContaining(pt, g);
		if (!in_sel.isEmpty()) {
			Component comp = in_sel.iterator().next();
			if (sel.getComponents().size() > 1) {
				menu = new MenuSelection(proj);
			} else {
				menu = new MenuComponent(proj,
					canvas.getCircuit(), comp);
				MenuExtender extender = (MenuExtender) comp.getFeature(MenuExtender.class);
				if (extender != null) extender.configureMenu(menu, proj);
			}
		} else {
			Collection<Component> cl = canvas.getCircuit().getAllContaining(pt, g);
			if (!cl.isEmpty()) {
				Component comp = cl.iterator().next();
				menu = new MenuComponent(proj,
					canvas.getCircuit(), comp);
				MenuExtender extender = (MenuExtender) comp.getFeature(MenuExtender.class);
				if (extender != null) extender.configureMenu(menu, proj);
			} else {
				menu = null;
			}
		}

		if (menu != null) {
			canvas.showPopupMenu(menu, x, y);
		}
	}

	@Override
	public void paintIcon(ComponentDrawContext c, int x, int y) {
		Graphics g = c.getGraphics();
		g.fillRect(x + 2, y + 1, 9, 2);
		g.drawRect(x + 2, y + 3, 15, 12);
		g.setColor(Color.lightGray);
		g.drawLine(x + 4, y + 2, x + 8, y + 2);
		for (int y_offs = y + 6; y_offs < y + 15; y_offs += 3) {
			g.drawLine(x + 4, y_offs, x + 14, y_offs);
		}
	}
}
