/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.cburch.logisim.proj.LogisimPreferences;
import com.cburch.logisim.util.LocaleManager;

class IntlOptions extends OptionsPanel {
	private class MyListener implements ActionListener, PropertyChangeListener {
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if (src == gateShape) {
				ComboOption x = (ComboOption) gateShape.getSelectedItem();
				LogisimPreferences.setGateShape((String) x.getValue());
			} else if (src == replaceAccents) {
				LogisimPreferences.setAccentsReplace(replaceAccents.isSelected());
			}
		}

		public void propertyChange(PropertyChangeEvent event) {
			String prop = event.getPropertyName();
			if (prop.equals(LogisimPreferences.ACCENTS_REPLACE)) {
				replaceAccents.setSelected(LogisimPreferences.getAccentsReplace());
			} else if (prop.equals(LogisimPreferences.GATE_SHAPE)) {
				ComboOption.setSelected(gateShape, LogisimPreferences.getGateShape());
			}
		}
	}
	
	private static class RestrictedLabel extends JLabel {
		@Override
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}
	}
	
	private MyListener myListener = new MyListener();

	private JLabel localeLabel = new RestrictedLabel();
	private JComponent locale;
	private JCheckBox replaceAccents = new JCheckBox();
	private JLabel gateShapeLabel = new JLabel();
	private JComboBox gateShape = new JComboBox();

	public IntlOptions(PreferencesFrame window) {
		super(window);
		
		locale = Strings.createLocaleSelector();
		
		Box localePanel = new Box(BoxLayout.X_AXIS);
		localePanel.add(Box.createGlue());
		localePanel.add(localeLabel);
		localeLabel.setMaximumSize(localeLabel.getPreferredSize());
		localeLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		localePanel.add(locale);
		locale.setAlignmentY(Component.TOP_ALIGNMENT);
		localePanel.add(Box.createGlue());
		
		JPanel shapePanel = new JPanel();
		shapePanel.add(gateShapeLabel);
		shapePanel.add(gateShape);
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(Box.createGlue());
		add(shapePanel);
		add(localePanel);
		add(replaceAccents);
		add(Box.createGlue());
		
		replaceAccents.addActionListener(myListener);
		LogisimPreferences.addPropertyChangeListener(LogisimPreferences.ACCENTS_REPLACE,
				myListener);
		replaceAccents.setSelected(LogisimPreferences.getAccentsReplace());
		
		gateShape.addItem(new ComboOption(LogisimPreferences.SHAPE_SHAPED, Strings.getter("shapeShaped")));
		gateShape.addItem(new ComboOption(LogisimPreferences.SHAPE_RECTANGULAR, Strings.getter("shapeRectangular")));
		gateShape.addItem(new ComboOption(LogisimPreferences.SHAPE_DIN40700, Strings.getter("shapeDIN40700")));
		gateShape.addActionListener(myListener);
		LogisimPreferences.addPropertyChangeListener(LogisimPreferences.GATE_SHAPE,
				myListener);
		ComboOption.setSelected(gateShape, LogisimPreferences.getGateShape());
	}
	
	@Override
	public String getTitle() {
		return Strings.get("intlTitle");
	}

	@Override
	public String getHelpText() {
		return Strings.get("intlHelp");
	}
	
	@Override
	public void localeChanged() {
		localeLabel.setText(Strings.get("intlLocale") + " ");
		replaceAccents.setText(Strings.get("intlReplaceAccents"));
		replaceAccents.setEnabled(LocaleManager.canReplaceAccents());
		gateShapeLabel.setText(Strings.get("intlGateShape"));
	}
}
