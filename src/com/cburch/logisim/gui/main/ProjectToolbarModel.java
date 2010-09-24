/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Icon;

import com.cburch.draw.toolbar.AbstractToolbarModel;
import com.cburch.draw.toolbar.ToolbarItem;
import com.cburch.draw.toolbar.ToolbarSeparator;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.util.Icons;
import com.cburch.logisim.util.StringGetter;
import com.cburch.logisim.util.UnmodifiableList;

class ProjectToolbarModel extends AbstractToolbarModel {
	private class Item implements ToolbarItem {
		private Icon icon;
		private Object action;
		private StringGetter toolTip;
		private boolean enabled;
		
		Item(String iconName, Object action, StringGetter toolTip) {
			this.icon = Icons.getIcon(iconName);
			this.action = action;
			this.toolTip = toolTip;
			this.enabled = false;
		}
		
		public boolean isSelectable() {
			return listener != null && enabled;
		}
		
		public void paintIcon(Component destination, Graphics g) {
			if (!isSelectable() && g instanceof Graphics2D) {
				Composite c = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
				((Graphics2D) g).setComposite(c);
			}

			if (icon == null) {
				g.setColor(new Color(255, 128, 128));
				g.fillRect(4, 4, 8, 8);
				g.setColor(Color.BLACK);
				g.drawLine(4, 4, 12, 12);
				g.drawLine(4, 12, 12, 4);
				g.drawRect(4, 4, 8, 8);
			} else {
				icon.paintIcon(destination, g, 0, 1);
			}
		}
		
		public String getToolTip() {
			if (toolTip != null) {
				return toolTip.get();
			} else {
				return null;
			}
		}
		
		public Dimension getDimension(Object orientation) {
			if (icon == null) {
				return new Dimension(16, 16);
			} else {
				int w = icon.getIconWidth();
				int h = icon.getIconHeight();
				return new Dimension(w, h + 2);
			}
		}
	}

	private Frame frame;
	private Item itemAdd;
	private Item itemUp;
	private Item itemDown;
	private Item itemDelete;
	private Item itemLayout;
	private Item itemAppearance;
	private List<ToolbarItem> items;
	private ActionListener listener;
	
	public ProjectToolbarModel(Frame frame) {
		this.frame = frame;
		
		itemAdd = new Item("projadd.gif", LogisimMenuBar.ADD_CIRCUIT,
				Strings.getter("projectAddCircuitTip"));
		itemUp = new Item("projup.gif", LogisimMenuBar.MOVE_CIRCUIT_UP,
				Strings.getter("projectMoveCircuitUpTip"));
		itemDown = new Item("projdown.gif", LogisimMenuBar.MOVE_CIRCUIT_DOWN,
				Strings.getter("projectMoveCircuitDownTip"));
		itemDelete = new Item("projdel.gif", LogisimMenuBar.REMOVE_CIRCUIT,
				Strings.getter("projectRemoveCircuitTip"));
		itemLayout = new Item("projlayo.gif", LogisimMenuBar.EDIT_LAYOUT,
				Strings.getter("projectEditLayoutTip"));
		itemAppearance = new Item("projapp.gif", LogisimMenuBar.EDIT_APPEARANCE,
				Strings.getter("projectEditAppearanceTip"));
		
		items = UnmodifiableList.create(new ToolbarItem[] {
				itemAdd,
				itemUp,
				itemDown,
				itemDelete,
				new ToolbarSeparator(4),
				itemLayout,
				itemAppearance,
			});
	}
	
	public void setActionListener(ActionListener value) {
		listener = value;
		fireToolbarAppearanceChanged();
	}
	
	void setEnabled(Object action, boolean value) {
		for (Object item : items) {
			if (item instanceof Item) {
				Item i = (Item) item;
				if (i.action.equals(action)) {
					if (i.enabled != value) {
						i.enabled = value;
						fireToolbarAppearanceChanged();
					}
					return;
				}
			}
		}
	}

	@Override
	public List<ToolbarItem> getItems() {
		return items;
	}
	
	@Override
	public boolean isSelected(ToolbarItem item) {
		String view = frame.getView();
		if (item == itemLayout) {
			return view.equals(Frame.LAYOUT);
		} else if (item == itemAppearance) {
			return view.equals(Frame.APPEARANCE);
		} else {
			return false;
		}
	}

	@Override
	public void itemSelected(ToolbarItem item) {
		if (listener != null && item instanceof Item) {
			Item i = (Item) item;
			if (i.enabled) {
				listener.actionPerformed(new ActionEvent(i.action,
						ActionEvent.ACTION_PERFORMED, i.action.toString()));
			}
		}
	}
}
