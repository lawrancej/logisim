/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.start;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;

import com.cburch.logisim.Main;

@SuppressWarnings("serial")
public class About {
    static final int IMAGE_BORDER = 10;
    static final int IMAGE_WIDTH = 380;
    static final int IMAGE_HEIGHT = 284;
    protected static JSVGCanvas svgCanvas =new JSVGCanvas();

    public static JComponent createComponents() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add("Center", svgCanvas);
        svgCanvas.setURI(About.class.getResource("/logisim/drawing.svg").toString());
        svgCanvas.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {});
        svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {});
        svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {});
        return panel;
    }

    public static void showAboutDialog(JFrame owner) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createComponents());
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        JOptionPane.showMessageDialog(owner, panel,
                "Logisim " + Main.VERSION_NAME, JOptionPane.PLAIN_MESSAGE);
    }
}