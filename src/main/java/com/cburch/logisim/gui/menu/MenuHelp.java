/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import com.cburch.logisim.gui.generic.LFrame;
import com.cburch.logisim.gui.start.About;
import com.cburch.logisim.util.MacCompatibility;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Locale;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import static com.cburch.logisim.util.LocaleString.*;

class MenuHelp extends JMenu implements ActionListener {
	private LogisimMenuBar menubar;
	private JMenuItem tutorial = new JMenuItem();
	private JMenuItem guide = new JMenuItem();
	private JMenuItem library = new JMenuItem();
	private JMenuItem about = new JMenuItem();
	private HelpSet helpSet;
	private String helpSetUrl = "";
	private JHelp helpComponent;
	private LFrame helpFrame;

	public MenuHelp(LogisimMenuBar menubar) {
		this.menubar = menubar;

		tutorial.addActionListener(this);
		guide.addActionListener(this);
		library.addActionListener(this);
		about.addActionListener(this);

		add(tutorial);
		add(guide);
		add(library);
		if (!MacCompatibility.isAboutAutomaticallyPresent()) {
			addSeparator();
			add(about);
		}
	}

	public void localeChanged() {
		this.setText(_("helpMenu"));
		if (helpFrame != null) {
			helpFrame.setTitle(_("helpWindowTitle"));
		}
		tutorial.setText(_("helpTutorialItem"));
		guide.setText(_("helpGuideItem"));
		library.setText(_("helpLibraryItem"));
		about.setText(_("helpAboutItem"));
		if (helpFrame != null) {
			helpFrame.setLocale(Locale.getDefault());
			loadBroker();
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == guide) {
			showHelp("guide");
		} else if (src == tutorial) {
			showHelp("tutorial");
		} else if (src == library) {
			showHelp("libs");
		} else if (src == about) {
			About.showAboutDialog(menubar.getParentWindow());
		}
	}
	
	private void loadBroker() {
		String helpUrl = _("helpsetUrl");
		if (helpUrl == null) helpUrl = "doc/doc_en.hs";
		if (helpSet == null || helpFrame == null || !helpUrl.equals(helpSetUrl)) {
			ClassLoader loader = MenuHelp.class.getClassLoader();
			try {
				URL hsURL = HelpSet.findHelpSet(loader, helpUrl);
				if (hsURL == null) {
					disableHelp();
					JOptionPane.showMessageDialog(menubar.getParentWindow(),
							_("helpNotFoundError"));
					return;
				}
				helpSetUrl = helpUrl;
				helpSet = new HelpSet(null, hsURL);
				helpComponent = new JHelp(helpSet);
				if (helpFrame == null) {
					helpFrame = new LFrame();
					helpFrame.setTitle(_("helpWindowTitle"));
					helpFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
					helpFrame.getContentPane().add(helpComponent);
					helpFrame.pack();
				} else {
					helpFrame.getContentPane().removeAll();
					helpFrame.getContentPane().add(helpComponent);
					helpComponent.revalidate();
				}
			} catch (Exception e) {
				disableHelp();
				e.printStackTrace();
				JOptionPane.showMessageDialog(menubar.getParentWindow(),
						_("helpUnavailableError"));
				return;
			}
		}
	}

	private void showHelp(String target) {
		loadBroker();
		try {
			helpComponent.setCurrentID(target);
			helpFrame.toFront();
			helpFrame.setVisible(true);
		} catch (Exception e) {
			disableHelp();
			e.printStackTrace();
			JOptionPane.showMessageDialog(menubar.getParentWindow(),
					_("helpDisplayError"));
		}
	}

	private void disableHelp() {
		guide.setEnabled(false);
		tutorial.setEnabled(false);
		library.setEnabled(false);
	}
}
