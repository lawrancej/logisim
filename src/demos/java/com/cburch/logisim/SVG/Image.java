package com.cburch.logisim.SVG;

import java.awt.Point;

import javax.swing.JPanel;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;

public class Image {
	protected static JSVGCanvas svgCanvas =new JSVGCanvas();
	
	public static java.awt.Image createComponent(String name, Point loc) {
		svgCanvas.setURI(Image.class.getResource("/logisim/icons/" + name).toString());
		svgCanvas.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {});
		svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {});
		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {});
		JPanel p =new JPanel();
		p.setLayout(null);
		p.setSize(50,50);
		svgCanvas.setLayout(null);
		svgCanvas.setLocation(loc);
		svgCanvas.setSize(50, 50);
		p.add(svgCanvas);
		java.awt.Image img = svgCanvas.createImage(50, 50);
		return img;
	}
}
