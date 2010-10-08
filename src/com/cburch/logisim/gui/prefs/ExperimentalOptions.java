/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.cburch.logisim.proj.LogisimPreferences;

class ExperimentalOptions extends OptionsPanel {
	private class MyListener implements ActionListener, PropertyChangeListener,
			ItemListener {
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if (src == accel) {
				ComboOption x = (ComboOption) accel.getSelectedItem();
				LogisimPreferences.setGraphicsAcceleration((String) x.getValue());
				/* This won't take effect until Logisim starts again, due to limitations
				 * of the rendering pipeline and Java's interaction with it. */
			}
		}

		public void propertyChange(PropertyChangeEvent event) {
			String prop = event.getPropertyName();
			if (prop.equals(LogisimPreferences.GRAPHICS_ACCELERATION)) {
				ComboOption.setSelected(accel, LogisimPreferences.getGraphicsAcceleration());
			} else if (prop.equals(LogisimPreferences.SHOW_TICK_RATE)) {
				tickRate.setSelected(LogisimPreferences.getShowTickRate());
			}
		}

		public void itemStateChanged(ItemEvent event) {
			Object src = event.getSource();
			if (src == tickRate) {
				LogisimPreferences.setShowTickRate(tickRate.isSelected());
			}
		}
	}
	
	private MyListener myListener = new MyListener();
	private JCheckBox tickRate = new JCheckBox();
	private JLabel accelLabel = new JLabel();
	private JLabel accelRestart = new JLabel();
	private JComboBox accel = new JComboBox();

	public ExperimentalOptions(PreferencesFrame window) {
		super(window);
		
		JPanel tickRatePanel = new JPanel();
		tickRatePanel.add(tickRate);
		
		JPanel accelPanel = new JPanel(new BorderLayout());
		accelPanel.add(accelLabel, BorderLayout.LINE_START);
		accelPanel.add(accel, BorderLayout.CENTER);
		accelPanel.add(accelRestart, BorderLayout.PAGE_END);
		accelRestart.setFont(accelRestart.getFont().deriveFont(Font.ITALIC));
		JPanel accelPanel2 = new JPanel();
		accelPanel2.add(accelPanel);
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(Box.createGlue());
		add(tickRatePanel);
		add(Box.createGlue());
		add(accelPanel2);
		add(Box.createGlue());
		
		accel.addItem(new ComboOption(LogisimPreferences.ACCEL_DEFAULT, Strings.getter("accelDefault")));
		accel.addItem(new ComboOption(LogisimPreferences.ACCEL_NONE, Strings.getter("accelNone")));
		accel.addItem(new ComboOption(LogisimPreferences.ACCEL_OPENGL, Strings.getter("accelOpenGL")));
		accel.addItem(new ComboOption(LogisimPreferences.ACCEL_D3D, Strings.getter("accelD3D")));
		accel.addActionListener(myListener);
		LogisimPreferences.addPropertyChangeListener(LogisimPreferences.GRAPHICS_ACCELERATION,
				myListener);
		tickRate.addItemListener(myListener);
		ComboOption.setSelected(accel, LogisimPreferences.getGraphicsAcceleration());
		tickRate.setSelected(LogisimPreferences.getShowTickRate());
	}

	@Override
	public String getTitle() {
		return Strings.get("experimentTitle");
	}

	@Override
	public String getHelpText() {
		return Strings.get("experimentHelp");
	}
	
	@Override
	public void localeChanged() {
		tickRate.setText(Strings.get("showTickRateLabel"));
		accelLabel.setText(Strings.get("accelLabel"));
		accelRestart.setText(Strings.get("accelRestartLabel"));
	}
}
