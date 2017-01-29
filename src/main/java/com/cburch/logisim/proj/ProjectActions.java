/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.proj;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.file.LoadFailedException;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.gui.start.SplashScreen;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.JFileChoosers;
import static com.cburch.logisim.util.LocaleString.*;

public class ProjectActions {
    private ProjectActions() { }

    private static class CreateFrame implements Runnable {
        private Loader loader;
        private Project proj;
        private boolean isStartupScreen;

        public CreateFrame(Loader loader, Project proj, boolean isStartup) {
            this.loader = loader;
            this.proj = proj;
            this.isStartupScreen = isStartup;
        }

        @Override
        public void run() {
            Frame frame = createFrame(null, proj);
            frame.setVisible(true);
            frame.toFront();
            frame.getCanvas().requestFocus();
            loader.setParent(frame);
            if (isStartupScreen) {
                proj.setStartupScreen(true);
            }

        }
    }

    public static Project doNew(SplashScreen monitor) {
        return doNew(monitor, false);
    }

    public static Project doNew(SplashScreen monitor, boolean isStartupScreen) {
        if (monitor != null) {
            monitor.setProgress(SplashScreen.FILE_CREATE);
        }

        Loader loader = new Loader(monitor);
        InputStream templReader = AppPreferences.getTemplate().createStream();
        LogisimFile file = null;
        try {
            file = loader.openLogisimFile(templReader);
        } catch (IOException ex) {
            displayException(monitor, ex);
        } catch (LoadFailedException ex) {
            displayException(monitor, ex);
        } finally {
            try { templReader.close(); } catch (IOException e) { }
        }
        if (file == null) {
            file = createEmptyFile(loader);
        }

        return completeProject(monitor, loader, file, isStartupScreen);
    }

    private static void displayException(Component parent, Exception ex) {
        String msg = getFromLocale("templateOpenError", ex.toString());
        String ttl = getFromLocale("templateOpenErrorTitle");
        JOptionPane.showMessageDialog(parent, msg, ttl, JOptionPane.ERROR_MESSAGE);
    }

    private static LogisimFile createEmptyFile(Loader loader) {
        InputStream templReader = AppPreferences.getEmptyTemplate().createStream();
        LogisimFile file;
        try {
            file = loader.openLogisimFile(templReader);
        } catch (Exception t) {
            file = LogisimFile.createNew(loader);
            file.addCircuit(new Circuit("main"));
        } finally {
            try { templReader.close(); } catch (IOException e) { }
        }
        return file;
    }

    private static Project completeProject(SplashScreen monitor, Loader loader,
            LogisimFile file, boolean isStartup) {
        if (monitor != null) {
            monitor.setProgress(SplashScreen.PROJECT_CREATE);
        }
        Project ret = new Project(file);

        if (monitor != null) {
            monitor.setProgress(SplashScreen.FRAME_CREATE);
        }
        SwingUtilities.invokeLater(new CreateFrame(loader, ret, isStartup));
        return ret;
    }

    public static LogisimFile createNewFile(Project baseProject) {
        Loader loader = new Loader(baseProject == null ? null : baseProject.getFrame());
        InputStream templReader = AppPreferences.getTemplate().createStream();
        LogisimFile file;
        try {
            file = loader.openLogisimFile(templReader);
        } catch (IOException ex) {
            displayException(baseProject.getFrame(), ex);
            file = createEmptyFile(loader);
        } catch (LoadFailedException ex) {
            if (!ex.isShown()) {
                displayException(baseProject.getFrame(), ex);
            }
            file = createEmptyFile(loader);
        } finally {
            try { templReader.close(); } catch (IOException e) { }
        }
        return file;
    }

    private static Frame createFrame(Project sourceProject, Project newProject) {
        if (sourceProject != null) {
            Frame frame = sourceProject.getFrame();
            if (frame != null) {
                frame.savePreferences();
            }
        }
        Frame newFrame = new Frame(newProject);
        newProject.setFrame(newFrame);
        return newFrame;
    }

