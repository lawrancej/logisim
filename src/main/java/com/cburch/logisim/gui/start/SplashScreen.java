/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.start;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import static com.cburch.logisim.util.LocaleString.getFromLocale;

@SuppressWarnings("serial")
public class SplashScreen extends JWindow implements ActionListener {
    public static final int LIBRARIES = 0;
    public static final int TEMPLATE_CREATE = 1;
    public static final int TEMPLATE_OPEN = 2;
    public static final int TEMPLATE_LOAD = 3;
    public static final int TEMPLATE_CLOSE = 4;
    public static final int GUI_INIT = 5;
    public static final int FILE_CREATE = 6;
    public static final int FILE_LOAD = 7;
    public static final int PROJECT_CREATE = 8;
    public static final int FRAME_CREATE = 9;

    private static final int PROGRESS_MAX = 3568;
    private static final boolean PRINT_TIMES = false;

    private static class Marker {
        int count;
        String message;
        Marker(int count, String message) {
            this.count = count;
            this.message = message;
        }
    }

    Marker[] markers = new Marker[] {
            new Marker(377, getFromLocale("progressLibraries")),
            new Marker(990, getFromLocale("progressTemplateCreate")),
            new Marker(1002, getFromLocale("progressTemplateOpen")),
            new Marker(1002, getFromLocale("progressTemplateLoad")),
            new Marker(1470, getFromLocale("progressTemplateClose")),
            new Marker(1478, getFromLocale("progressGuiInitialize")),
            new Marker(2114, getFromLocale("progressFileCreate")),
            new Marker(2114, getFromLocale("progressFileLoad")),
            new Marker(2383, getFromLocale("progressProjectCreate")),
            new Marker(2519, getFromLocale("progressFrameCreate")),
    };
    // for avoiding mutual recursion
    boolean inClose = false;
    JProgressBar progress = new JProgressBar(0, PROGRESS_MAX);
    JButton close = new JButton(getFromLocale("startupCloseButton"));
    JButton cancel = new JButton(getFromLocale("startupQuitButton"));
    long startTime = System.currentTimeMillis();

    public SplashScreen() {
        JPanel imagePanel = new JPanel();
        imagePanel.add(About.createComponents());
        imagePanel.setBorder(null);

        progress.setStringPainted(true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(close);
        close.addActionListener(this);
        buttonPanel.add(cancel);
        cancel.addActionListener(this);

        JPanel contents = new JPanel(new BorderLayout());
        contents.add(imagePanel, BorderLayout.NORTH);
        contents.add(progress, BorderLayout.CENTER);
        contents.add(buttonPanel, BorderLayout.SOUTH);
        contents.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        Color bg = imagePanel.getBackground();
        contents.setBackground(bg);
        buttonPanel.setBackground(bg);
        setBackground(bg);
        setContentPane(contents);
    }

    public void setProgress(int markerId) {
        final Marker marker = markers == null ? null : markers[markerId];
        if (marker != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progress.setString(marker.message);
                    progress.setValue(marker.count);
                }
            });
            if (PRINT_TIMES) {
                //OK
                System.err.println((System.currentTimeMillis() - startTime)
                        + " " + marker.message);
            }
        } else {
            if (PRINT_TIMES) {
                //OK
                System.err.println((System.currentTimeMillis() - startTime) + " ??");
            }
        }
    }

    @Override
    public void setVisible(boolean value) {
        if (value) {
            pack();
            Dimension dim = getToolkit().getScreenSize();
            int x = (int) (dim.getWidth() - getWidth()) / 2;
            int y = (int) (dim.getHeight() - getHeight()) / 2;
            setLocation(x, y);
        }
        super.setVisible(value);
    }

    public void close() {
        if (inClose) {
            return;
        }

        inClose = true;
        setVisible(false);
        inClose = false;
        if (PRINT_TIMES) {
            //OK
            System.err.println((System.currentTimeMillis() - startTime)
                    + " closed");
        }
        markers = null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cancel) {
            System.exit(0);
        } else if (src == close) {
            close();
        }
    }
}
