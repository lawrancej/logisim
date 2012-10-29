/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.file.FileStatistics;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.util.TableSorter;
import static com.cburch.logisim.util.LocaleString.*;

public class StatisticsDialog extends JDialog implements ActionListener {
	public static void show(JFrame parent, LogisimFile file, Circuit circuit) {
		FileStatistics stats = FileStatistics.compute(file, circuit);
		StatisticsDialog dlog = new StatisticsDialog(parent,
				circuit.getName(), new StatisticsTableModel(stats));
		dlog.setVisible(true);
	}
	
	private static class StatisticsTableModel extends AbstractTableModel {
		private FileStatistics stats;
		
		StatisticsTableModel(FileStatistics stats) {
			this.stats = stats;
		}

		public int getColumnCount() {
			return 5;
		}

		public int getRowCount() {
			return stats.getCounts().size() + 2;
		}
		
		@Override
		public Class<?> getColumnClass(int column) {
			return column < 2 ? String.class : Integer.class;
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0: return _("statsComponentColumn");
			case 1: return _("statsLibraryColumn");
			case 2: return _("statsSimpleCountColumn");
			case 3: return _("statsUniqueCountColumn");
			case 4: return _("statsRecursiveCountColumn");
			default: return "??"; // should never happen
			}
		}
		
		public Object getValueAt(int row, int column) {
			List<FileStatistics.Count> counts = stats.getCounts();
			int countsLen = counts.size();
			if (row < 0 || row >= countsLen + 2) return "";
			FileStatistics.Count count;
			if (row < countsLen) count = counts.get(row);
			else if (row == countsLen) count = stats.getTotalWithoutSubcircuits();
			else count = stats.getTotalWithSubcircuits();
			switch (column) {
			case 0:
				if (row < countsLen) {
					return count.getFactory().getDisplayName();
				} else if (row == countsLen) {
					return _("statsTotalWithout");
				} else {
					return _("statsTotalWith");
				}
			case 1: 
				if (row < countsLen) {
					Library lib = count.getLibrary();
					return lib == null ? "-" : lib.getDisplayName();
				} else {
					return "";
				}
			case 2: return Integer.valueOf(count.getSimpleCount());
			case 3: return Integer.valueOf(count.getUniqueCount());
			case 4: return Integer.valueOf(count.getRecursiveCount());
			default: return ""; // should never happen
			}
		}
	}
	
	private static class CompareString implements Comparator<String> {
		private String[] fixedAtBottom;
		
		public CompareString(String... fixedAtBottom) {
			this.fixedAtBottom = fixedAtBottom;
		}
		
		public int compare(String a, String b) {
			for (int i = fixedAtBottom.length - 1; i >= 0; i--) {
				String s = fixedAtBottom[i];
				if (a.equals(s)) return b.equals(s) ? 0 : 1;
				if (b.equals(s)) return -1;
			}
			return a.compareToIgnoreCase(b);
		}
	}
	
	private static class StatisticsTable extends JTable {
		@Override
		public void setBounds(int x, int y, int width, int height) {
			super.setBounds(x, y, width, height);
			setPreferredColumnWidths(new double[] { 0.45, 0.25, 0.1, 0.1, 0.1 });
		}
		
		protected void setPreferredColumnWidths(double[] percentages) {
			Dimension tableDim = getPreferredSize();
			
			double total = 0;
			for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
				total += percentages[i];
			}
			
			for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
				TableColumn column = getColumnModel().getColumn(i);
				double width = tableDim.width * (percentages[i] / total);
				column.setPreferredWidth((int) width);
			}
		}
	}
	
	private StatisticsDialog(JFrame parent, String circuitName,
			StatisticsTableModel model) {
		super(parent, true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(_("statsDialogTitle", circuitName));
		
		JTable table = new StatisticsTable();
		TableSorter mySorter = new TableSorter(model, table.getTableHeader());
		Comparator<String> comp = new CompareString("",
				_("statsTotalWithout"), _("statsTotalWith"));
		mySorter.setColumnComparator(String.class, comp);
		table.setModel(mySorter);
		JScrollPane tablePane = new JScrollPane(table);
		
		JButton button = new JButton(_("statsCloseButton"));
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
