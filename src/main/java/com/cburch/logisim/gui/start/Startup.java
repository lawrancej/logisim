/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.start;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.io.File;

import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cburch.logisim.Main;
import com.cburch.logisim.file.LoadFailedException;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.gui.main.Print;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.gui.menu.WindowManagers;
import com.cburch.logisim.gui.start.SplashScreen;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectActions;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.MacCompatibility;

import static com.cburch.logisim.util.LocaleString.*;

/**
 * A class to encapsulate the startup process
 */
public class Startup {
    private static Startup startupTemp = null;

    private static final Logger logger = LoggerFactory.getLogger( Startup.class );

    static void doOpen(File file) {
        if (startupTemp != null) {
            startupTemp.doOpenFile(file);
        }

    }
    static void doPrint(File file) {
        if (startupTemp != null) {
            startupTemp.doPrintFile(file);
        }

    }

    private void doOpenFile(File file) {
        if (initialized) {
            ProjectActions.doOpen(null, null, file);
        } else {
            filesToOpen.add(file);
        }
    }

    private void doPrintFile(File file) {
        if (initialized) {
            Project toPrint = ProjectActions.doOpen(null, null, file);
            Print.doPrint(toPrint);
            toPrint.getFrame().dispose();
        } else {
            filesToPrint.add(file);
        }
    }

    private static void registerHandler() {
        try {
            Class<?> needed1 = Class.forName("com.apple.eawt.Application");
            if (needed1 == null) {
                return;
            }

            Class<?> needed2 = Class.forName("com.apple.eawt.ApplicationAdapter");
            if (needed2 == null) {
                return;
            }

            MacOsAdapter.register();
            MacOsAdapter.addListeners(true);
        } catch (ClassNotFoundException e) {
            logger.warn( "Failed to register handler: " + e.getLocalizedMessage() );
            return;
        } catch (Exception t) {
            try {
                MacOsAdapter.addListeners(false);
            } catch (Exception t2) { 
                logger.warn( "Failed to register MacOS adapters" );
            }
        }
    }

    // based on command line
    boolean isTty;
    private File templFile = null;
    private boolean templEmpty = false;
    private boolean templPlain = false;
    private ArrayList<File> filesToOpen = new ArrayList<File>();
    private boolean showSplash;
    private File loadFile;
    private HashMap<File,File> substitutions = new HashMap<File,File>();
    private int ttyFormat = 0;

    // from other sources
    private boolean initialized = false;
    private SplashScreen monitor = null;
    private ArrayList<File> filesToPrint = new ArrayList<File>();

    private Startup(boolean isTty) {
        this.isTty = isTty;
        this.showSplash = !isTty;
    }

    List<File> getFilesToOpen() {
        return filesToOpen;
    }

    File getLoadFile() {
        return loadFile;
    }

    int getTtyFormat() {
        return ttyFormat;
    }

    Map<File,File> getSubstitutions() {
        return Collections.unmodifiableMap(substitutions);
    }

    /**
     * Starts splash screen and launches Logisim
     */
    public void run() {
        if (isTty) {
            try {
                TtyInterface.run(this);
                return;
            } catch (Exception e) {
                logger.error( "Logisim failed to start.\nException: "
                    + e.getLocalizedMessage() );
                e.printStackTrace();
                System.exit(-1);
            }
        }

        // kick off the progress monitor
        // (The values used for progress values are based on a single run where
        // I loaded a large file.)
        if (showSplash) {
            try {
                monitor = new SplashScreen();
                monitor.setVisible(true);
            } catch (Exception e) {
                monitor = null;
                showSplash = false;
                logger.warn( "Not showing the splash screen, for some reason" );
            }
        }

        // pre-load the two basic component libraries, just so that the time
        // taken is shown separately in the progress bar.
        if (showSplash) {
            monitor.setProgress(SplashScreen.LIBRARIES);
        }

        Loader templLoader = new Loader(monitor);
        int count = templLoader.getBuiltin().getLibrary("Base").getTools().size()
             + templLoader.getBuiltin().getLibrary("Gates").getTools().size();
        if (count < 0) {
            // this will never happen, but the optimizer doesn't know that...
            //OK
            logger.error( "FATAL - no components were found");
            System.exit(-1);
        }

        // load in template
        loadTemplate(templLoader, templFile, templEmpty);

        // now that the splash screen is almost gone, we do some last-minute
        // interface initialization
        if (showSplash) {
            monitor.setProgress(SplashScreen.GUI_INIT);
        }

        WindowManagers.initialize();
        if (MacCompatibility.isSwingUsingScreenMenuBar()) {
            MacCompatibility.setFramelessJMenuBar(new LogisimMenuBar(null, null));
        } else {
            new LogisimMenuBar(null, null);
            // most of the time occupied here will be in loading menus, which
            // will occur eventually anyway; we might as well do it when the
            // monitor says we are
        }

        // if user has double-clicked a file to open, we'll
        // use that as the file to open now.
        initialized = true;

        // load file
        if (filesToOpen.isEmpty()) {
            ProjectActions.doNew(monitor, true);
            if (showSplash) {
                monitor.close();
            }

        } else {
            boolean first = true;
            for (File fileToOpen : filesToOpen) {
                try {
                    ProjectActions.doOpen(monitor, fileToOpen, substitutions);
                } catch (LoadFailedException ex) {
                    //OK
                    logger.error( "Could not open " 
                        + fileToOpen.getName() + ": " + ex.getMessage() );
                    System.exit(-1);
                }
                if (first) {
                    first = false;
                    if (showSplash) {
                        monitor.close();
                    }

                    monitor = null;
                }
            }
        }

        for (File fileToPrint : filesToPrint) {
            doPrintFile(fileToPrint);
        }
    }

