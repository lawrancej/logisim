/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.start;

import com.cburch.logisim.circuit.Analyze;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.Propagator;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.file.FileStatistics;
import com.cburch.logisim.file.LoadFailedException;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.io.Keyboard;
import com.cburch.logisim.std.io.Tty;
import com.cburch.logisim.std.memory.Ram;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.tools.Library;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.cburch.logisim.util.LocaleString.getFromLocale;

public final class TtyInterface {
    public static final int FORMAT_TABLE = 1;
    public static final int FORMAT_SPEED = 2;
    public static final int FORMAT_TTY = 4;
    public static final int FORMAT_HALT = 8;
    public static final int FORMAT_STATISTICS = 16;

    private static boolean lastIsNewline = true;

    private TtyInterface() {
    }

    public static void sendFromTty(char c) {
        lastIsNewline = (int) c == (int) '\n';
        //OK
        System.out.print(c);
    }

    private static void ensureLineTerminated() {
        if (!lastIsNewline) {
            lastIsNewline = true;
            //OK
            System.out.print('\n');
        }
    }

    public static void run(Startup args) {
        File fileToOpen = args.getFilesToOpen().get(0);
        Loader loader = new Loader(null);
        LogisimFile file;
        try {
            file = loader.openLogisimFile(fileToOpen, args.getSubstitutions());
        } catch (LoadFailedException e) {
            //OK
            System.err.println(getFromLocale("ttyLoadError", fileToOpen.getName()));
            System.exit(-1);
            return;
        }

        int format = args.getTtyFormat();
        if ((format & FORMAT_STATISTICS) != 0) {
            format &= ~FORMAT_STATISTICS;
            displayStatistics(file);
        }
        // no simulation remaining to perform, so just exit
        if (format == 0) {
            System.exit(0);
        }

        Project proj = new Project(file);
        Circuit circuit = file.getMainCircuit();
        Map<Instance, String> pinNames = Analyze.getPinLabels(circuit);
        ArrayList<Instance> outputPins = new ArrayList<>();
        Instance haltPin = null;
        for (Map.Entry<Instance, String> entry : pinNames.entrySet()) {
            Instance pin = entry.getKey();
            String pinName = entry.getValue();
            if (!Pin.isInputPin(pin)) {
                outputPins.add(pin);
                if (pinName.equals("halt")) {
                    haltPin = pin;
                }
            }
        }

        CircuitState circState = new CircuitState(proj, circuit);
        // we have to do our initial propagation before the simulation starts -
        // it's necessary to populate the circuit with substates.
        circState.getPropagator().propagate();
        if (args.getLoadFile() != null) {
            try {
                boolean loaded = loadRam(circState, args.getLoadFile());
                if (!loaded) {
                    //OK
                    System.err.println(getFromLocale("loadNoRamError"));
                    System.exit(-1);
                }
            } catch (IOException e) {
                //OK
                System.err.println(getFromLocale("loadIoError") + ": " + e.toString());
                System.exit(-1);
            }
        }
        int ttyFormat = args.getTtyFormat();
        int simCode = runSimulation(circState, outputPins, haltPin, ttyFormat);
        System.exit(simCode);
    }

    private static void displayStatistics(LogisimFile file) {
        FileStatistics stats = FileStatistics.compute(file, file.getMainCircuit());
        FileStatistics.Count total = stats.getTotalWithSubcircuits();
        int maxName = 0;
        for (FileStatistics.Count count : stats.getCounts()) {
            int nameLength = count.getFactory().getDisplayName().length();
            if (nameLength > maxName) {
                maxName = nameLength;
            }

        }
        String fmt = "%" + countDigits(total.getUniqueCount()) + "d\t"
            + '%' + countDigits(total.getRecursiveCount()) + "d\t";
        String fmtNormal = fmt + "%-" + maxName + "s\t%s\n";
        for (FileStatistics.Count count : stats.getCounts()) {
            Library lib = count.getLibrary();
            String libName = lib == null ? "-" : lib.getDisplayName();
            //OK
            System.out.printf(fmtNormal,
                    count.getUniqueCount(),
                    count.getRecursiveCount(),
                    count.getFactory().getDisplayName(), libName);
        }
        FileStatistics.Count totalWithout = stats.getTotalWithoutSubcircuits();
        //OK
        System.out.printf(fmt + "%s\n",
                totalWithout.getUniqueCount(),
                totalWithout.getRecursiveCount(),
                getFromLocale("statsTotalWithout"));
        //OK
        System.out.printf(fmt + "%s\n",
                total.getUniqueCount(),
                total.getRecursiveCount(),
                getFromLocale("statsTotalWith"));
    }

    private static int countDigits(int num) {
        int digits = 1;
        int lessThan = 10;
        while (num >= lessThan) {
            digits++;
            lessThan *= 10;
        }
        return digits;
    }

    private static boolean loadRam(CircuitState circState, File loadFile)
            throws IOException {
        if (loadFile == null) {
            return false;
        }


        boolean found = false;
        for (Component comp : circState.getCircuit().getNonWires()) {
            if (comp.getFactory() instanceof Ram) {
                Ram ramFactory = (Ram) comp.getFactory();
                InstanceState ramState = circState.getInstanceState(comp);
                ramFactory.loadImage(ramState, loadFile);
                found = true;
            }
        }

        for (CircuitState sub : circState.getSubstates()) {
            found |= loadRam(sub, loadFile);
        }
        return found;
    }

