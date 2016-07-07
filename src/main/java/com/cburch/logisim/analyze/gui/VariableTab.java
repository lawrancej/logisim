/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.gui;

import com.cburch.logisim.analyze.model.VariableList;
import com.cburch.logisim.analyze.model.VariableListEvent;
import com.cburch.logisim.analyze.model.VariableListListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.cburch.logisim.util.LocaleString.getFromLocale;

class VariableTab extends AnalyzerTab implements TabInterface {
    private static class VariableListModel extends AbstractListModel<String>
            implements VariableListListener {
        private final VariableList list;
        private String[] listCopy;

        private VariableListModel(VariableList list) {
            this.list = list;
            updateCopy();
            list.addVariableListListener(this);
        }

        private void updateCopy() {
            listCopy = list.toArray(new String[list.size()]);
        }

        @Override
        public int getSize() {
            return listCopy.length;
        }

        @Override
        public String getElementAt(int index) {
            return index >= 0 && index < listCopy.length ? listCopy[index] : null;
        }

        private void update() {
            String[] oldCopy = listCopy;
            updateCopy();
            fireContentsChanged(this, 0, oldCopy.length);
        }

        @Override
        public void listChanged(VariableListEvent event) {
            String[] oldCopy = listCopy;
            updateCopy();
            int index;
            switch (event.getType()) {
            case VariableListEvent.ALL_REPLACED:
                fireContentsChanged(this, 0, oldCopy.length);
                return;
            case VariableListEvent.ADD:
                index = list.indexOf(event.getVariable());
                fireIntervalAdded(this, index, index);
                return;
            case VariableListEvent.REMOVE:
                index = (Integer) event.getData();
                fireIntervalRemoved(this, index, index);
                return;
            case VariableListEvent.MOVE:
                fireContentsChanged(this, 0, getSize());
                return;
            case VariableListEvent.REPLACE:
                index = (Integer) event.getData();
                fireContentsChanged(this, index, index);
                return;
            }
        }
    }