    public static Project doNew(Project baseProject) {
        LogisimFile file = createNewFile(baseProject);
        Project newProj = new Project(file);
        Frame frame = createFrame(baseProject, newProj);
        frame.setVisible(true);
        frame.getCanvas().requestFocus();
        newProj.getLogisimFile().getLoader().setParent(frame);
        return newProj;
    }

    public static Project doOpen(SplashScreen monitor, File source,
            Map<File,File> substitutions) throws LoadFailedException {
        if (monitor != null) {
            monitor.setProgress(SplashScreen.FILE_LOAD);
        }
        Loader loader = new Loader(monitor);
        LogisimFile file = loader.openLogisimFile(source, substitutions);
        AppPreferences.updateRecentFile(source);
        return completeProject(monitor, loader, file, false);
    }

    public static void doOpen(Component parent, Project baseProject) {
        JFileChooser chooser;
        if (baseProject != null) {
            Loader oldLoader = baseProject.getLogisimFile().getLoader();
            chooser = oldLoader.createChooser();
            if (oldLoader.getMainFile() != null) {
                chooser.setSelectedFile(oldLoader.getMainFile());
            }
        } else {
            chooser = JFileChoosers.create();
        }
        chooser.setFileFilter(Loader.LOGISIM_FILTER);
        int returnVal = chooser.showOpenDialog(parent);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selected = chooser.getSelectedFile();
        System.out.println("9"+selected.toString());
        if (selected != null) {
            doOpen(parent, baseProject, selected);
        }
    }
    /*
     * Method to open directly a breadboard configuration , but for the moment this method isn't used
     * open breadboard with a template configuration is more efficiently 
     */
    public static void doOpenProtoboard(Component parent, Project baseProject) {
    	JFileChooser chooser =new JFileChooser();
    	File selected = chooser.getCurrentDirectory();
        File selector = new File(selected.toString()+"/protoboard.circ");
        doOpen(parent, baseProject, selector);
    }

