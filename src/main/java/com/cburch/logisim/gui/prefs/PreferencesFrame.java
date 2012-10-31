/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

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

import com.cburch.logisim.gui.generic.LFrame;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.WindowMenuItemManager;
import static com.cburch.logisim.util.LocaleString.*;

public class PreferencesFrame extends LFrame {
	private static WindowMenuManager MENU_MANAGER = null;
	
	public static void initializeManager() {
		MENU_MANAGER = new WindowMenuManager();
	}
	
	private static class WindowMenuManager extends WindowMenuItemManager
			implements LocaleListener {
		private PreferencesFrame window = null;

		WindowMenuManager() {
			super(_("preferencesFrameMenuItem"), true);
			LocaleManager.addLocaleListener(this);
		}
		
		@Override
		public JFrame getJFrame(boolean create) {
			if (create) {
				if (window == null) {
					window = new PreferencesFrame();
					frameOpened(window);
				}
			}
			return window;
		}
		
		public void localeChanged() {
			setText(_("preferencesFrameMenuItem"));
		}
	}

	private class MyListener
			implements ActionListener, LocaleListener {
		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();
			if (src == close) {
				WindowEvent e = new WindowEvent(PreferencesFrame.this,
						WindowEvent.WINDOW_CLOSING);
				PreferencesFrame.this.processWindowEvent(e);
			}
		}
		
		public void localeChanged() {
			setTitle(_("preferencesFrameTitle"));
			for (int i = 0; i < panels.length; i++) {
				tabbedPane.setTitleAt(i, panels[i].getTitle());
				tabbedPane.setToolTipTextAt(i, panels[i].getToolTipText());
				panels[i].localeChanged();
			}
			close.setText(_("closeButton"));
		}
	}
	
	private MyListener myListener = new MyListener();
	
	private OptionsPanel[] panels;
	private JTabbedPane tabbedPane;
	private JButton close = new JButton();

	private PreferencesFrame() {
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setJMenuBar(new LogisimMenuBar(this, null));
		
		panels = new OptionsPanel[] {
				new TemplateOptions(this),
				new IntlOptions(this),
				new WindowOptions(this),
				new LayoutOptions(this),
				new ExperimentalOptions(this),
		};
		tabbedPane = new JTabbedPane();
		int intlIndex = -1;
		for (int index = 0; index < panels.length; index++) {
			OptionsPanel panel = panels[index];
			tabbedPane.addTab(panel.getTitle(), null, panel, panel.getToolTipText());
			if (panel instanceof IntlOptions) intlIndex = index;
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(close);
		close.addActionListener(myListener);

		Container contents = getContentPane();
		tabbedPane.setPreferredSize(new Dimension(450, 300));
		contents.add(tabbedPane, BorderLayout.CENTER);
		contents.add(buttonPanel, BorderLayout.SOUTH);
		
		if (intlIndex >= 0) tabbedPane.setSelectedIndex(intlIndex);

		LocaleManager.addLocaleListener(myListener);
		myListener.localeChanged();
		pack();
	}
	
	public static void showPreferences() {
		JFrame frame = MENU_MANAGER.getJFrame(true);
		frame.setVisible(true);
	}
}