    private class MyListener
            implements ActionListener, DocumentListener, ListSelectionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            Object src = event.getSource();
            if ((src == add || src == field) && add.isEnabled()) {
                String name = field.getText().trim();
                if (!name.isEmpty()) {
                    data.add(name);
                    if (data.contains(name)) {
                        list.setSelectedValue(name, true);
                    }
                    field.setText("");
                    field.grabFocus();
                }
            } else if (src == rename) {
                String oldName = list.getSelectedValue();
                String newName = field.getText().trim();
                if (oldName != null && !newName.isEmpty()) {
                    data.replace(oldName, newName);
                    field.setText("");
                    field.grabFocus();
                }
            } else if (src == remove) {
                String name = list.getSelectedValue();
                if (name != null) {
                    data.remove(name);
                }

            } else if (src == moveUp) {
                String name = list.getSelectedValue();
                if (name != null) {
                    data.move(name, -1);
                    list.setSelectedValue(name, true);
                }
            } else if (src == moveDown) {
                String name = list.getSelectedValue();
                if (name != null) {
                    data.move(name, 1);
                    list.setSelectedValue(name, true);
                }
            }
        }

        @Override
        public void insertUpdate(DocumentEvent event) {
            computeEnabled();
        }
        @Override
        public void removeUpdate(DocumentEvent event) { insertUpdate(event); }
        @Override
        public void changedUpdate(DocumentEvent event) { insertUpdate(event); }

        @Override
        public void valueChanged(ListSelectionEvent event) {
            computeEnabled();
        }
    }

    private final VariableList data;

    private final JList<String> list = new JList<>();
    private final JTextField field = new JTextField();
    private final JButton remove = new JButton();
    private final JButton moveUp = new JButton();
    private final JButton moveDown = new JButton();
    private final JButton add = new JButton();
    private final JButton rename = new JButton();
    private final JLabel error = new JLabel(" ");

    VariableTab(VariableList data) {
        this.data = data;

        list.setModel(new VariableListModel(data));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        MyListener myListener = new MyListener();
        list.addListSelectionListener(myListener);
        remove.addActionListener(myListener);
        moveUp.addActionListener(myListener);
        moveDown.addActionListener(myListener);
        add.addActionListener(myListener);
        rename.addActionListener(myListener);
        field.addActionListener(myListener);
        field.getDocument().addDocumentListener(myListener);

        JScrollPane listPane = new JScrollPane(list,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listPane.setPreferredSize(new Dimension(100, 100));

        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        topPanel.add(remove);
        topPanel.add(moveUp);
        topPanel.add(moveDown);

        JPanel fieldPanel = new JPanel();
        fieldPanel.add(rename);
        fieldPanel.add(add);

        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        setLayout(gb);
        Insets oldInsets = gc.insets;

          gc.insets = new Insets(10, 10, 0, 0);
          gc.fill = GridBagConstraints.BOTH;
          gc.weightx = 1.0;
        gb.setConstraints(listPane, gc); add(listPane);

          gc.fill = GridBagConstraints.NONE;
          gc.anchor = GridBagConstraints.PAGE_START;
          gc.weightx = 0.0;
        gb.setConstraints(topPanel, gc); add(topPanel);

          gc.insets = new Insets(10, 10, 0, 10);
          gc.gridwidth = GridBagConstraints.REMAINDER;
          gc.gridx = 0;
          gc.gridy = GridBagConstraints.RELATIVE;
          gc.fill = GridBagConstraints.HORIZONTAL;
        gb.setConstraints(field, gc); add(field);

          gc.insets = oldInsets;
          gc.fill = GridBagConstraints.NONE;
          gc.anchor = GridBagConstraints.LINE_END;
        gb.setConstraints(fieldPanel, gc); add(fieldPanel);

          gc.fill = GridBagConstraints.HORIZONTAL;
        gb.setConstraints(error, gc); add(error);

        if (!data.isEmpty()) {
            list.setSelectedValue(data.get(0), true);
        }

        computeEnabled();
    }

    @Override
    void localeChanged() {
        remove.setText(getFromLocale("variableRemoveButton"));
        moveUp.setText(getFromLocale("variableMoveUpButton"));
        moveDown.setText(getFromLocale("variableMoveDownButton"));
        add.setText(getFromLocale("variableAddButton"));
        rename.setText(getFromLocale("variableRenameButton"));
        validateInput();
    }

    @Override
    void updateTab() {
        VariableListModel model = (VariableListModel) list.getModel();
        model.update();
    }

    void registerDefaultButtons(DefaultRegistry registry) {
        registry.registerDefaultButton(field, add);
    }

    private void computeEnabled() {
        int index = list.getSelectedIndex();
        int max = list.getModel().getSize();
        boolean selected = index >= 0 && index < max;
        remove.setEnabled(selected);
        moveUp.setEnabled(selected && index > 0);
        moveDown.setEnabled(selected && index < max);

        boolean ok = validateInput();
        add.setEnabled(ok && data.size() < data.getMaximumSize());
        rename.setEnabled(ok && selected);
    }

    private boolean validateInput() {
        String text = field.getText().trim();
        boolean ok = true;
        boolean errorShown = true;
        if (text.length() == 0) {
            errorShown = false;
            ok = false;
        } else if (!Character.isJavaIdentifierStart(text.charAt(0))) {
            error.setText(getFromLocale("variableStartError"));
            ok = false;
        } else {
            for (int i = 1; i < text.length() && ok; i++) {
                char c = text.charAt(i);
                if (!Character.isJavaIdentifierPart(c)) {
                    error.setText(getFromLocale("variablePartError", String.valueOf(c)));
                    ok = false;
                }
            }
        }
        if (ok) {
            for (int i = 0, n = data.size(); i < n && ok; i++) {
                String other = data.get(i);
                if (text.equals(other)) {
                    error.setText(getFromLocale("variableDuplicateError"));
                    ok = false;
                }
            }
        }
        if (ok || !errorShown) {
            if (data.size() >= data.getMaximumSize()) {
                error.setText(getFromLocale("variableMaximumError", String.valueOf(data.getMaximumSize())));
            } else {
                error.setText(" ");
            }
        }
        return ok;
    }

    @Override
    public void copy() {
        field.requestFocus();
        field.copy();
    }

    @Override
    public void paste() {
        field.requestFocus();
        field.paste();
    }

    @Override
    public void delete() {
        field.requestFocus();
        field.replaceSelection("");
    }

    @Override
    public void selectAll() {
        field.requestFocus();
        field.selectAll();
    }
}
