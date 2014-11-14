/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.menu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.file.LogisimFileActions;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.Library;
import static com.cburch.logisim.util.LocaleString.*;

@SuppressWarnings("serial")
public class ProjectLibraryActions {
    private ProjectLibraryActions() { }

    private static class LibraryJList extends JList<Library> {
        LibraryJList(List<Library> libraries) {
            setListData(libraries.toArray(new Library[]{}));
        }

        Library[] getSelectedLibraries() {
            return this.getSelectedValuesList().toArray(new Library[]{});
        }
    }

    public static void doLoadBuiltinLibrary(Project proj) {
        LogisimFile file = proj.getLogisimFile();
        List<Library> baseBuilt = file.getLoader().getBuiltin().getLibraries();
        ArrayList<Library> builtins = new ArrayList<Library>(baseBuilt);
        builtins.removeAll(file.getLibraries());
        if (builtins.isEmpty()) {
            JOptionPane.showMessageDialog(proj.getFrame(),
                    getFromLocale("loadBuiltinNoneError"),
                    getFromLocale("loadBuiltinErrorTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LibraryJList list = new LibraryJList(builtins);
        JScrollPane listPane = new JScrollPane(list);
        int action = JOptionPane.showConfirmDialog(proj.getFrame(), listPane,
                getFromLocale("loadBuiltinDialogTitle"), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (action == JOptionPane.OK_OPTION) {
            Library[] libs = list.getSelectedLibraries();
            if (libs != null) {
                proj.doAction(LogisimFileActions.loadLibraries(libs));
            }

        }
    }

    public static void doLoadLogisimLibrary(Project proj) {
        Loader loader = proj.getLogisimFile().getLoader();
        JFileChooser chooser = loader.createChooser();
        chooser.setDialogTitle(getFromLocale("loadLogisimDialogTitle"));
        chooser.setFileFilter(Loader.LOGISIM_FILTER);
        int check = chooser.showOpenDialog(proj.getFrame());
        if (check == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            Library lib = loader.loadLogisimLibrary(f);
            if (lib != null) {
                proj.doAction(LogisimFileActions.loadLibrary(lib));
            }
        }
    }

    public static void doLoadJarLibrary(Project proj) {
        Loader loader = proj.getLogisimFile().getLoader();
        JFileChooser chooser = loader.createChooser();
        chooser.setDialogTitle(getFromLocale("loadJarDialogTitle"));
        chooser.setFileFilter(Loader.JAR_FILTER);
        int check = chooser.showOpenDialog(proj.getFrame());
        if (check == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String className = null;

            // try to retrieve the class name from the "Library-Class"
            // attribute in the manifest. This section of code was contributed
            // by Christophe Jacquet (Request Tracker #2024431).
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(f);
                Manifest manifest = jarFile.getManifest();
                className = manifest.getMainAttributes().getValue("Library-Class");
            } catch (IOException e) {
                // if opening the JAR file failed, do nothing
            } finally {
                if (jarFile != null) {
                    try { jarFile.close(); } catch (IOException e) { }
                }
            }

            // if the class name was not found, go back to the good old dialog
            if (className == null) {
                className = JOptionPane.showInputDialog(proj.getFrame(),
                    getFromLocale("jarClassNamePrompt"),
                    getFromLocale("jarClassNameTitle"),
                    JOptionPane.QUESTION_MESSAGE);
                // if user canceled selection, abort
                if (className == null) {
                    return;
                }

            }

            Library lib = loader.loadJarLibrary(f, className);
            if (lib != null) {
                proj.doAction(LogisimFileActions.loadLibrary(lib));
            }
        }
    }

    public static void doUnloadLibraries(Project proj) {
        LogisimFile file = proj.getLogisimFile();
        ArrayList<Library> canUnload = new ArrayList<Library>();
        for (Library lib : file.getLibraries()) {
            String message = file.getUnloadLibraryMessage(lib);
            if (message == null) {
                canUnload.add(lib);
            }

        }
        if (canUnload.isEmpty()) {
            JOptionPane.showMessageDialog(proj.getFrame(),
                    getFromLocale("unloadNoneError"),
                    getFromLocale("unloadErrorTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LibraryJList list = new LibraryJList(canUnload);
        JScrollPane listPane = new JScrollPane(list);
        int action = JOptionPane.showConfirmDialog(proj.getFrame(), listPane,
                getFromLocale("unloadLibrariesDialogTitle"), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (action == JOptionPane.OK_OPTION) {
            Library[] libs = list.getSelectedLibraries();
            if (libs != null) {
                proj.doAction(LogisimFileActions.unloadLibraries(libs));
            }

        }
    }

    public static void doUnloadLibrary(Project proj, Library lib) {
        String message = proj.getLogisimFile().getUnloadLibraryMessage(lib);
        if (message != null) {
            JOptionPane.showMessageDialog(proj.getFrame(), message,
                getFromLocale("unloadErrorTitle"), JOptionPane.ERROR_MESSAGE);
        } else {
            proj.doAction(LogisimFileActions.unloadLibrary(lib));
        }
    }
}
