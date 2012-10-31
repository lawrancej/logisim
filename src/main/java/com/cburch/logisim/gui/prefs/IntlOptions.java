/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.util.LocaleManager;
import static com.cburch.logisim.util.LocaleString.*;

class IntlOptions extends OptionsPanel {
	private static class RestrictedLabel extends JLabel {
		@Override
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}
	}

	private JLabel localeLabel = new RestrictedLabel();
	private JComponent locale;
	private PrefBoolean replAccents;
	private PrefOptionList gateShape;

	public IntlOptions(PreferencesFrame window) {
		super(window);
		
		locale = createLocaleSelector();
		replAccents = new PrefBoolean(AppPreferences.ACCENTS_REPLACE,
				__("intlReplaceAccents"));
		gateShape = new PrefOptionList(AppPreferences.GATE_SHAPE,
				__("intlGateShape"), new PrefOption[] {
					new PrefOption(AppPreferences.SHAPE_SHAPED,
							__("shapeShaped")),
					new PrefOption(AppPreferences.SHAPE_RECTANGULAR,
							__("shapeRectangular")),
					new PrefOption(AppPreferences.SHAPE_DIN40700,
							__("shapeDIN40700")) });
		
		Box localePanel = new Box(BoxLayout.X_AXIS);
		localePanel.add(Box.createGlue());
		localePanel.add(localeLabel);
		localeLabel.setMaximumSize(localeLabel.getPreferredSize());
		localeLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		localePanel.add(locale);
		locale.setAlignmentY(Component.TOP_ALIGNMENT);
		localePanel.add(Box.createGlue());
		
		JPanel shapePanel = new JPanel();
		shapePanel.add(gateShape.getJLabel());
		shapePanel.add(gateShape.getJComboBox());
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(Box.createGlue());
		add(shapePanel);
		add(localePanel);
		add(replAccents);
		add(Box.createGlue());
	}
	
	@Override
	public String getTitle() {
		return _("intlTitle");
	}

	@Override
	public String getHelpText() {
		return _("intlHelp");
	}
	
	@Override
	public void localeChanged() {
		gateShape.localeChanged();
		localeLabel.setText(_("intlLocale") + " ");
		replAccents.localeChanged();
		replAccents.setEnabled(LocaleManager.canReplaceAccents());
	}
}
