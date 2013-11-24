package com.cburch.logisim.SVG;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;

public class Image {
	static final int IMAGE_BORDER = 10;
	static final int IMAGE_WIDTH = 200;
	static final int IMAGE_HEIGHT = 200;
	protected static JSVGCanvas svgCanvas =new JSVGCanvas();
	
	public static Component createComponent(String name) {
		svgCanvas.setURI(Image.class.getResource("/logisim/icons/" + name).toString());
		svgCanvas.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {});
		svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {});
		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {});
		JPanel p =new JPanel(new BorderLayout());
		p.setPreferredSize(new Dimension(300 * 3, 300 *16/9 * 3));
		p.add("Center", svgCanvas);
		return p;
	}
}
