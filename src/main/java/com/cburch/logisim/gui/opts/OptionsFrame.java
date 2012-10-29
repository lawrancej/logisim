/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.opts;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.file.LogisimFileActions;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.gui.generic.LFrame;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import static com.cburch.logisim.util.LocaleString.*;
import com.cburch.logisim.util.WindowMenuItemManager;

public class OptionsFrame extends LFrame {
	private class WindowMenuManager extends WindowMenuItemManager
			implements LocaleListener {
		WindowMenuManager() {
			super(_("optionsFrameMenuItem"), false);
		}
		
		@Override
		public JFrame getJFrame(boolean create) {
			return OptionsFrame.this;
		}
		
		public void localeChanged() {
			String title = project.getLogisimFile().getDisplayName();
			setText(_("optionsFrameMenuItem", title));
		}
	}

	private class MyListener
			implements ActionListener, LibraryListener, LocaleListener {
		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();
			if (src == revert) {
				getProject().doAction(LogisimFileActions.revertDefaults());
			} else if (src == close) {
				WindowEvent e = new WindowEvent(OptionsFrame.this,
						WindowEvent.WINDOW_CLOSING);
				OptionsFrame.this.processWindowEvent(e);
			}
		}

		public void libraryChanged(LibraryEvent event) {
			if (event.getAction() == LibraryEvent.SET_NAME) {
				setTitle(computeTitle(file));
				windowManager.localeChanged();
			}
		}
		
		public void localeChanged() {
			setTitle(computeTitle(file));
			for (int i = 0; i < panels.length; i++) {
				tabbedPane.setTitleAt(i, panels[i].getTitle());
				tabbedPane.setToolTipTextAt(i, panels[i].getToolTipText());
				panels[i].localeChanged();
			}
			revert.setText(_("revertButton"));
			close.setText(_("closeButton"));
			windowManager.localeChanged();
		}
	}
	
	private Project project;
	private LogisimFile file;
	private MyListener myListener = new MyListener();
	private WindowMenuManager windowManager = new WindowMenuManager();
	
	private OptionsPanel[] panels;
	private JTabbedPane tabbedPane;
	private JButton revert = new JButton();
	private JButton close = new JButton();

	public OptionsFrame(Project project) {
		this.project = project;
		this.file = project.getLogisimFile();
		file.addLibraryListener(myListener);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setJMenuBar(new LogisimMenuBar(this, project));
		
		panels = new OptionsPanel[] {
				new SimulateOptions(this),
				new ToolbarOptions(this),
				new MouseOptions(this),
		};
		tabbedPane = new JTabbedPane();
		for (int index = 0; index < panels.length; index++) {
			OptionsPanel panel = panels[index];
			tabbedPane.addTab(panel.getTitle(), null, panel, panel.getToolTipText());
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(revert);
		buttonPanel.add(close);
		revert.addActionListener(myListener);
		close.addActionListener(myListener);

		Container contents = getContentPane();
		tabbedPane.setPreferredSize(new Dimension(450, 300));
		contents.add(tabbedPane, BorderLayout.CENTER);
		contents.add(buttonPanel, BorderLayout.SOUTH);

		LocaleManager.addLocaleListener(myListener);
		myListener.localeChanged();
		pack();
	}
	
	public Project getProject() {
		return project;
	}
	
	public LogisimFile getLogisimFile() {
		return file;
	}
	
	public Options getOptions() {
		return file.getOptions();
	}
	
	@Override
	public void setVisible(boolean value) {
		if (value) {
			windowManager.frameOpened(this);
		}
		super.setVisible(value);
	}
	
	OptionsPanel[] getPrefPanels() {
		return panels;
	}
	
	private static String computeTitle(LogisimFile file) {
		String name = file == null ? "???" : file.getName();
		return _("optionsFrameTitle", name);
	}
}
