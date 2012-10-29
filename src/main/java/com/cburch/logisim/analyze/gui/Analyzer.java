/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.cburch.logisim.analyze.model.AnalyzerModel;
import com.cburch.logisim.gui.generic.LFrame;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import static com.cburch.logisim.util.LocaleString._;

public class Analyzer extends LFrame {
	// used by circuit analysis to select the relevant tab automatically.
	public static final int INPUTS_TAB = 0;
	public static final int OUTPUTS_TAB = 1;
	public static final int TABLE_TAB = 2;
	public static final int EXPRESSION_TAB = 3;
	public static final int MINIMIZED_TAB = 4;
	
	private class MyListener implements LocaleListener {
		public void localeChanged() {
			Analyzer.this.setTitle(_("analyzerWindowTitle"));
			tabbedPane.setTitleAt(INPUTS_TAB, _("inputsTab"));
			tabbedPane.setTitleAt(OUTPUTS_TAB, _("outputsTab"));
			tabbedPane.setTitleAt(TABLE_TAB, _("tableTab"));
			tabbedPane.setTitleAt(EXPRESSION_TAB, _("expressionTab"));
			tabbedPane.setTitleAt(MINIMIZED_TAB, _("minimizedTab"));
			tabbedPane.setToolTipTextAt(INPUTS_TAB, _("inputsTabTip"));
			tabbedPane.setToolTipTextAt(OUTPUTS_TAB, _("outputsTabTip"));
			tabbedPane.setToolTipTextAt(TABLE_TAB, _("tableTabTip"));
			tabbedPane.setToolTipTextAt(EXPRESSION_TAB, _("expressionTabTip"));
			tabbedPane.setToolTipTextAt(MINIMIZED_TAB, _("minimizedTabTip"));
			buildCircuit.setText(_("buildCircuitButton"));
			inputsPanel.localeChanged();
			outputsPanel.localeChanged();
			truthTablePanel.localeChanged();
			expressionPanel.localeChanged();
			minimizedPanel.localeChanged();
			buildCircuit.localeChanged();
		}
	}
	
	private class EditListener implements ActionListener, ChangeListener {
		private void register(LogisimMenuBar menubar) {
			menubar.addActionListener(LogisimMenuBar.CUT, this);
			menubar.addActionListener(LogisimMenuBar.COPY, this);
			menubar.addActionListener(LogisimMenuBar.PASTE, this);
			menubar.addActionListener(LogisimMenuBar.DELETE, this);
			menubar.addActionListener(LogisimMenuBar.SELECT_ALL, this);
			tabbedPane.addChangeListener(this);
			enableItems(menubar);
		}
		
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			Component c = tabbedPane.getSelectedComponent();
			if (c instanceof JScrollPane) {
				c = ((JScrollPane) c).getViewport().getView();
			}
			if (!(c instanceof TabInterface)) return;
			TabInterface tab = (TabInterface) c;
			if (src == LogisimMenuBar.CUT) {
				tab.copy();
				tab.delete();
			} else if (src == LogisimMenuBar.COPY) {
				tab.copy();
			} else if (src == LogisimMenuBar.PASTE) {
				tab.paste();
			} else if (src == LogisimMenuBar.DELETE) {
				tab.delete();
			} else if (src == LogisimMenuBar.SELECT_ALL) {
				tab.selectAll();
			}
		}
		
		private void enableItems(LogisimMenuBar menubar) {
			Component c = tabbedPane.getSelectedComponent();
			if (c instanceof JScrollPane) {
				c = ((JScrollPane) c).getViewport().getView();
			}
			boolean support = c instanceof TabInterface;
			menubar.setEnabled(LogisimMenuBar.CUT, support);
			menubar.setEnabled(LogisimMenuBar.COPY, support);
			menubar.setEnabled(LogisimMenuBar.PASTE, support);
			menubar.setEnabled(LogisimMenuBar.DELETE, support);
			menubar.setEnabled(LogisimMenuBar.SELECT_ALL, support);
		}

