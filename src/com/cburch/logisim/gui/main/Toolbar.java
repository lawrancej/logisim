/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.file.ToolbarData;
import com.cburch.logisim.proj.LogisimPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.InputEventUtil;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;

import java.util.ArrayList;

class Toolbar extends JPanel implements LocaleListener {
	static final Object VERTICAL = new Object();
	static final Object HORIZONTAL = new Object();
	
	private static abstract class Item {
		int pos;
		Item(int pos) { this.pos = pos; }
		abstract int getWidth();
		abstract void paint(Graphics g, int x, int y);
		abstract void mouseClicked();
		abstract String getToolTipText();
	}

	private class Separator extends Item {
		Separator(int pos) {
			super(pos);
		}
		@Override
		int getWidth() { return 4; }
		@Override
		void paint(Graphics g, int x, int y) {
			g.setColor(Color.gray);
			int width = 2;
			int height = 2;
			if (orientation == VERTICAL) {
				x += 4;
				y += 1;
				width = Toolbar.this.getWidth() - 8;
			} else {
				x += 1;
				y += 4;
				height = Toolbar.this.getHeight() - 8;
			}
			g.fillRect(x, y, width, height);
		}
		@Override
		void mouseClicked() { }
		@Override
		String getToolTipText() { return null; }
	}

	private class ToolButton extends Item {
		Tool tool;

		ToolButton(Tool tool, int pos) {
			super(pos);
			this.tool = tool;
		}

		Tool getTool() {
			return tool;
		}

		@Override
		String getToolTipText() {
			String ret = tool.getDescription();
			int index = 1;
			for (Item item : contents) {
				if (item == this) break;
				if (item instanceof ToolButton) ++index;
			}
			if (index <= 10) {
				if (index == 10) index = 0;
				ret += " (" + InputEventUtil.toKeyDisplayString(getToolkit().getMenuShortcutKeyMask())
					+ "-" + index + ")";
			}
			return ret;
		}

		@Override
		int getWidth() {
			return inset_width;
		}

		@Override
		void paint(Graphics g, int x, int y) {
			inset.paintBorder(Toolbar.this, g, x, y,
				inset_width, inset_height);
			x += inset_left;
			y += inset_top;

			// draw background
			if (cur_down == this) {
				g.setColor(Color.GRAY);
				g.fillRect(x, y, 24, 24);
			} else {
				g.setColor(getBackground());
				g.fillRect(x, y, 24, 24);
			}

			// draw halo
			if (tool == haloedTool && proj.getFrame().getShowHalo()) {
				g.setColor(AttributeTable.HALO_COLOR);
				g.fillRect(x + 1, y + 1, 22, 22);
			}

			// draw selection indicator
			if (tool == proj.getTool()) {
				GraphicsUtil.switchToWidth(g, 2);
				g.setColor(Color.BLACK);
				g.drawRect(x, y, 24, 24);
				GraphicsUtil.switchToWidth(g, 1);
			}

			// draw tool icon
			g.setColor(Color.BLACK);
			Graphics g_copy = g.create();
			ComponentDrawContext c
				= new ComponentDrawContext(Toolbar.this, null, null, g, g_copy);
			tool.paintIcon(c, x + 2, y + 2);
			g_copy.dispose();
		}

		@Override
		void mouseClicked() {
			proj.setTool(tool);
			proj.getFrame().viewAttributes(tool);
		}
	}

	private class MyListener
			implements MouseListener, ProjectListener,
				ToolbarData.ToolbarListener, AttributeListener,
				PropertyChangeListener {
		//
		// MouseListener methods
		//
		public void mousePressed(MouseEvent e) {
			Item it = findItem(e);
			if (it == null) return;

			cur_down = it;
			repaint();
		}
		public void mouseReleased(MouseEvent e) {
			if (cur_down != null) {
				Item it = findItem(e);
				if (it == cur_down) cur_down.mouseClicked();
				cur_down = null;
				repaint();
			}
		}
		public void mouseClicked(MouseEvent e) { }
		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e) { }

		//
		// ProjectListener methods
		//
		public void projectChanged(ProjectEvent e) {
			int act = e.getAction();
			if (act == ProjectEvent.ACTION_SET_TOOL) {
				repaint();
			} else if (act == ProjectEvent.ACTION_SET_FILE) {
				LogisimFile old = (LogisimFile) e.getOldData();
				if (old != null) {
					ToolbarData data = old.getOptions().getToolbarData();
					data.removeToolbarListener(this);
					data.removeToolAttributeListener(this);
				}
				LogisimFile file = (LogisimFile) e.getData();
				if (file != null) {
					data = file.getOptions().getToolbarData();
					data.addToolbarListener(this);
					data.addToolAttributeListener(this);
				}
				contents.clear();
				remakeToolbar();
			}
		}

		//
		// ToolbarListener methods
		//
		public void toolbarChanged() {
			remakeToolbar();
		}
		
		//
		// AttributeListener methods
		//
		public void attributeListChanged(AttributeEvent e) { }
		public void attributeValueChanged(AttributeEvent e) {
			repaint();
		}
		
