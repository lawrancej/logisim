/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.util.TableLayout;
import com.sun.java.swing.plaf.motif.MotifLookAndFeel;
import static com.cburch.logisim.util.LocaleString.*;

@SuppressWarnings("serial")
class WindowOptions extends OptionsPanel {
    private PrefBoolean[] checks;
    private PrefOptionList toolbarPlacement;
    private PrefOptionList lookAndFeel;

    public WindowOptions(PreferencesFrame window) {
        super(window);

        checks = new PrefBoolean[] {
                new PrefBoolean(AppPreferences.SHOW_TICK_RATE,
                        getFromLocale("windowTickRate")),
            };

        toolbarPlacement = new PrefOptionList(AppPreferences.TOOLBAR_PLACEMENT,
                getFromLocale("windowToolbarLocation"),
                new PrefOption[] {
                    new PrefOption(Direction.NORTH.toString(),
                            Direction.NORTH.getDisplayGetter()),
                    new PrefOption(Direction.SOUTH.toString(),
                            Direction.SOUTH.getDisplayGetter()),
                    new PrefOption(Direction.EAST.toString(),
                            Direction.EAST.getDisplayGetter()),
                    new PrefOption(Direction.WEST.toString(),
                            Direction.WEST.getDisplayGetter()),
                    new PrefOption(AppPreferences.TOOLBAR_DOWN_MIDDLE,
                            getFromLocale("windowToolbarDownMiddle")),
                    new PrefOption(AppPreferences.TOOLBAR_HIDDEN,
                            getFromLocale("windowToolbarHidden")) });

        JPanel panel = new JPanel(new TableLayout(2));
        panel.add(toolbarPlacement.getJLabel());
        panel.add(toolbarPlacement.getJComboBox());
        

        setLayout(new TableLayout(1));
        for (int i = 0; i < checks.length; i++) {
            add(checks[i]);
        }
        add(panel);
        
        lookAndFeel = new PrefOptionList(AppPreferences.LOOK_AND_FEEL,
            getFromLocale("lookAndFeel"),
            new PrefOption[] {
                new PrefOption(UIManager.getSystemLookAndFeelClassName(), getFromLocale("systemLookAndFeel")),
                new PrefOption(NimbusLookAndFeel.class.getName(), getFromLocale("nimbusLookAndFeel")),
                new PrefOption(MotifLookAndFeel.class.getName(), getFromLocale("motifLookAndFeel")),
                new PrefOption(MetalLookAndFeel.class.getName(), getFromLocale("metalLookAndFeel")),
                 });
        panel.add(lookAndFeel.getJLabel());
        panel.add(lookAndFeel.getJComboBox());
    }

    @Override
    public String getTitle() {
        return getFromLocale("windowTitle");
    }

    @Override
    public String getHelpText() {
        return getFromLocale("windowHelp");
    }

    @Override
    public void localeChanged() {
        for (int i = 0; i < checks.length; i++) {
            checks[i].localeChanged();
        }
        toolbarPlacement.localeChanged();
    }
}
