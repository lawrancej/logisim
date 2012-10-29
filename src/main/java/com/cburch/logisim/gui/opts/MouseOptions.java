/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.opts;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.cburch.logisim.file.MouseMappings;
import com.cburch.logisim.gui.generic.AttrTable;
import com.cburch.logisim.gui.generic.AttrTableModel;
import com.cburch.logisim.gui.generic.ProjectExplorer;
import com.cburch.logisim.gui.generic.ProjectExplorerEvent;
import com.cburch.logisim.gui.generic.ProjectExplorerListener;
import com.cburch.logisim.gui.generic.ProjectExplorerToolNode;
import com.cburch.logisim.gui.main.AttrTableToolModel;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.InputEventUtil;
import static com.cburch.logisim.util.LocaleString.*;

class MouseOptions extends OptionsPanel {
	private class AddArea extends JPanel {
		public AddArea() {
			setPreferredSize(new Dimension(75, 60));
			setMinimumSize(new Dimension(75, 60));
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(10, 10, 10, 10),
					BorderFactory.createEtchedBorder()));
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Dimension sz = getSize();
			g.setFont(remove.getFont());
			String label1;
			String label2;
			if (curTool == null) {
				g.setColor(Color.GRAY);
				label1 = _("mouseMapNone");
				label2 = null;
			} else {
				g.setColor(Color.BLACK);
				label1 = _("mouseMapText");
				label2 = _("mouseMapText2", curTool.getDisplayName());
			}
			FontMetrics fm = g.getFontMetrics();
			int x1 = (sz.width - fm.stringWidth(label1)) / 2;
			if (label2 == null) {
				int y = Math.max(0, (sz.height - fm.getHeight()) / 2 + fm.getAscent() - 2);
				g.drawString(label1, x1, y);
			} else {
				int x2 = (sz.width - fm.stringWidth(label2)) / 2;
				int y = Math.max(0, (sz.height - 2 * fm.getHeight()) / 2 + fm.getAscent() - 2);
				g.drawString(label1, x1, y);
				y += fm.getHeight();
				g.drawString(label2, x2, y);
			}
		}
	}
	
	private class MyListener
			implements ActionListener, MouseListener, ListSelectionListener,
				MouseMappings.MouseMappingsListener, ProjectExplorerListener {
		//
		// ActionListener method
		//
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if (src == remove) {
				int row = mappings.getSelectedRow();
				getProject().doAction(OptionsActions.removeMapping(getOptions().getMouseMappings(),
					model.getKey(row)));
				row = Math.min(row, model.getRowCount() - 1);
				if (row >= 0) setSelectedRow(row);
			}
		}

		//
		// MouseListener methods
		//
		public void mouseClicked(MouseEvent e) { }
		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e) { }
		public void mousePressed(MouseEvent e) {
			if (e.getSource() == addArea && curTool != null) {
				Tool t = curTool.cloneTool();
				Integer mods = Integer.valueOf(e.getModifiersEx());
				getProject().doAction(OptionsActions.setMapping(getOptions().getMouseMappings(), mods, t));
				setSelectedRow(model.getRow(mods));
			}
		}
		public void mouseReleased(MouseEvent e) { }

		//
		// ListSelectionListener method
		//
		public void valueChanged(ListSelectionEvent e) {
			int row = mappings.getSelectedRow();
			if (row < 0) {
				remove.setEnabled(false);
				attrTable.setAttrTableModel(null);
			} else {
				remove.setEnabled(true);
				Tool tool = model.getTool(row);
				Project proj = getProject();
				AttrTableModel model;
				if (tool.getAttributeSet() == null) {
					model = null;
				} else {
					model = new AttrTableToolModel(proj, tool);
				}
				attrTable.setAttrTableModel(model);
			}
		}

		//
		// MouseMappingsListener method
		//
		public void mouseMappingsChanged() {
			model.fireTableStructureChanged();
		}

		//
		// Explorer.Listener methods
		//
		public void selectionChanged(ProjectExplorerEvent event) {
			Object target = event.getTarget();
			if (target instanceof ProjectExplorerToolNode) {
				Tool tool = ((ProjectExplorerToolNode) target).getValue();
				setCurrentTool(tool);
			} else {
				setCurrentTool(null);
			}
		}

		public void doubleClicked(ProjectExplorerEvent event) { }
		public void moveRequested(ProjectExplorerEvent event, AddTool dragged, AddTool target) { }
		public void deleteRequested(ProjectExplorerEvent event) { }
		public JPopupMenu menuRequested(ProjectExplorerEvent event) { return null; }
	}

	private class MappingsModel extends AbstractTableModel {
		ArrayList<Integer> cur_keys;

		MappingsModel() {
			fireTableStructureChanged();
		}

		// AbstractTableModel methods
		@Override
		public void fireTableStructureChanged() {
			cur_keys = new ArrayList<Integer>(getOptions().getMouseMappings().getMappedModifiers());
			Collections.sort(cur_keys);
			super.fireTableStructureChanged();
		}

		public int getRowCount() {
			return cur_keys.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Object getValueAt(int row, int column) {
			Integer key = cur_keys.get(row);
			if (column == 0) {
				return InputEventUtil.toDisplayString(key.intValue());
			} else {
				Tool tool = getOptions().getMouseMappings().getToolFor(key);
				return tool.getDisplayName();
			}
		}

		// other methods
		Integer getKey(int row) {
			return cur_keys.get(row);
		}

		Tool getTool(int row) {
			if (row < 0 || row >= cur_keys.size()) return null;
			Integer key = cur_keys.get(row);
			return getOptions().getMouseMappings().getToolFor(key.intValue());
		}

		int getRow(Integer mods) {
			int row = Collections.binarySearch(cur_keys, mods);
			if (row < 0) row = -(row + 1);
			return row;
		}
	}

	private MyListener listener = new MyListener();
	private Tool curTool = null;
	private MappingsModel model;
	
	private ProjectExplorer explorer;
	private JPanel addArea = new AddArea();
	private JTable mappings = new JTable();
	private AttrTable attrTable;
	private JButton remove = new JButton();

	public MouseOptions(OptionsFrame window) {
		super(window, new GridLayout(1, 3));
		
		explorer = new ProjectExplorer(getProject());
		explorer.setListener(listener);

		// Area for adding mappings
		addArea.addMouseListener(listener);

		// Area for viewing current mappings
		model = new MappingsModel();
		mappings.setTableHeader(null);
		mappings.setModel(model);
		mappings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mappings.getSelectionModel().addListSelectionListener(listener);
		mappings.clearSelection();
		JScrollPane mapPane = new JScrollPane(mappings);

		// Button for removing current mapping
		JPanel removeArea = new JPanel();
		remove.addActionListener(listener);
		remove.setEnabled(false);
		removeArea.add(remove);

		// Area for viewing/changing attributes
		attrTable = new AttrTable(getOptionsFrame());

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(gridbag);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridheight = 4;
		gbc.fill = GridBagConstraints.BOTH;
		JScrollPane explorerPane = new JScrollPane(explorer,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		gridbag.setConstraints(explorerPane, gbc);
		add(explorerPane);
		gbc.weightx = 0.0;
		JPanel gap = new JPanel();
		gap.setPreferredSize(new Dimension(10, 10));
		gridbag.setConstraints(gap, gbc); add(gap);
		gbc.weightx = 1.0;
		gbc.gridheight = 1;
		gbc.gridx = 2;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.weighty = 0.0; gridbag.setConstraints(addArea, gbc); add(addArea);
		gbc.weighty = 1.0; gridbag.setConstraints(mapPane, gbc); add(mapPane);
		gbc.weighty = 0.0; gridbag.setConstraints(removeArea, gbc); add(removeArea);
		gbc.weighty = 1.0; gridbag.setConstraints(attrTable, gbc); add(attrTable);

		getOptions().getMouseMappings().addMouseMappingsListener(listener);
		setCurrentTool(null);
	}

	@Override
	public String getTitle() {
		return _("mouseTitle");
	}

	@Override
	public String getHelpText() {
		return _("mouseHelp");
	}
	
	@Override
	public void localeChanged() {
		remove.setText(_("mouseRemoveButton"));
		addArea.repaint();
	}

	private void setCurrentTool(Tool t) {
		curTool = t;
		localeChanged();
	}

	private void setSelectedRow(int row) {
		if (row < 0) row = 0;
		if (row >= model.getRowCount()) row = model.getRowCount() - 1;
		if (row >= 0) {
			mappings.getSelectionModel().setSelectionInterval(row, row);
		}
	}
}