		public void stateChanged(ChangeEvent e) {
			enableItems((LogisimMenuBar) getJMenuBar());
			
			Object selected = tabbedPane.getSelectedComponent();
			if (selected instanceof JScrollPane) {
				selected = ((JScrollPane) selected).getViewport().getView();
			}
			if (selected instanceof AnalyzerTab) {
				((AnalyzerTab) selected).updateTab();
			}
		}
	}
	
	private MyListener myListener = new MyListener();
	private EditListener editListener = new EditListener();
	private AnalyzerModel model = new AnalyzerModel();
	private JTabbedPane tabbedPane = new JTabbedPane();
	
	private VariableTab inputsPanel;
	private VariableTab outputsPanel;
	private TableTab truthTablePanel;
	private ExpressionTab expressionPanel;
	private MinimizedTab minimizedPanel;
	private BuildCircuitButton buildCircuit;
	
	Analyzer() {
		inputsPanel = new VariableTab(model.getInputs());
		outputsPanel = new VariableTab(model.getOutputs());
		truthTablePanel = new TableTab(model.getTruthTable());
		expressionPanel = new ExpressionTab(model);
		minimizedPanel = new MinimizedTab(model);
		buildCircuit = new BuildCircuitButton(this, model);

		truthTablePanel.addMouseListener(new TruthTableMouseListener());
		
		tabbedPane = new JTabbedPane();
		addTab(INPUTS_TAB, inputsPanel);
		addTab(OUTPUTS_TAB, outputsPanel);
		addTab(TABLE_TAB, truthTablePanel);
		addTab(EXPRESSION_TAB, expressionPanel);
		addTab(MINIMIZED_TAB, minimizedPanel);
		
		Container contents = getContentPane();
		JPanel vertStrut = new JPanel(null);
		vertStrut.setPreferredSize(new Dimension(0, 300));
		JPanel horzStrut = new JPanel(null);
		horzStrut.setPreferredSize(new Dimension(450, 0));
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(buildCircuit);
		contents.add(vertStrut, BorderLayout.WEST);
		contents.add(horzStrut, BorderLayout.NORTH);
		contents.add(tabbedPane, BorderLayout.CENTER);
		contents.add(buttonPanel, BorderLayout.SOUTH);
		
		DefaultRegistry registry = new DefaultRegistry(getRootPane());
		inputsPanel.registerDefaultButtons(registry);
		outputsPanel.registerDefaultButtons(registry);
		expressionPanel.registerDefaultButtons(registry);
		
		LocaleManager.addLocaleListener(myListener);
		myListener.localeChanged();
		
		LogisimMenuBar menubar = new LogisimMenuBar(this, null);
		setJMenuBar(menubar);
		editListener.register(menubar);
	}
	
	private void addTab(int index, final JComponent comp) {
		final JScrollPane pane = new JScrollPane(comp,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		if (comp instanceof TableTab) {
			pane.setVerticalScrollBar(((TableTab) comp).getVerticalScrollBar());
		}
		pane.addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent event) {
				int width = pane.getViewport().getWidth();
				comp.setSize(new Dimension(width, comp.getHeight()));
			}

			public void componentMoved(ComponentEvent arg0) { }
			public void componentShown(ComponentEvent arg0) { }
			public void componentHidden(ComponentEvent arg0) { }
		});
		tabbedPane.insertTab("Untitled", null, pane, null, index);
	}
	
	public AnalyzerModel getModel() {
		return model;
	}
	
	public void setSelectedTab(int index) {
		Object found = tabbedPane.getComponentAt(index);
		if (found instanceof AnalyzerTab) {
			((AnalyzerTab) found).updateTab();
		}
		tabbedPane.setSelectedIndex(index);
	}
	
	public static void main(String[] args) {
		Analyzer frame = new Analyzer();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