    private static void setLocale(String lang) {
        Locale[] opts = getFromLocaleOptions();
        for (int i = 0; i < opts.length; i++) {
            if (lang.equals(opts[i].toString())) {
                LocaleManager.setLocale(opts[i]);
                return;
            }
        }
        //OK
        logger.warn(getFromLocale("invalidLocaleError"));
        //OK
        logger.warn(getFromLocale("invalidLocaleOptionsHeader"));
        for (int i = 0; i < opts.length; i++) {
            //OK
            logger.warn("   " + opts[i].toString());
        }
        System.exit(-1);
    }

    private void loadTemplate(Loader loader, File templFile,
            boolean templEmpty) {
        if (showSplash) {
            monitor.setProgress(SplashScreen.TEMPLATE_OPEN);
        }

        if (templFile != null) {
            AppPreferences.setTemplateFile(templFile);
            AppPreferences.setTemplateType(AppPreferences.TEMPLATE_CUSTOM);
        } else if (templEmpty) {
            AppPreferences.setTemplateType(AppPreferences.TEMPLATE_EMPTY);
        } else if (templPlain) {
            AppPreferences.setTemplateType(AppPreferences.TEMPLATE_PLAIN);
        }
    }
    /**
     * Parses the command-line arguments to com.cburch.logisim.Main
     * @param args command line arguments
     * @return A Startup object or null if it fails
     */
    public static Startup parseArgs(String[] args) {
        // see whether we'll be using any graphics
        boolean isTty = false;
        boolean isClearPreferences = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-tty")) {
                isTty = true;
            } else if (args[i].equals("-clearprefs") || args[i].equals("-clearprops")) {
                isClearPreferences = true;
            }
        }

        if (!isTty) {
            // we're using the GUI: Set up the Look&Feel to match the platform
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Logisim");
            System.setProperty("apple.laf.useScreenMenuBar", "true");

            LocaleManager.setReplaceAccents(false);

            // Initialize graphics acceleration if appropriate
            AppPreferences.handleGraphicsAcceleration();
        }

        Startup ret = new Startup(isTty);
        startupTemp = ret;
        if (!isTty) {
            registerHandler();
        }

        if (isClearPreferences) {
            AppPreferences.clear();
        }

        try {
            UIManager.setLookAndFeel(AppPreferences.LOOK_AND_FEEL.get());
        } catch (Exception ex) { }

        // parse arguments
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-tty")) {
                if (i + 1 < args.length) {
                    i++;
                    String[] fmts = args[i].split(",");
                    if (fmts.length == 0) {
                        //OK
                        logger.warn(getFromLocale("ttyFormatError"));
                    }
                    for (int j = 0; j < fmts.length; j++) {
                        String fmt = fmts[j].trim();
                        if (fmt.equals("table")) {
                            ret.ttyFormat |= TtyInterface.FORMAT_TABLE;
                        } else if (fmt.equals("speed")) {
                            ret.ttyFormat |= TtyInterface.FORMAT_SPEED;
                        } else if (fmt.equals("tty")) {
                            ret.ttyFormat |= TtyInterface.FORMAT_TTY;
                        } else if (fmt.equals("halt")) {
                            ret.ttyFormat |= TtyInterface.FORMAT_HALT;
                        } else if (fmt.equals("stats")) {
                            ret.ttyFormat |= TtyInterface.FORMAT_STATISTICS;
                        } else {
                            //OK
                            System.err.println(getFromLocale("ttyFormatError"));
                        }
                    }
                } else {
                    //OK
                    System.err.println(getFromLocale("ttyFormatError"));
                    return null;
                }
            } else if (arg.equals("-sub")) {
                if (i + 2 < args.length) {
                    File a = new File(args[i + 1]);
                    File b = new File(args[i + 2]);
                    if (ret.substitutions.containsKey(a)) {
                        //OK
                        logger.error(getFromLocale("argDuplicateSubstitutionError"));
                        return null;
                    } else {
                        ret.substitutions.put(a, b);
                        i += 2;
                    }
                } else {
                    //OK
                    logger.error(getFromLocale("argTwoSubstitutionError"));
                    return null;
                }
            } else if (arg.equals("-load")) {
                if (i + 1 < args.length) {
                    i++;
                    if (ret.loadFile != null) {
                        //OK
                        logger.warn(getFromLocale("loadMultipleError"));
                    }
                    File f = new File(args[i]);
                    ret.loadFile = f;
                } else {
                    //OK
                    logger.error(getFromLocale("loadNeedsFileError"));
                    return null;
                }
            } else if (arg.equals("-empty")) {
                if (ret.templFile != null || ret.templEmpty || ret.templPlain) {
                    //OK
                    logger.error(getFromLocale("argOneTemplateError"));
                    return null;
                }
                ret.templEmpty = true;
            } else if (arg.equals("-plain")) {
                if (ret.templFile != null || ret.templEmpty || ret.templPlain) {
                    //OK
                    logger.error(getFromLocale("argOneTemplateError"));
                    return null;
                }
                ret.templPlain = true;
            } else if (arg.equals("-version")) {
                //OK
                System.out.println(Main.VERSION_NAME);
                return null;
            } else if (arg.equals("-gates")) {
                i++;
                if (i >= args.length) {
                    printUsage();
                }

                String a = args[i];
                if (a.equals("shaped")) {
                    AppPreferences.GATE_SHAPE.set(AppPreferences.SHAPE_SHAPED);
                } else if (a.equals("rectangular")) {
                    AppPreferences.GATE_SHAPE.set(AppPreferences.SHAPE_RECTANGULAR);
                } else {
                    //OK
                    logger.error(getFromLocale("argGatesOptionError"));
                    System.exit(-1);
                }
            } else if (arg.equals("-locale")) {
                i++;
                if (i >= args.length) {
                    printUsage();
                }

                setLocale(args[i]);
            } else if (arg.equals("-accents")) {
                i++;
                if (i >= args.length) {
                    printUsage();
                }

                String a = args[i];
                if (a.equals("yes")) {
                    AppPreferences.ACCENTS_REPLACE.setBoolean(false);
                } else if (a.equals("no")) {
                    AppPreferences.ACCENTS_REPLACE.setBoolean(true);
                } else {
                    //OK
                    logger.error(getFromLocale("argAccentsOptionError"));
                    System.exit(-1);
                }
            } else if (arg.equals("-template")) {
                if (ret.templFile != null || ret.templEmpty || ret.templPlain) {
                    //OK
                    logger.error(getFromLocale("argOneTemplateError"));
                    return null;
                }
                i++;
                if (i >= args.length) {
                    printUsage();
                }

                ret.templFile = new File(args[i]);
                if (!ret.templFile.exists()) {
                    //OK
                    logger.warn(String.format(
                            getFromLocale("templateMissingError"), args[i]));
                } else if (!ret.templFile.canRead()) {
                    //OK
                    logger.warn(String.format(
                            getFromLocale("templateCannotReadError"), args[i]));
                }
            } else if (arg.equals("-nosplash")) {
                ret.showSplash = false;
            } else if (arg.equals("-clearprefs")) {
                // already handled above
            } else if (arg.charAt(0) == '-') {
                printUsage();
                return null;
            } else {
                ret.filesToOpen.add(new File(arg));
            }
        }
        if (ret.isTty && ret.filesToOpen.isEmpty()) {
            //OK
            logger.error(getFromLocale("ttyNeedsFileError"));
            return null;
        }
        if (ret.loadFile != null && !ret.isTty) {
            //OK
            logger.error(getFromLocale("loadNeedsTtyError"));
            return null;
        }
        return ret;
    }

    /**
     * Prints command line help functionality
     */
    private static void printUsage() {
        System.err.println(String.format(getFromLocale("argUsage"), Startup.class.getName()));
        System.err.println();
        System.err.println(getFromLocale("argOptionHeader"));
        System.err.println("   " + getFromLocale("argAccentsOption"));
        System.err.println("   " + getFromLocale("argClearOption"));
        System.err.println("   " + getFromLocale("argEmptyOption"));
        System.err.println("   " + getFromLocale("argGatesOption"));
        System.err.println("   " + getFromLocale("argHelpOption"));
        System.err.println("   " + getFromLocale("argLoadOption"));
        System.err.println("   " + getFromLocale("argLocaleOption"));
        System.err.println("   " + getFromLocale("argNoSplashOption"));
        System.err.println("   " + getFromLocale("argPlainOption"));
        System.err.println("   " + getFromLocale("argSubOption"));
        System.err.println("   " + getFromLocale("argTemplateOption"));
        System.err.println("   " + getFromLocale("argTtyOption"));
        System.err.println("   " + getFromLocale("argVersionOption"));
        System.exit(-1);
    }
}
