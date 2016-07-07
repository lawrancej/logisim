/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import net.roydesign.io.DocumentFile;
import net.roydesign.mac.MRJAdapter;
import net.roydesign.mac.SystemAnalyzer;

import javax.swing.JMenuBar;
import java.io.File;
import java.io.IOException;

public class MacCompatibility {
    private MacCompatibility() { }

    public static boolean isSwingUsingScreenMenuBar() {
        try {
            return MRJAdapter.isSwingUsingScreenMenuBar();
        } catch (Exception t) {
            return false;
        }
    }

    public static void setFramelessJMenuBar(JMenuBar menubar) {
        try {
            MRJAdapter.setFramelessJMenuBar(menubar);
        } catch (Exception t) { }
    }

    public static void setFileCreatorAndType(File dest, String app, String type)
            throws IOException {
        IOException ioExcept = null;
        try {
            try {
                DocumentFile.setFileCreatorAndType(dest, app, type);
            } catch (IOException e) {
                ioExcept = e;
            }
        } catch (Exception t) { }
        if (ioExcept != null) {
            throw ioExcept;
        }
    }

    public static boolean isMacOSX() {
        return SystemAnalyzer.isMacOSX();
    }

}
