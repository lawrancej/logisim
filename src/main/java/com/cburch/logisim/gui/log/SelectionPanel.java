/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.log;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import static com.cburch.logisim.util.LocaleString.*;

class SelectionPanel extends LogPanel {
	private class Listener extends MouseAdapter
			implements ActionListener, TreeSelectionListener,
				ListSelectionListener {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				TreePath path = selector.getPathForLocation(e.getX(), e.getY());
				if (path != null && listener != null) {
					doAdd(selector.getSelectedItems());
				}
			}
		}

		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();
			if (src == addTool) {
				doAdd(selector.getSelectedItems());
			} else if (src == changeBase) {
				SelectionItem sel = (SelectionItem) list.getSelectedValue();
				if (sel != null) {
					int radix = sel.getRadix();
					switch (radix) {
					case 2:  sel.setRadix(10); break;
					case 10: sel.setRadix(16); break;
					default: sel.setRadix(2);
					}
				}
			} else if (src == moveUp) {
				doMove(-1);
			} else if (src == moveDown) {
				doMove(1);
			} else if (src == remove) {
				Selection sel = getSelection();
				Object[] toRemove = list.getSelectedValues();
				boolean changed = false;
				for (int i = 0; i < toRemove.length; i++) {
					int index = sel.indexOf((SelectionItem) toRemove[i]);
					if (index >= 0) {
						sel.remove(index);
						changed = true;
					}
				}
				if (changed) {
					list.clearSelection();
				}
			}
		}

		public void valueChanged(TreeSelectionEvent event) {
			computeEnabled();
		}

		public void valueChanged(ListSelectionEvent event) {
			computeEnabled();
		}
		
		private void computeEnabled() {
			int index = list.getSelectedIndex();
			addTool.setEnabled(selector.hasSelectedItems());
			changeBase.setEnabled(index >= 0);
			moveUp.setEnabled(index > 0);
			moveDown.setEnabled(index >= 0 && index < list.getModel().getSize() - 1);
			remove.setEnabled(index >= 0);
		}
		
		private void doAdd(List<SelectionItem> selectedItems) {
			if (selectedItems != null && selectedItems.size() > 0) {
				SelectionItem last = null;
				for (SelectionItem item : selectedItems) {
					getSelection().add(item);
					last = item;
				}
				list.setSelectedValue(last, true);
			}
		}
		
		private void doMove(int delta) {
			Selection sel = getSelection();
			int oldIndex = list.getSelectedIndex();
			int newIndex = oldIndex + delta;
			if (oldIndex >= 0 && newIndex >= 0 && newIndex < sel.size()) {
				sel.move(oldIndex, newIndex);
				list.setSelectedIndex(newIndex);
			}
		}
	}
	
	private Listener listener = new Listener();
	
	private ComponentSelector selector;
	private JButton addTool;
	private JButton changeBase;
	private JButton moveUp;
	private JButton moveDown;
	private JButton remove;
	private SelectionList list;
	
	public SelectionPanel(LogFrame window) {
		super(window);
		selector = new ComponentSelector(getModel());
		addTool = new JButton();
		changeBase = new JButton();
		moveUp = new JButton();
		moveDown = new JButton();
		remove = new JButton();
		list = new SelectionList();
		list.setSelection(getSelection());
		
		JPanel buttons = new JPanel(new GridLayout(5, 1));
		buttons.add(addTool);
		buttons.add(changeBase);
		buttons.add(moveUp);
		buttons.add(moveDown);
		buttons.add(remove);
		
		addTool.addActionListener(listener);
		changeBase.addActionListener(listener);
		moveUp.addActionListener(listener);
		moveDown.addActionListener(listener);
		remove.addActionListener(listener);
		selector.addMouseListener(listener);
		selector.addTreeSelectionListener(listener);
		list.addListSelectionListener(listener);
		listener.computeEnabled();
		
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(gridbag);
		JScrollPane explorerPane = new JScrollPane(selector,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollPane listPane = new JScrollPane(list,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gridbag.setConstraints(explorerPane, gbc); add(explorerPane);
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.weightx = 0.0;
		gridbag.setConstraints(buttons, gbc); add(buttons);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gridbag.setConstraints(listPane, gbc); add(listPane);
	}

	@Override
	public String getTitle() {
		return _("selectionTab");
	}

	@Override
	public String getHelpText() {
		return _("selectionHelp");
	}
	
	@Override
	public void localeChanged() {
		addTool.setText(_("selectionAdd"));
		changeBase.setText(_("selectionChangeBase"));
		moveUp.setText(_("selectionMoveUp"));
		moveDown.setText(_("selectionMoveDown"));
		remove.setText(_("selectionRemove"));
		selector.localeChanged();
		list.localeChanged();
	}

	@Override
	public void modelChanged(Model oldModel, Model newModel) {
		if (getModel() == null) {
			selector.setLogModel(newModel);
			list.setSelection(null);
		} else {
			selector.setLogModel(newModel);
			list.setSelection(getSelection());
		}
		listener.computeEnabled();
	}
}
