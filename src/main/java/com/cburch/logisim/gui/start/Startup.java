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

public class Startup {
	private static Startup startupTemp = null;

	static void doOpen(File file) {
		if (startupTemp != null) startupTemp.doOpenFile(file);
	}
	static void doPrint(File file) {
		if (startupTemp != null) startupTemp.doPrintFile(file);
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
			if (needed1 == null) return;
			Class<?> needed2 = Class.forName("com.apple.eawt.ApplicationAdapter");
			if (needed2 == null) return;
			MacOsAdapter.register();
			MacOsAdapter.addListeners(true);
		} catch (ClassNotFoundException e) {
			return;
		} catch (Throwable t) {
			try {
				MacOsAdapter.addListeners(false);
			} catch (Throwable t2) { }
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

	public void run() {
		if (isTty) {
			try {
				TtyInterface.run(this);
				return;
			} catch (Throwable t) {
				t.printStackTrace();
				System.exit(-1);
				return;
			}
		}
		
		// kick off the progress monitor
		// (The values used for progress values are based on a single run where
		// I loaded a large file.)
		if (showSplash) {
			try {
				monitor = new SplashScreen();
				monitor.setVisible(true);
			} catch (Throwable t) {
				monitor = null;
				showSplash = false;
			}
		}
		
		// pre-load the two basic component libraries, just so that the time
		// taken is shown separately in the progress bar.
		if (showSplash) monitor.setProgress(SplashScreen.LIBRARIES);
		Loader templLoader = new Loader(monitor);
		int count = templLoader.getBuiltin().getLibrary("Base").getTools().size()
			 + templLoader.getBuiltin().getLibrary("Gates").getTools().size();
		if (count < 0) {
			// this will never happen, but the optimizer doesn't know that...
			System.err.println("FATAL ERROR - no components"); //OK
			System.exit(-1);
		}

		// load in template
		loadTemplate(templLoader, templFile, templEmpty);
		
		// now that the splash screen is almost gone, we do some last-minute
		// interface initialization
		if (showSplash) monitor.setProgress(SplashScreen.GUI_INIT);
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
			if (showSplash) monitor.close();
		} else {
			boolean first = true;
			for (File fileToOpen : filesToOpen) {
				try {
					ProjectActions.doOpen(monitor, fileToOpen, substitutions);
				} catch (LoadFailedException ex) {
					System.err.println(fileToOpen.getName() + ": " + ex.getMessage()); //OK
					System.exit(-1);
				}
				if (first) {
					first = false;
					if (showSplash) monitor.close();
					monitor = null;
				}
			}
		}

		for (File fileToPrint : filesToPrint) {
			doPrintFile(fileToPrint);
		}
	}

	private static void setLocale(String lang) {
		Locale[] opts = getLocaleOptions();
		for (int i = 0; i < opts.length; i++) {
			if (lang.equals(opts[i].toString())) {
				LocaleManager.setLocale(opts[i]);
				return;
			}
		}
		System.err.println(_("invalidLocaleError")); //OK
		System.err.println(_("invalidLocaleOptionsHeader")); //OK
		for (int i = 0; i < opts.length; i++) {
			System.err.println("   " + opts[i].toString()); //OK
		}
		System.exit(-1);
	}

	private void loadTemplate(Loader loader, File templFile,
			boolean templEmpty) {
		if (showSplash) monitor.setProgress(SplashScreen.TEMPLATE_OPEN);
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
	 * @param args
	 * @return A Startup object
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
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) { }

