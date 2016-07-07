/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import com.cburch.logisim.circuit.*;
import com.cburch.logisim.gui.log.LogFrame;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.util.CustomAction;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static com.cburch.logisim.util.LocaleString.getFromLocale;

public class MenuSimulate extends Menu {
    private class TickFrequencyChoice extends JRadioButtonMenuItem
            implements ActionListener {
        private final double freq;

        private TickFrequencyChoice(double value) {
            freq = value;
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentSim != null) {
                currentSim.setTickFrequency(freq);
            }
        }

        public void localeChanged() {
            double f = freq;
            if (f < 1000.0) {
                String hzStr;
                if (Math.abs(f - (double) Math.round(f)) < 0.0001) {
                    hzStr = String.valueOf((int) Math.round(f));
                } else {
                    hzStr = String.valueOf(f);
                }
                setText(getFromLocale("simulateTickFreqItem", hzStr));
            } else {
                String kHzStr;
                double kf = (double) Math.round(f / 100.0) / 10.0;
                if (kf == (double) Math.round(kf)) {
                    kHzStr = String.valueOf((int) kf);
                } else {
                    kHzStr = String.valueOf(kf);
                }
                setText(getFromLocale("simulateTickKFreqItem", kHzStr));
            }
        }
    }

    private class CircuitStateMenuItem extends JMenuItem
            implements CircuitListener, ActionListener {
        private final CircuitState circuitState;

        private CircuitStateMenuItem(CircuitState circuitState) {
            this.circuitState = circuitState;

            Circuit circuit = circuitState.getCircuit();
            circuit.addCircuitListener(this);
            this.setText(circuit.getName());
            addActionListener(this);
        }

        void unregister() {
            Circuit circuit = circuitState.getCircuit();
            circuit.removeCircuitListener(this);
        }

        @Override
        public void circuitChanged(CircuitEvent event) {
            if (event.getAction() == CircuitEvent.ACTION_SET_NAME) {
                this.setText(circuitState.getCircuit().getName());
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            menubar.fireStateChanged(currentSim, circuitState);
        }
    }

    private class MyListener implements ActionListener, SimulatorListener, ChangeListener { 	
        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            Project proj = menubar.getProject();
            Simulator sim = proj == null ? null : proj.getSimulator();
            if (src == run || src == LogisimMenuBar.SIMULATE_ENABLE) {
                if (sim != null) {
                    sim.setIsRunning(!sim.isRunning());
                    proj.repaintCanvas();
                }
            } else if (src == reset) {
                if (sim != null) {
                    sim.requestReset();
                }

            } else if (src == step || src == LogisimMenuBar.SIMULATE_STEP) {
                if (sim != null) {
                    sim.step();
                }

            } else if (src == tickOnce || src == LogisimMenuBar.TICK_STEP) {
                if (sim != null) {
                    sim.tick();
                }

            } else if (src == ticksEnabled || src == LogisimMenuBar.TICK_ENABLE) {
                if (sim != null) {
                    sim.setIsTicking(!sim.isTicking());
                }

            } else if (src == log) {
                LogFrame frame = menubar.getProject().getLogFrame(true);
                frame.setVisible(true);
            }
        }

        @Override
        public void propagationCompleted(SimulatorEvent e) { }
        @Override
        public void tickCompleted(SimulatorEvent e) { }
        @Override
        public void simulatorStateChanged(SimulatorEvent e) {
            Simulator sim = e.getSource();
            if (sim != currentSim) {
                return;
            }

            computeEnabled();
            run.setSelected(sim.isRunning());
            ticksEnabled.setSelected(sim.isTicking());
            double freq = sim.getTickFrequency();
            for (TickFrequencyChoice item : tickFreqs) {
                item.setSelected(freq == item.freq);
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            step.setEnabled(run.isEnabled() && !run.isSelected());
        }
    }

    private final LogisimMenuBar menubar;
    private final MyListener myListener = new MyListener();
    private CircuitState currentState = null;
    private CircuitState bottomState = null;
    private Simulator currentSim = null;

    private final MenuItemCheckImpl run;
    private final JMenuItem reset = new JMenuItem();
    private final MenuItemImpl step;
    private final MenuItemCheckImpl ticksEnabled;
    private final MenuItemImpl tickOnce;
    private final JMenu tickFreq = new JMenu();
    private final TickFrequencyChoice[] tickFreqs = {
        new TickFrequencyChoice(4096.0),
        new TickFrequencyChoice(2048.0),
        new TickFrequencyChoice(1024.0),
        new TickFrequencyChoice(512.0),
        new TickFrequencyChoice(256.0),
        new TickFrequencyChoice(128.0),
        new TickFrequencyChoice(64.0),
        new TickFrequencyChoice(32.0),
        new TickFrequencyChoice(16.0),
        new TickFrequencyChoice(8.0),
        new TickFrequencyChoice(4.0),
        new TickFrequencyChoice(2.0),
        new TickFrequencyChoice(1.0),
        new TickFrequencyChoice(0.5),
        new TickFrequencyChoice(0.25),
    };
    private final JMenu downStateMenu = new JMenu();
    private final ArrayList<CircuitStateMenuItem> downStateItems
        = new ArrayList<>();
    private final JMenu upStateMenu = new JMenu();
    private final ArrayList<CircuitStateMenuItem> upStateItems
        = new ArrayList<>();
    private final JMenuItem log = new JMenuItem();

    public MenuSimulate(LogisimMenuBar menubar) {
        this.menubar = menubar;

        run = new MenuItemCheckImpl(this, LogisimMenuBar.SIMULATE_ENABLE);
        step = new MenuItemImpl(this, LogisimMenuBar.SIMULATE_STEP);
        ticksEnabled = new MenuItemCheckImpl(this, LogisimMenuBar.TICK_ENABLE);
        tickOnce = new MenuItemImpl(this, LogisimMenuBar.TICK_STEP);

        menubar.registerItem(LogisimMenuBar.SIMULATE_ENABLE, run);
        menubar.registerItem(LogisimMenuBar.SIMULATE_STEP, step);
        menubar.registerItem(LogisimMenuBar.TICK_ENABLE, ticksEnabled);
        menubar.registerItem(LogisimMenuBar.TICK_STEP, tickOnce);

        int menuMask = getToolkit().getMenuShortcutKeyMask();
        run.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_E, menuMask));
        reset.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_R, menuMask));
        step.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_I, menuMask));
        tickOnce.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_T, menuMask));
        ticksEnabled.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_K, menuMask));
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "Space");
        am.put("Space", new CustomAction("Space", this));
        
        ButtonGroup bgroup = new ButtonGroup();
        for (TickFrequencyChoice tickFreq1 : tickFreqs) {
            bgroup.add(tickFreq1);
            tickFreq.add(tickFreq1);
        }

        add(run);
        add(reset);
        add(step);
        addSeparator();
        add(upStateMenu);
        add(downStateMenu);
        addSeparator();
        add(tickOnce);
        add(ticksEnabled);
        add(tickFreq);
        addSeparator();
        add(log);

        setEnabled(false);
        run.setEnabled(false);
        reset.setEnabled(false);
        step.setEnabled(false);
        upStateMenu.setEnabled(false);
        downStateMenu.setEnabled(false);
        tickOnce.setEnabled(false);
        ticksEnabled.setEnabled(false);
        tickFreq.setEnabled(false);

        run.addChangeListener(myListener);
        menubar.addActionListener(LogisimMenuBar.SIMULATE_ENABLE, myListener);
        menubar.addActionListener(LogisimMenuBar.SIMULATE_STEP, myListener);
        menubar.addActionListener(LogisimMenuBar.TICK_ENABLE, myListener);
        menubar.addActionListener(LogisimMenuBar.TICK_STEP, myListener);
        // run.addActionListener(myListener);
        reset.addActionListener(myListener);
        // step.addActionListener(myListener);
        // tickOnce.addActionListener(myListener);
        // ticksEnabled.addActionListener(myListener);
        log.addActionListener(myListener);

        computeEnabled();
    }
    
    public void tick() {
    	Project proj = menubar.getProject();
        Simulator sim = proj == null ? null : proj.getSimulator();
        if (sim != null)
        	sim.tick();
    }

    public void localeChanged() {
        this.setText(getFromLocale("simulateMenu"));
        run.setText(getFromLocale("simulateRunItem"));
        reset.setText(getFromLocale("simulateResetItem"));
        step.setText(getFromLocale("simulateStepItem"));
        tickOnce.setText(getFromLocale("simulateTickOnceItem"));
        ticksEnabled.setText(getFromLocale("simulateTickItem"));
        tickFreq.setText(getFromLocale("simulateTickFreqMenu"));
        for (TickFrequencyChoice tickFreq1 : tickFreqs) {
            tickFreq1.localeChanged();
        }
        downStateMenu.setText(getFromLocale("simulateDownStateMenu"));
        upStateMenu.setText(getFromLocale("simulateUpStateMenu"));
        log.setText(getFromLocale("simulateLogItem"));
    }

    public void setCurrentState(Simulator sim, CircuitState value) {
        if (currentState == value) {
            return;
        }

        Simulator oldSim = currentSim;
        CircuitState oldState = currentState;
        currentSim = sim;
        currentState = value;
        if (bottomState == null) {
            bottomState = currentState;
        } else if (currentState == null) {
            bottomState = null;
        } else {
            CircuitState cur = bottomState;
            while (cur != null && cur != currentState) {
                cur = cur.getParentState();
            }
            if (cur == null) {
                bottomState = currentState;
            }

        }

        boolean oldPresent = oldState != null;
        boolean present = currentState != null;
        if (oldPresent != present) {
            computeEnabled();
        }

        if (currentSim != oldSim) {
            double freq = currentSim == null ? 1.0 : currentSim.getTickFrequency();
            for (TickFrequencyChoice tickFreq1 : tickFreqs) {
                tickFreq1.setSelected(Math.abs(tickFreq1.freq - freq) < 0.001);
            }

            if (oldSim != null) {
                oldSim.removeSimulatorListener(myListener);
            }

            if (currentSim != null) {
                currentSim.addSimulatorListener(myListener);
            }

            myListener.simulatorStateChanged(new SimulatorEvent(sim));
        }

        clearItems(downStateItems);
        CircuitState cur = bottomState;
        while (cur != null && cur != currentState) {
            downStateItems.add(new CircuitStateMenuItem(cur));
            cur = cur.getParentState();
        }
        if (cur != null) {
            cur = cur.getParentState();
        }

        clearItems(upStateItems);
        while (cur != null) {
            upStateItems.add(0, new CircuitStateMenuItem(cur));
            cur = cur.getParentState();
        }
        recreateStateMenus();
    }

    private void clearItems(List<CircuitStateMenuItem> items) {
        items.forEach(CircuitStateMenuItem::unregister);
        items.clear();
    }

    private void recreateStateMenus() {
        recreateStateMenu(downStateMenu, downStateItems, KeyEvent.VK_RIGHT);
        recreateStateMenu(upStateMenu, upStateItems, KeyEvent.VK_LEFT);
    }

    private void recreateStateMenu(JMenu menu,
                                   List<CircuitStateMenuItem> items, int code) {
        menu.removeAll();
        menu.setEnabled(items.size() > 0);
        boolean first = true;
        int mask = getToolkit().getMenuShortcutKeyMask();
        for (int i = items.size() - 1; i >= 0; i--) {
            JMenuItem item = items.get(i);
            menu.add(item);
            if (first) {
                item.setAccelerator(KeyStroke.getKeyStroke(code, mask));
                first = false;
            } else {
                item.setAccelerator(null);
            }
        }
    }

    @Override
    void computeEnabled() {
        boolean present = currentState != null;
        Simulator sim = this.currentSim;
        boolean simRunning = sim != null && sim.isRunning();
        setEnabled(present);
        run.setEnabled(present);
        reset.setEnabled(present);
        step.setEnabled(present && !simRunning);
        upStateMenu.setEnabled(present);
        downStateMenu.setEnabled(present);
        tickOnce.setEnabled(present);
        ticksEnabled.setEnabled(present && simRunning);
        tickFreq.setEnabled(present);
        menubar.fireEnableChanged();
    }
}