    private static boolean prepareForTty(CircuitState circState,
            ArrayList<InstanceState> keybStates) {
        boolean found = false;
        for (Component comp : circState.getCircuit().getNonWires()) {
            Object factory = comp.getFactory();
            if (factory instanceof Tty) {
                Tty ttyFactory = (Tty) factory;
                InstanceState ttyState = circState.getInstanceState(comp);
                Tty.sendToStdout(ttyState);
                found = true;
            } else if (factory instanceof Keyboard) {
                keybStates.add(circState.getInstanceState(comp));
                found = true;
            }
        }

        for (CircuitState sub : circState.getSubstates()) {
            found |= prepareForTty(sub, keybStates);
        }
        return found;
    }

    private static int runSimulation(CircuitState circState,
                                     Iterable<Instance> outputPins, Instance haltPin, int format) {
        boolean showTable = (format & FORMAT_TABLE) != 0;
        boolean showSpeed = (format & FORMAT_SPEED) != 0;
        boolean showTty = (format & FORMAT_TTY) != 0;
        boolean showHalt = (format & FORMAT_HALT) != 0;

        ArrayList<InstanceState> keyboardStates = null;
        StdinThread stdinThread = null;
        if (showTty) {
            keyboardStates = new ArrayList<>();
            boolean ttyFound = prepareForTty(circState, keyboardStates);
            if (!ttyFound) {
                //OK
                System.err.println(getFromLocale("ttyNoTtyError"));
                System.exit(-1);
            }
            if (keyboardStates.isEmpty()) {
                keyboardStates = null;
            } else {
                stdinThread = new StdinThread();
                stdinThread.start();
            }
        }

        int retCode;
        long tickCount = 0L;
        long start = System.currentTimeMillis();
        boolean halted = false;
        ArrayList<Value> prevOutputs = null;
        Propagator prop = circState.getPropagator();
        while (true) {
            ArrayList<Value> curOutputs = new ArrayList<>();
            for (Instance pin : outputPins) {
                InstanceState pinState = circState.getInstanceState(pin);
                Value val = Pin.getValue(pinState);
                if (pin == haltPin) {
                    halted |= val.equals(Value.TRUE);
                } else if (showTable) {
                    curOutputs.add(val);
                }
            }
            if (showTable) {
                displayTableRow(prevOutputs, curOutputs);
            }

            if (halted) {
                // normal exit
                retCode = 0;
                break;
            }
            if (prop.isOscillating()) {
                // abnormal exit
                retCode = 1;
                break;
            }
            if (keyboardStates != null) {
                char[] buffer = stdinThread.getBuffer();
                if (buffer != null) {
                    for (InstanceState keyState : keyboardStates) {
                        Keyboard.addToBuffer(keyState, buffer);
                    }
                }
            }
            prevOutputs = curOutputs;
            tickCount++;
            prop.tick();
            prop.propagate();
        }
        long elapse = System.currentTimeMillis() - start;
        if (showTty) {
            ensureLineTerminated();
        }

        if (showHalt || retCode != 0) {
            if (retCode == 0) {
                //OK
                System.out.println(getFromLocale("ttyHaltReasonPin"));
            } else if (retCode == 1) {
                //OK
                System.out.println(getFromLocale("ttyHaltReasonOscillation"));
            }
        }
        if (showSpeed) {
            displaySpeed(tickCount, elapse);
        }
        return retCode;
    }

    private static void displayTableRow(List<Value> prevOutputs,
                                        List<Value> curOutputs) {
        boolean shouldPrint = false;
        if (prevOutputs == null) {
            shouldPrint = true;
        } else {
            for (int i = 0; i < curOutputs.size(); i++) {
                Value a = prevOutputs.get(i);
                Value b = curOutputs.get(i);
                if (!a.equals(b)) {
                    shouldPrint = true;
                    break;
                }
            }
        }
        if (shouldPrint) {
            for (int i = 0; i < curOutputs.size(); i++) {
                //OK
                if (i != 0) {
                    System.out.print("\t");
                }

                //OK
                System.out.print(curOutputs.get(i));
            }
            //OK
            System.out.println();
        }
    }

    private static void displaySpeed(long tickCount, long elapse) {
        double hertz = (double) tickCount / (double) elapse * 1000.0;
        double precision;
        if (hertz >= 100.0) {
            precision = 1.0;
        }

        else if (hertz >= 10.0) {
            precision = 0.1;
        }

        else if (hertz >= 1.0) {
            precision = 0.01;
        }

        else if (hertz >= 0.01) {
            precision = 0.0001;
        }

        else {
            precision = 0.0000001;
        }

        hertz = (double) (int) (hertz / precision) * precision;
        String hertzStr = hertz == (double) (int) hertz ? String.valueOf((int) hertz) : String.valueOf(hertz);
        System.out.println(getFromLocale("ttySpeedMsg", hertzStr, String.valueOf(tickCount), String.valueOf(elapse)));
    }

    // It's possible to avoid using the separate thread using System.in.available(),
    // but this doesn't quite work because on some systems, the keyboard input
    // is not interactively echoed until System.in.read() is invoked.
    private static class StdinThread extends Thread {
        // of char[]
        private final LinkedList<char[]> queue;

        private StdinThread() {
            queue = new LinkedList<>();
        }

        public char[] getBuffer() {
            synchronized (queue) {
                if (queue.isEmpty()) {
                    return null;
                } else {
                    return queue.removeFirst();
                }
            }
        }

        @Override
        public void run() {
            InputStreamReader stdin = new InputStreamReader(System.in);
            char[] buffer = new char[32];
            while (true) {
                try {
                    int nbytes = stdin.read(buffer);
                    if (nbytes > 0) {
                        char[] add = new char[nbytes];
                        System.arraycopy(buffer, 0, add, 0, nbytes);
                        synchronized (queue) {
                            queue.addLast(add);
                        }
                    }
                } catch (IOException e) { }
            }
        }
    }
}