		//
		// PropertyChangeListener method
		//
		public void propertyChange(PropertyChangeEvent event) {
			String prop = event.getPropertyName();
			if (prop.equals(LogisimPreferences.GATE_SHAPE)) {
				repaint();
			}
		}
	}
	
	private class SelectAction implements KeyListener {
		int mask;
		
		SelectAction(int mask) {
			this.mask = mask;
		}

		public void keyTyped(KeyEvent event) {
		}

		public void keyPressed(KeyEvent event) {
			if ((event.getModifiers() & mask) != 0) {
				int selection = -1;
				switch (event.getKeyCode()) {
				case KeyEvent.VK_1: selection = 0; break;
				case KeyEvent.VK_2: selection = 1; break;
				case KeyEvent.VK_3: selection = 2; break;
				case KeyEvent.VK_4: selection = 3; break;
				case KeyEvent.VK_5: selection = 4; break;
				case KeyEvent.VK_6: selection = 5; break;
				case KeyEvent.VK_7: selection = 6; break;
				case KeyEvent.VK_8: selection = 7; break;
				case KeyEvent.VK_9: selection = 8; break;
				case KeyEvent.VK_0: selection = 9; break;
				}
				if (selection >= 0) {
					int current = 0;
					for (Item item : contents) {
						if (item instanceof ToolButton) {
						    if (current == selection) {
						        ToolButton b = (ToolButton) item;
						        b.mouseClicked();
						    }
						    ++current;
						}
					}
				}
			}
		}
		public void keyReleased(KeyEvent arg0) { }
	}

	private Project     proj;
	private ToolbarData data;
	private MyListener listener = new MyListener();
	private ArrayList<Item> contents = new ArrayList<Item>();
	private Item        cur_down = null;
	private Tool haloedTool = null;
	private Object orientation = HORIZONTAL;

	private Border      inset = BorderFactory.createEmptyBorder(2, 2, 2, 2);
	private int         inset_left;
	private int         inset_top;
	private int         inset_width;
	private int         inset_height;

	public Toolbar(Project proj) {
		this.proj = proj;
		this.data = proj.getOptions().getToolbarData();

		Insets ins = inset.getBorderInsets(this);
		inset_left   = ins.left;
		inset_top    = ins.top;
		inset_width  = ins.left + 24 + ins.right;
		inset_height = ins.top + 24 + ins.bottom;
		setPreferredSize(new Dimension(inset_width, inset_height));

		// set up listeners
		ToolbarData data = proj.getOptions().getToolbarData();
		data.addToolbarListener(listener);
		data.addToolAttributeListener(listener);
		LogisimPreferences.addPropertyChangeListener(LogisimPreferences.GATE_SHAPE, listener);
		proj.addProjectListener(listener);
		this.addMouseListener(listener);
		this.setToolTipText(Strings.get("toolbarDefaultToolTip"));

		remakeToolbar();
		LocaleManager.addLocaleListener(this);
	}
	
	public void registerShortcuts(JComponent component) {
		int imask = getToolkit().getMenuShortcutKeyMask();
		component.addKeyListener(new SelectAction(imask));
	}
	
	public void setOrientation(Object value) {
		if (value != VERTICAL && value != HORIZONTAL) {
			throw new IllegalArgumentException();
		}
		orientation = value;
	}

	public void setHaloedTool(Tool t) {
		if (haloedTool == t) return;
		haloedTool = t;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int x = 0;
		for (Item i : contents) {
			if (orientation == VERTICAL) {
				i.paint(g, 0, x);
			} else {
				i.paint(g, x, 0);
			}
			x += i.getWidth();
		}
	}

	@Override
	public String getToolTipText(MouseEvent e) {
		Item it = findItem(e);
		return it == null ? Strings.get("toolbarDefaultToolTip") : it.getToolTipText();
	}

	private Item findItem(MouseEvent e) {
		int ex = e.getX();
		int ey = e.getY();
		if (orientation == VERTICAL) {
			int t = ex; ex  = ey; ey = t;
		}
		if (ey < 0 && ey >= inset_height) return null;

		int x = 0;
		for (Item i : contents) {
			int wid = i.getWidth();
			if (x <= ex && ex < x + wid) return i;
			x += wid;
		}
		return null;
	}

	private void remakeToolbar() {
		// compute new contents
		ArrayList<Item> old_contents = contents;
		ArrayList<Item> new_contents = new ArrayList<Item>();
		int pos = -1;
		for (Tool tool : data.getContents()) {
			++pos;
			if (tool == null) {
				new_contents.add(new Toolbar.Separator(pos));
			} else {
				Item i = findButton(tool);
				if (i == null) {
					i = new ToolButton(tool, pos);
				} else {
					i.pos = pos;
					old_contents.remove(i);
				}
				new_contents.add(i);
			}
		}
		contents = new_contents;

		// now dispose of anything not retained previously
		Tool cur = proj.getTool();
		for (Item i : old_contents) {
			if (i instanceof ToolButton && ((ToolButton) i).tool == cur) {
				Tool t = data.getFirstTool();
				proj.setTool(t);
				proj.getFrame().viewAttributes(t);
			}
		}
		repaint();
	}

	private ToolButton findButton(Tool t) {
		for (Item i : contents) {
			if (i instanceof ToolButton) {
				ToolButton b = (ToolButton) i;
				if (b.tool == t) return b;
			}
		}
		return null;
	}

	public void localeChanged() {
		repaint();
	}

}
