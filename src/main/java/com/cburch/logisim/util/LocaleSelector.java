/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.util.Locale;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.cburch.logisim.prefs.AppPreferences;

class LocaleSelector extends JList
			implements LocaleListener, ListSelectionListener {
	private static class LocaleOption implements Runnable {
		private Locale locale;
		private String text;
		
		LocaleOption(Locale locale) {
			this.locale = locale;
			update(locale);
		}
		
		@Override
		public String toString() {
			return text;
		}

		void update(Locale current) {
			if (current != null && current.equals(locale)) {
				text = locale.getDisplayName(locale);
			} else {
				text = locale.getDisplayName(locale)
					+ " / " + locale.getDisplayName(current);
			}
		}
		
		public void run() {
			if (!LocaleManager.getLocale().equals(locale)) {
				LocaleManager.setLocale(locale);
				AppPreferences.LOCALE.set(locale.getLanguage());
			}
		}
	}

	private LocaleOption[] items;
	
	LocaleSelector(Locale[] locales) {
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		DefaultListModel model = new DefaultListModel();
		items = new LocaleOption[locales.length];
		for (int i = 0; i < locales.length; i++) {
			items[i] = new LocaleOption(locales[i]);
			model.addElement(items[i]);
		}
		setModel(model);
		setVisibleRowCount(Math.min(items.length, 8));
		LocaleManager.addLocaleListener(this);
		localeChanged();
		addListSelectionListener(this);
	}
	
	public void localeChanged() {
		Locale current = LocaleManager.getLocale();
		LocaleOption sel = null;
		for (int i = 0; i < items.length; i++) {
			items[i].update(current);
			if (current.equals(items[i].locale)) sel = items[i];
		}
		if (sel != null) {
			setSelectedValue(sel, true);
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		LocaleOption opt = (LocaleOption) getSelectedValue();
		if (opt != null) {
			SwingUtilities.invokeLater(opt);
		}
	}
}
