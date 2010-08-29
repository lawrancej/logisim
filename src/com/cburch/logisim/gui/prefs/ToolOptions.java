/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.cburch.logisim.proj.LogisimPreferences;

class ToolOptions extends OptionsPanel {
    private class MyListener implements ActionListener, PropertyChangeListener {
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == afterAdd) {
                ComboOption x = (ComboOption) afterAdd.getSelectedItem();
                LogisimPreferences.setAfterAdd((String) x.getValue());
            } else if (src == showGhosts) {
                LogisimPreferences.setShowGhosts(showGhosts.isSelected());
            }
        }

        public void propertyChange(PropertyChangeEvent event) {
            String prop = event.getPropertyName();
            if (prop.equals(LogisimPreferences.AFTER_ADD)) {
                ComboOption.setSelected(afterAdd, LogisimPreferences.getAfterAdd());
            } else if (prop.equals(LogisimPreferences.SHOW_GHOSTS)) {
                showGhosts.setSelected(LogisimPreferences.getShowGhosts());
            }
        }
    }
    
    private MyListener myListener = new MyListener();

    private JLabel afterAddLabel = new JLabel();
    private JComboBox afterAdd = new JComboBox();
    private JCheckBox showGhosts = new JCheckBox();

    public ToolOptions(PreferencesFrame window) {
        super(window);
        
        afterAdd.addItem(new ComboOption(LogisimPreferences.AFTER_ADD_UNCHANGED, Strings.getter("afterAddUnchanged")));
        afterAdd.addItem(new ComboOption(LogisimPreferences.AFTER_ADD_EDIT, Strings.getter("afterAddEdit")));
        afterAdd.addActionListener(myListener);
        LogisimPreferences.addPropertyChangeListener(LogisimPreferences.AFTER_ADD,
                myListener);
        ComboOption.setSelected(afterAdd, LogisimPreferences.getAfterAdd());

        showGhosts.addActionListener(myListener);
        String propName = LogisimPreferences.SHOW_GHOSTS;
        LogisimPreferences.addPropertyChangeListener(propName, myListener);
        showGhosts.setSelected(LogisimPreferences.getShowGhosts());
        
        JPanel afterAddPanel = new JPanel();
        afterAddPanel.add(afterAddLabel);
        afterAddPanel.add(afterAdd);
        
        JPanel showGhostsPanel = new JPanel();
        showGhostsPanel.add(showGhosts);
        
        localeChanged();
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(Box.createGlue());
        add(afterAddPanel);
        add(showGhostsPanel);
        add(Box.createGlue());
    }

    @Override
    public String getTitle() {
        return Strings.get("toolTitle");
    }

    @Override
    public String getHelpText() {
        return Strings.get("toolHelp");
    }
    
    @Override
    public void localeChanged() {
        afterAddLabel.setText(Strings.get("afterAddLabel"));
        showGhosts.setText(Strings.get("showGhostsLabel"));
    }
}
