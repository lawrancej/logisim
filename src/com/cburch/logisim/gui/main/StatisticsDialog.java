/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.file.FileStatistics;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.util.TableSorter;

public class StatisticsDialog extends JDialog implements ActionListener {
	public static void show(Frame parent, LogisimFile file, Circuit circuit) {
		FileStatistics stats = FileStatistics.compute(file, circuit);
		List<FileStatistics.Count> counts = stats.getCounts();
		StatisticsDialog dlog = new StatisticsDialog(parent,
				circuit.getDisplayName(), new StatisticsTableModel(counts));
		dlog.setVisible(true);
	}
	
	private static class StatisticsTableModel extends AbstractTableModel {
		private List<FileStatistics.Count> counts;
		
		StatisticsTableModel(List<FileStatistics.Count> counts) {
			this.counts = counts;
		}

		public int getColumnCount() {
			return 5;
		}

		public int getRowCount() {
			return counts.size();
		}
		
		@Override
		public Class<?> getColumnClass(int column) {
			return column < 3 ? Integer.class : String.class;
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0: return Strings.get("statsSimpleCountColumn");
			case 1: return Strings.get("statsUniqueCountColumn");
			case 2: return Strings.get("statsRecursiveCountColumn");
			case 3: return Strings.get("statsComponentColumn");
			case 4: return Strings.get("statsLibraryColumn");
			default: return "??"; // should never happen
			}
		}
		
		public Object getValueAt(int row, int column) {
			if (row < 0 || row >= counts.size()) return "";
			FileStatistics.Count count = counts.get(row);
			switch (column) {
			case 0: return Integer.valueOf(count.getSimpleCount());
			case 1: return Integer.valueOf(count.getUniqueCount());
			case 2: return Integer.valueOf(count.getRecursiveCount());
			case 3: return count.getFactory().getDisplayName();
			case 4: 
				Library lib = count.getLibrary();
				return lib == null ? "-" : lib.getDisplayName();
			default: return ""; // should never happen
			}
		}
		
	}
	
	private StatisticsDialog(Frame parent, String circuitName,
			StatisticsTableModel model) {
		super(parent, true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(Strings.get("statsDialogTitle", circuitName));
		
		JTable table = new JTable();
		TableSorter mySorter = new TableSorter(model, table.getTableHeader());
		mySorter.setColumnComparator(String.class, String.CASE_INSENSITIVE_ORDER);
		table.setModel(mySorter);
		JScrollPane tablePane = new JScrollPane(table);
		
		JButton button = new JButton(Strings.get("statsCloseButton"));
		button.addActionListener(this);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(button);
		
		Container contents = this.getContentPane();
		contents.setLayout(new BorderLayout());
		contents.add(tablePane, BorderLayout.CENTER);
		contents.add(buttonPanel, BorderLayout.PAGE_END);
		this.pack();
		
		Dimension pref = contents.getPreferredSize();
		if (pref.width > 750 || pref.height > 550) {
			if (pref.width > 750) pref.width = 750;
			if (pref.height > 550) pref.height = 550;
			this.setSize(pref);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		this.dispose();
	}
}
