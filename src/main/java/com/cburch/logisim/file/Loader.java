/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.file;

import java.awt.Component;
import java.awt.Dimension;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import com.cburch.logisim.std.Builtin;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.util.JFileChoosers;
import com.cburch.logisim.util.MacCompatibility;
import com.cburch.logisim.util.ZipClassLoader;

import static com.cburch.logisim.util.LocaleString.*;

import org.slf4j.*;

/**
 * A class to encapsulate loading functionality
 * of files or library objects
 */
public class Loader implements LibraryLoader {
    private static final Logger logger = 
        LoggerFactory.getLogger( Loader.class );

    public static final String LOGISIM_EXTENSION = ".circ";
    public static final FileFilter LOGISIM_FILTER = new LogisimFileFilter();
    public static final FileFilter JAR_FILTER = new JarFileFilter();

    private static class LogisimFileFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            return f.isDirectory()
                || f.getName().endsWith(LOGISIM_EXTENSION);
        }

        @Override
        public String getDescription() {
            return getFromLocale("logisimFileFilter");
        }
    }

    private static class JarFileFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            return f.isDirectory()
                || f.getName().endsWith(".jar");
        }

        @Override
        public String getDescription() {
            return getFromLocale("jarFileFilter");
        }
    }

    // fixed
    private Component parent;
    private Builtin builtin = new Builtin();

    // to be cleared with each new file
    private File mainFile = null;
    private Stack<File> filesOpening = new Stack<File>();
    private Map<File,File> substitutions = new HashMap<File,File>();

    /**
     * Constructor
     * @param parent the parent
     */
    public Loader(Component parent) {
        this.parent = parent;
        clear();
    }

    /**
     * Get built-in functionality.
     * @return the built-in functionality
     */
    public Builtin getBuiltin() {
        return builtin;
    }

    /**
     * Sets the parent of the component from a high level.
     * @param value the component's new parent
     */
    public void setParent(Component value) {
        parent = value;
    }

    /**
     * Get substitutions from a source file
     * @param source the source file
     * @return the returned file
     */
    private File getSubstitution(File source) {
        File ret = substitutions.get(source);
        return ret == null ? source : ret;
    }

    /**
     * Get the file from the file chooser
     * @return the selected file
     */
    public File getMainFile() {
        return mainFile;
    }

    /**
     * Spawns a file chooser
     * @return the JFileChooser
     */
    public JFileChooser createChooser() {
        return JFileChoosers.createAt(getCurrentDirectory());
    }

    // used here and in LibraryManager only
    File getCurrentDirectory() {
        File ref;
        if (!filesOpening.empty()) {
            ref = filesOpening.peek();
        } else {
            ref = mainFile;
        }
        return ref == null ? null : ref.getParentFile();
    }

    private void setMainFile(File value) {
        mainFile = value;
    }

    /**
     * More substantive methods accessed from outside this package.
     */
    public void clear() {
        filesOpening.clear();
        mainFile = null;
    }

    /**
     * Open a file from an array of files to substitute.
     * @param file the main file
     * @param substitutions the substitution files
     * @return A functioning LogisimFile
     * @throws LoadFailedException if there was an error loading the file
     */
    public LogisimFile openLogisimFile(File file, Map<File,File> substitutions)
            throws LoadFailedException {
        this.substitutions = substitutions;
        // FIXME allevaton: What's up with this? Try finally with no catch?
        try {
            return openLogisimFile(file);
        } finally {
            this.substitutions = Collections.emptyMap();
        }
    }

    /**
     * Open a file from the hard drive.
     * @param file the file object
     * @return the LogisimFile object
     * @throws LoadFailedException if there was a problem loading the file
     */
    public LogisimFile openLogisimFile(File file) throws LoadFailedException {
        try {
            LogisimFile ret = loadLogisimFile(file);
            if (ret != null) {
                setMainFile(file);
            }

            showMessages(ret);
            return ret;
        } catch (LoaderException e) {
            logger.error( "An error occurred when loading the file.\n" 
                    + e.getMessage() );
            throw new LoadFailedException(e.getMessage(), e.isShown());
        }
    }

    /** 
     * Open a file from an input stream
     * @param reader The input stream
     * @return a fully functioning LogisimFile object
     * @throws LoadFailedException if it failed to load
     * @throws IOException if another error occurred
     */
    public LogisimFile openLogisimFile(InputStream reader)
            throws LoadFailedException, IOException {
        LogisimFile ret = null;
        try {
            ret = LogisimFile.load(reader, this);
        } catch (LoaderException e) {
            logger.error( "An error occurred when loading the file.\n" 
                    + e.getMessage() );
            return null;
        }
        showMessages(ret);
        return ret;
    }

    /**
     * Loads a specified Logisim library
     * @param file the library file
     * @return the library object
     */
    public Library loadLogisimLibrary(File file) {
        File actual = getSubstitution(file);
        LoadedLibrary ret = LibraryManager.instance.loadLogisimLibrary(this, actual);
        if (ret != null) {
            LogisimFile retBase = (LogisimFile) ret.getBase();
            showMessages(retBase);
        }
        return ret;
    }

    /**
     * Loads a library as a jar
     * @param file the jar file
     * @param className the class in the jar
     * @return the library object
     */
    public Library loadJarLibrary(File file, String className) {
        File actual = getSubstitution(file);
        return LibraryManager.instance.loadJarLibrary(this, actual, className);
    }

    /**
     * Reload the specified library.
     * @param lib The specified library
     */
    public void reload(LoadedLibrary lib) {
        LibraryManager.instance.reload(this, lib);
    }

    /**
     * Save the current file.
     * @param file The source file
     * @param dest The destination file
     * @return if the safe was a success
     */
    public boolean save(LogisimFile file, File dest) {
        Library reference = LibraryManager.instance.findReference(file, dest);
        if (reference != null) {
            JOptionPane.showMessageDialog(parent,
                    getFromLocale("fileCircularError", reference.getDisplayName()),
                    getFromLocale("fileSaveErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        File backup = determineBackupName(dest);
        boolean backupCreated = backup != null && dest.renameTo(backup);

        FileOutputStream fwrite = null;
        try {
            try {
                MacCompatibility.setFileCreatorAndType(dest, "LGSM", "circ");
            } catch (IOException e) {
                logger.warn( "Could not set Mac compatible" 
                        + " file associative flags (file creator and type)" );
            }
            fwrite = new FileOutputStream(dest);
            file.write(fwrite, this);
            file.setName(toProjectName(dest));

            File oldFile = getMainFile();
            setMainFile(dest);
            LibraryManager.instance.fileSaved(this, dest, oldFile, file);
        } catch (IOException e) {
            if (backupCreated) {
                logger.info( "Backup found, recovering from it." );
                recoverBackup(backup, dest);
            }

            if (dest.exists() && dest.length() == 0) {
                dest.delete();
            }

            JOptionPane.showMessageDialog(parent,
                getFromLocale("fileSaveError",
                    e.toString()),
                getFromLocale("fileSaveErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (fwrite != null) {
            try {
                fwrite.close();
            } catch (IOException e) {
                if (backupCreated) {
                    logger.info( "Backup found, recovering from it." );
                    recoverBackup(backup, dest);
                }
                if (dest.exists() && dest.length() == 0) {
                    dest.delete();
                }

                JOptionPane.showMessageDialog(parent,
                    getFromLocale("fileSaveCloseError",
                        e.toString()),
                    getFromLocale("fileSaveErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        if (!dest.exists() || dest.length() == 0) {
            if (backupCreated && backup != null && backup.exists()) {
                recoverBackup(backup, dest);
            } else {
                dest.delete();
            }
            JOptionPane.showMessageDialog(parent,
                    getFromLocale("fileSaveZeroError"),
                    getFromLocale("fileSaveErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (backupCreated && backup.exists()) {
            backup.delete();
        }
        return true;
    }

    /**
     * Get a name for a backup file
     * @param base the base file
     * @return the backup file
     */
    private static File determineBackupName(File base) {
        File dir = base.getParentFile();
        String name = base.getName();
        if (name.endsWith(LOGISIM_EXTENSION)) {
            name = name.substring(0, name.length() - LOGISIM_EXTENSION.length());
        }
        for (int i = 1; i <= 20; i++) {
            String ext = i == 1 ? ".bak" : (".bak" + i);
            File candidate = new File(dir, name + ext);
            if (!candidate.exists()) {
                return candidate;
            }

        }
        return null;
    }

    /**
     * Recover from a backup.
     * @param backup the backup file to recover from
     * @param dest where does the backup file go
     */
    private static void recoverBackup(File backup, File dest) {
        if (backup != null && backup.exists()) {
            if (dest.exists()) {
                dest.delete();
            }

            backup.renameTo(dest);
        }
    }

    /**
     * Loads a Logisim file
     * @param request the requested file
     * @return the completed file object
     */
    LogisimFile loadLogisimFile(File request) throws LoadFailedException {
        File actual = getSubstitution(request);
        for (File fileOpening : filesOpening) {
            if (fileOpening.equals(actual)) {
                logger.error( "An error occurred when opening the file.\n" 
                    + getFromLocale("logisimCircularError" ) );
                throw new LoadFailedException(getFromLocale("logisimCircularError",
                        toProjectName(actual)));
            }
        }

        LogisimFile ret = null;
        filesOpening.push(actual);
        try {
            ret = LogisimFile.load(actual, this);
        } catch (IOException e) {
            logger.error( "An error occurred when opening the file.\n" 
                + getFromLocale("logisimCircularError" ) );
            throw new LoadFailedException(getFromLocale("logisimLoadError",
                    toProjectName(actual), e.toString()));
        } finally {
            filesOpening.pop();
        }
        ret.setName(toProjectName(actual));
        return ret;
    }

    /**
     * Loads a Java jar file as a library
     * @param request the requested jar file
     * @param string the class name specified in the file
     * @return the loaded library
     */
    Library loadJarFile(File request, String className) throws LoadFailedException {
        File actual = getSubstitution(request);
        // Up until 2.1.8, this was written to use a URLClassLoader, which
        // worked pretty well, except that the class never releases its file
        // handles. For this reason, with 2.2.0, it's been switched to use
        // a custom-written class ZipClassLoader instead. The ZipClassLoader
        // is based on something downloaded off a forum, and I'm not as sure
        // that it works as well. It certainly does more file accesses.

        // Anyway, here's the line for this new version:
        ZipClassLoader loader = new ZipClassLoader(actual);

        // And here's the code that was present up until 2.1.8, and which I
        // know to work well except for the closing-files bit. If necessary, we
        // can revert by deleting the above declaration and reinstating the below.
        /*
        URL url;
        try {
            url = new URL("file", "localhost", file.getCanonicalPath());
        } catch (MalformedURLException e1) {
            throw new LoadFailedException("Internal error: Malformed URL");
        } catch (IOException e1) {
            throw new LoadFailedException(Strings.get("jarNotOpenedError"));
        }
        URLClassLoader loader = new URLClassLoader(new URL[] { url });
        */

        // load library class from loader
        Class<?> retClass;
        try {
            retClass = loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            logger.error( "Class not found" );
            throw new LoadFailedException(getFromLocale("jarClassNotFoundError", className));
        }
        if (!(Library.class.isAssignableFrom(retClass))) {
            logger.error( "Class not library" );
            throw new LoadFailedException(getFromLocale("jarClassNotLibraryError", className));
        }

        // instantiate library
        Library ret;
        try {
            ret = (Library) retClass.newInstance();
        } catch (Exception e) {
            logger.error( "Class not library" );
            throw new LoadFailedException(getFromLocale("jarLibraryNotCreatedError", className));
        }
        return ret;
    }

    //
    // Library methods
    //

    /**
     * Loads a Logisim library
     * @param desc the file descriptor
     * @return the library object
     */
    @Override
    public Library loadLibrary(String desc) {
        return LibraryManager.instance.loadLibrary(this, desc);
    }

    /**
     * Gets a descriptor from a library
     * @param lib the library
     * @return the descriptor
     */
    @Override
    public String getDescriptor(Library lib) {
        return LibraryManager.instance.getDescriptor(this, lib);
    }

    /**
     * Shows an error based on its description, also logs it
     * @param description the error text
     */
    @Override
    public void showError(String description) {
        logger.error( description );
        if (!filesOpening.empty()) {
            File top = filesOpening.peek();
            String init = toProjectName(top) + ":";
            if (description.contains("\n")) {
                description = init + "\n" + description;
            } else {
                description = init + " " + description;
            }
        }

        if (description.contains("\n") || description.length() > 60) {
            int lines = 1;
            for (int pos = description.indexOf('\n'); pos >= 0;
                    pos = description.indexOf('\n', pos + 1)) {
                lines++;
            }
            lines = Math.max(4, Math.min(lines, 7));

            JTextArea textArea = new JTextArea(lines, 60);
            textArea.setEditable(false);
            textArea.setText(description);
            textArea.setCaretPosition(0);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(350, 150));
            JOptionPane.showMessageDialog(parent, scrollPane,
                    getFromLocale("fileErrorTitle"), JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(parent, description,
                    getFromLocale("fileErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Show the messages from a file
     * @param source the file to load from 
     */
    private void showMessages(LogisimFile source) {
        if (source == null) {
            return;
        }

        String message = source.getMessage();
        while (message != null) {
            JOptionPane.showMessageDialog(parent,
                message, getFromLocale("fileMessageTitle"),
                JOptionPane.INFORMATION_MESSAGE);
            message = source.getMessage();
        }
    }

    //
    // helper methods
    //

    /**
     * Get a file from a name
     * @param name file name
     * @param filter the file name filter
     * @return the file object
     */
    File getFileFor(String name, FileFilter filter) {
        // Determine the actual file name.
        File file = new File(name);
        if (!file.isAbsolute()) {
            File currentDirectory = getCurrentDirectory();
            if (currentDirectory != null) {
                file = new File(currentDirectory, name);
            }

        }
        while (!file.canRead()) {
            // It doesn't exist. Figure it out from the user.
            JOptionPane.showMessageDialog(parent,
                getFromLocale("fileLibraryMissingError",
                    file.getName()));
            JFileChooser chooser = createChooser();
            chooser.setFileFilter(filter);
            chooser.setDialogTitle(getFromLocale("fileLibraryMissingTitle", file.getName()));
            int action = chooser.showDialog(parent, getFromLocale("fileLibraryMissingButton"));
            if (action != JFileChooser.APPROVE_OPTION) {
                throw new LoaderException(getFromLocale("fileLoadCanceledError"));
            }
            file = chooser.getSelectedFile();
        }
        return file;
    }

    /**
     * Conform a file to the current project
     * @param file the destined file
     * @return the project name
     */
    private String toProjectName(File file) {
        String ret = file.getName();
        if (ret.endsWith(LOGISIM_EXTENSION)) {
            return ret.substring(0, ret.length() - LOGISIM_EXTENSION.length());
        } else {
            return ret;
        }
    }

}