		// parse arguments
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-tty")) {
				if (i + 1 < args.length) {
					i++;
					String[] fmts = args[i].split(",");
					if (fmts.length == 0) {
						System.err.println(_("ttyFormatError")); //OK
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
							System.err.println(_("ttyFormatError")); //OK
						}
					}
				} else {
					System.err.println(_("ttyFormatError")); //OK
					return null;
				}
			} else if (arg.equals("-sub")) {
				if (i + 2 < args.length) {
					File a = new File(args[i + 1]);
					File b = new File(args[i + 2]);
					if (ret.substitutions.containsKey(a)) {
						System.err.println(_("argDuplicateSubstitutionError")); //OK
						return null;
					} else {
						ret.substitutions.put(a, b);
						i += 2;
					}
				} else {
					System.err.println(_("argTwoSubstitutionError")); //OK
					return null;
				}
			} else if (arg.equals("-load")) {
				if (i + 1 < args.length) {
					i++;
					if (ret.loadFile != null) {
						System.err.println(_("loadMultipleError")); //OK
					}
					File f = new File(args[i]);
					ret.loadFile = f;
				} else {
					System.err.println(_("loadNeedsFileError")); //OK
					return null;
				}
			} else if (arg.equals("-empty")) {
				if (ret.templFile != null || ret.templEmpty || ret.templPlain) {
					System.err.println(_("argOneTemplateError")); //OK
					return null;
				}
				ret.templEmpty = true;
			} else if (arg.equals("-plain")) {
				if (ret.templFile != null || ret.templEmpty || ret.templPlain) {
					System.err.println(_("argOneTemplateError")); //OK
					return null;
				}
				ret.templPlain = true;
			} else if (arg.equals("-version")) {
				System.out.println(Main.VERSION_NAME); //OK
				return null;
			} else if (arg.equals("-gates")) {
				i++;
				if (i >= args.length) printUsage();
				String a = args[i];
				if (a.equals("shaped")) {
					AppPreferences.GATE_SHAPE.set(AppPreferences.SHAPE_SHAPED);
				} else if (a.equals("rectangular")) {
					AppPreferences.GATE_SHAPE.set(AppPreferences.SHAPE_RECTANGULAR);
				} else {
					System.err.println(_("argGatesOptionError")); //OK
					System.exit(-1);
				}
			} else if (arg.equals("-locale")) {
				i++;
				if (i >= args.length) printUsage();
				setLocale(args[i]);
			} else if (arg.equals("-accents")) {
				i++;
				if (i >= args.length) printUsage();
				String a = args[i];
				if (a.equals("yes")) {
					AppPreferences.ACCENTS_REPLACE.setBoolean(false);
				} else if (a.equals("no")) {
					AppPreferences.ACCENTS_REPLACE.setBoolean(true);
				} else {
					System.err.println(_("argAccentsOptionError")); //OK
					System.exit(-1);
				}
			} else if (arg.equals("-template")) {
				if (ret.templFile != null || ret.templEmpty || ret.templPlain) {
					System.err.println(_("argOneTemplateError")); //OK
					return null;
				}
				i++;
				if (i >= args.length) printUsage();
				ret.templFile = new File(args[i]);
				if (!ret.templFile.exists()) {
					System.err.println(String.format( //OK
							_("templateMissingError"), args[i]));
				} else if (!ret.templFile.canRead()) {
					System.err.println(String.format( //OK
							_("templateCannotReadError"), args[i]));
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
			System.err.println(_("ttyNeedsFileError")); //OK
			return null;
		}
		if (ret.loadFile != null && !ret.isTty) {
			System.err.println(_("loadNeedsTtyError")); //OK
			return null;
		}
		return ret;
	}

	private static void printUsage() {
		System.err.println(String.format(_("argUsage"), Startup.class.getName())); //OK
		System.err.println(); //OK
		System.err.println(_("argOptionHeader")); //OK
		System.err.println("   " + _("argAccentsOption")); //OK
		System.err.println("   " + _("argClearOption")); //OK
		System.err.println("   " + _("argEmptyOption")); //OK
		System.err.println("   " + _("argGatesOption")); //OK
		System.err.println("   " + _("argHelpOption")); //OK
		System.err.println("   " + _("argLoadOption")); //OK
		System.err.println("   " + _("argLocaleOption")); //OK
		System.err.println("   " + _("argNoSplashOption")); //OK
		System.err.println("   " + _("argPlainOption")); //OK
		System.err.println("   " + _("argSubOption")); //OK
		System.err.println("   " + _("argTemplateOption")); //OK
		System.err.println("   " + _("argTtyOption")); //OK
		System.err.println("   " + _("argVersionOption")); //OK
		System.exit(-1);
	}
}