    public static Project doOpen(Component parent,
            Project baseProject, File f) {
        Project proj = Projects.findProjectFor(f);
        Loader loader = null;
        if (proj != null) {
            proj.getFrame().toFront();
            loader = proj.getLogisimFile().getLoader();
            if (proj.isFileDirty()) {
                String message = getFromLocale("openAlreadyMessage",
                        proj.getLogisimFile().getName());
                String[] options = {
                        getFromLocale("openAlreadyLoseChangesOption"),
                        getFromLocale("openAlreadyNewWindowOption"),
                        getFromLocale("openAlreadyCancelOption"),
                    };
                int result = JOptionPane.showOptionDialog(proj.getFrame(),
                        message, getFromLocale("openAlreadyTitle"), 0,
                        JOptionPane.QUESTION_MESSAGE, null,
                        options, options[2]);
                if (result == 0) {
                    // keep proj as is, so that load happens into the window
                    ;
                } else if (result == 1) {
                    // we'll create a new project
                    proj = null;
                } else {
                    return proj;
                }
            }
        }

        if (proj == null && baseProject != null && baseProject.isStartupScreen()) {
            proj = baseProject;
            proj.setStartupScreen(false);
            loader = baseProject.getLogisimFile().getLoader();
        } else {
            loader = new Loader(baseProject == null ? parent : baseProject.getFrame());
        }

        try {
            LogisimFile lib = loader.openLogisimFile(f);
            AppPreferences.updateRecentFile(f);
            if (lib == null) {
                return null;
            }

            if (proj == null) {
                proj = new Project(lib);
            } else {
                proj.setLogisimFile(lib);
            }
        } catch (LoadFailedException ex) {
            if (!ex.isShown()) {
                JOptionPane.showMessageDialog(parent,
                    getFromLocale("fileOpenError", ex.toString()),
                    getFromLocale("fileOpenErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }

        Frame frame = proj.getFrame();
        if (frame == null) {
            frame = createFrame(baseProject, proj);
        }
        frame.setVisible(true);
        frame.toFront();
        frame.getCanvas().requestFocus();
        proj.getLogisimFile().getLoader().setParent(frame);
        return proj;
    }


    // returns true if save is completed
    public static boolean doSaveAs(Project proj) {
        Loader loader = proj.getLogisimFile().getLoader();
        JFileChooser chooser = loader.createChooser();
        chooser.setFileFilter(Loader.LOGISIM_FILTER);
        if (loader.getMainFile() != null) {
            chooser.setSelectedFile(loader.getMainFile());
        }
        int returnVal = chooser.showSaveDialog(proj.getFrame());
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return false;
        }


        File f = chooser.getSelectedFile();
        String circExt = Loader.LOGISIM_EXTENSION;
        if (!f.getName().endsWith(circExt)) {
            String old = f.getName();
            int ext0 = old.lastIndexOf('.');
            if (ext0 < 0 || !Pattern.matches("\\.\\p{L}{2,}[0-9]?", old.substring(ext0))) {
                f = new File(f.getParentFile(), old + circExt);
            } else {
                String ext = old.substring(ext0);
                String ttl = getFromLocale("replaceExtensionTitle");
                String msg = getFromLocale("replaceExtensionMessage", ext);
                Object[] options = {
                        getFromLocale("replaceExtensionReplaceOpt", ext),
                        getFromLocale("replaceExtensionAddOpt", circExt),
                        getFromLocale("replaceExtensionKeepOpt")
                    };
                JOptionPane dlog = new JOptionPane(msg);
                dlog.setMessageType(JOptionPane.QUESTION_MESSAGE);
                dlog.setOptions(options);
                dlog.createDialog(proj.getFrame(), ttl).setVisible(true);

                Object result = dlog.getValue();
                if (result == options[0]) {
                    String name = old.substring(0, ext0) + circExt;
                    f = new File(f.getParentFile(), name);
                } else if (result == options[1]) {
                    f = new File(f.getParentFile(), old + circExt);
                }
            }
        }

        if (f.exists()) {
            int confirm = JOptionPane.showConfirmDialog(proj.getFrame(),
                getFromLocale("confirmOverwriteMessage"),
                getFromLocale("confirmOverwriteTitle"),
                JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

        }
        return doSave(proj, f);
    }

    public static boolean doSave(Project proj) {
        Loader loader = proj.getLogisimFile().getLoader();
        File f = loader.getMainFile();
        if (f == null) {
            return doSaveAs(proj);
        }

        else {
            return doSave(proj, f);
        }

    }

    private static boolean doSave(Project proj, File f) {
        Loader loader = proj.getLogisimFile().getLoader();
        Tool oldTool = proj.getTool();
        proj.setTool(null);
        boolean ret = loader.save(proj.getLogisimFile(), f);
        if (ret) {
            AppPreferences.updateRecentFile(f);
            proj.setFileAsClean();
        }
        proj.setTool(oldTool);
        return ret;
    }

    public static void doQuit() {
        Frame top = Projects.getTopFrame();
        top.savePreferences();

        for (Project proj : new ArrayList<Project>(Projects.getOpenProjects())) {
            if (!proj.confirmClose(getFromLocale("confirmQuitTitle"))) {
                return;
            }

        }
        System.exit(0);
    }
    
    /*
     * Method to open directly a breadboard configuration with a template configuration 
     */
    public static void setTemplate(){
    	JFileChooser chooser =new JFileChooser();
    	File selected = chooser.getCurrentDirectory();
        File selector = new File(selected.toString()+"/breadboard.circ");
    	AppPreferences.setTemplateFile(selector);
    	AppPreferences.setTemplateType(AppPreferences.TEMPLATE_CUSTOM);
    	System.out.println(""+AppPreferences.TEMPLATE_CUSTOM);
    }
    
    /*
     * Method to reset to default the template configuration 
     */
    public static void resetTemplate(){
    	AppPreferences.getEmptyTemplate();
    	AppPreferences.setTemplateType(AppPreferences.TEMPLATE_PLAIN);
    }
}
