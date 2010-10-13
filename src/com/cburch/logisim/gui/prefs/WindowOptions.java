/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

import javax.swing.JPanel;

import com.cburch.logisim.data.Direction;
import com.cburch.logisim.prefs.LogisimPreferences;
import com.cburch.logisim.util.TableLayout;

class WindowOptions extends OptionsPanel {
	private PrefBoolean[] checks;
	private PrefOptionList toolbarPlacement;

	public WindowOptions(PreferencesFrame window) {
		super(window);
		
		checks = new PrefBoolean[] {
				new PrefBoolean(LogisimPreferences.SHOW_TICK_RATE,
						Strings.getter("windowTickRate")),
				new PrefBoolean(LogisimPreferences.SHOW_PROJECT_TOOLBAR,
						Strings.getter("windowProjectToolbar")),
			};

		toolbarPlacement = new PrefOptionList(LogisimPreferences.TOOLBAR_PLACEMENT,
				Strings.getter("windowToolbarLocation"),
				new PrefOption[] {
					new PrefOption(Direction.NORTH.toString(),
							Direction.NORTH.getDisplayGetter()),
					new PrefOption(Direction.SOUTH.toString(),
							Direction.SOUTH.getDisplayGetter()),
					new PrefOption(Direction.EAST.toString(),
							Direction.EAST.getDisplayGetter()),
					new PrefOption(Direction.WEST.toString(),
							Direction.WEST.getDisplayGetter()),
					new PrefOption(LogisimPreferences.TOOLBAR_DOWN_MIDDLE,
							Strings.getter("windowToolbarDownMiddle")),
					new PrefOption(LogisimPreferences.TOOLBAR_HIDDEN,
							Strings.getter("windowToolbarHidden")) });
		
		JPanel panel = new JPanel(new TableLayout(2));
		panel.add(toolbarPlacement.getJLabel());
		panel.add(toolbarPlacement.getJComboBox());

		setLayout(new TableLayout(1));
		for (int i = 0; i < checks.length; i++) {
			add(checks[i]);
		}
		add(panel);
	}

	@Override
	public String getTitle() {
		return Strings.get("windowTitle");
	}

	@Override
	public String getHelpText() {
		return Strings.get("windowHelp");
	}
	
	@Override
	public void localeChanged() {
		for (int i = 0; i < checks.length; i++) {
			checks[i].localeChanged();
		}
		toolbarPlacement.localeChanged();
	}
}
