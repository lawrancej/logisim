/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import static com.cburch.logisim.util.LocaleString.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import java.io.FileOutputStream;
import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.DefaultFontMapper.BaseFontParameters;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.proj.Project;

class ExportImage {
	private static final int SLIDER_DIVISIONS = 6;

	private static final int FORMAT_GIF = 0;
	private static final int FORMAT_PNG = 1;
	private static final int FORMAT_JPG = 2;
	private static final int FORMAT_SVG = 3;
	private static final int FORMAT_PDF = 4;
	
	private static final int BORDER_SIZE = 5;

	private ExportImage() { }

	static void doExport(Project proj) {
		// First display circuit/parameter selection dialog
		Frame frame = proj.getFrame();
		CircuitJList list = new CircuitJList(proj, true);
		if (list.getModel().getSize() == 0) {
			JOptionPane.showMessageDialog(proj.getFrame(),
					getFromLocale("exportEmptyCircuitsMessage"),
					getFromLocale("exportEmptyCircuitsTitle"),
					JOptionPane.YES_NO_OPTION);
			return;
		}
		OptionsPanel options = new OptionsPanel(list);
		int action = JOptionPane.showConfirmDialog(frame,
				options, getFromLocale("exportImageSelect"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (action != JOptionPane.OK_OPTION) return;
		List<Circuit> circuits = list.getSelectedValuesList();
		double scale = options.getScale();
		boolean printerView = options.getPrinterView();
		if (circuits.isEmpty()) return;
		
		ImageFileFilter filter;
		int fmt = options.getImageFormat();
		switch (options.getImageFormat()) {
		case FORMAT_GIF:
			filter = new ImageFileFilter(fmt, getFromLocale("exportGifFilter"),
				new String[] { "gif" });
			break;
		case FORMAT_PNG:
			filter = new ImageFileFilter(fmt, getFromLocale("exportPngFilter"),
				new String[] { "png" });
			break;
		case FORMAT_JPG:
			filter = new ImageFileFilter(fmt, getFromLocale("exportJpgFilter"),
				new String[] { "jpg", "jpeg", "jpe", "jfi", "jfif", "jfi" });
			break;
		case FORMAT_SVG:
			filter = null;
			filter = new ImageFileFilter(fmt, getFromLocale("exportSvgFilter"),
				new String[] { "svg" });
			break;
        case FORMAT_PDF:
          filter = null;
          filter = new ImageFileFilter(fmt, getFromLocale("exportPdfFilter"),
              new String[] { "pdf" });
          break;
		default:
			System.err.println("unexpected format; aborted"); //OK
			return;
		}
		
		// Then display file chooser
		Loader loader = proj.getLogisimFile().getLoader();
		JFileChooser chooser = loader.createChooser();
		if (circuits.size() > 1) {
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle(getFromLocale("exportImageDirectorySelect"));
		} else {
			chooser.setFileFilter(filter);
			chooser.setDialogTitle(getFromLocale("exportImageFileSelect"));
		}
		int returnVal = chooser.showDialog(frame, getFromLocale("exportImageButton"));
		if (returnVal != JFileChooser.APPROVE_OPTION) return;

		// Determine whether destination is valid
		File dest = chooser.getSelectedFile();
		chooser.setCurrentDirectory(dest.isDirectory() ? dest : dest.getParentFile());
		if (dest.exists()) {
			if (!dest.isDirectory()) {
				int confirm = JOptionPane.showConfirmDialog(proj.getFrame(),
					getFromLocale("confirmOverwriteMessage"),
					getFromLocale("confirmOverwriteTitle"),
					JOptionPane.YES_NO_OPTION);
				if (confirm != JOptionPane.YES_OPTION) return;
			}
		} else {
			if (circuits.size() > 1) {
				boolean created = dest.mkdir();
				if (!created) {
					JOptionPane.showMessageDialog(proj.getFrame(),
							getFromLocale("exportNewDirectoryErrorMessage"),
							getFromLocale("exportNewDirectoryErrorTitle"),
							JOptionPane.YES_NO_OPTION);
					return;
				}
			}
		}

		// Create the progress monitor
		ProgressMonitor monitor = new ProgressMonitor(frame,
				getFromLocale("exportImageProgress"),
				null,
				0, 10000);
		monitor.setMillisToDecideToPopup(100);
		monitor.setMillisToPopup(200);
		monitor.setProgress(0);

		// And start a thread to actually perform the operation
		// (This is run in a thread so that Swing will update the
		// monitor.)
		new ExportThread(frame, frame.getCanvas(), dest, filter,
				circuits, scale, printerView, monitor).start();

	}

	private static class OptionsPanel extends JPanel implements ChangeListener {
		JSlider slider;
		JLabel curScale;
		JCheckBox printerView;
		JRadioButton formatPng;
		JRadioButton formatGif;
		JRadioButton formatJpg;
		JRadioButton formatSvg;
		JRadioButton formatPdf;
		GridBagLayout gridbag;
		GridBagConstraints gbc;
		Dimension curScaleDim;

		OptionsPanel(JList list) {
			// set up components
			formatPng = new JRadioButton("PNG");
			formatGif = new JRadioButton("GIF");
			formatJpg = new JRadioButton("JPEG");
			formatSvg = new JRadioButton("SVG");
			formatPdf = new JRadioButton("PDF");
			ButtonGroup bgroup = new ButtonGroup();
			bgroup.add(formatPng);
			bgroup.add(formatGif);
			bgroup.add(formatJpg);
			bgroup.add(formatSvg);
			bgroup.add(formatPdf);
			formatPng.setSelected(true);

			slider = new JSlider(JSlider.HORIZONTAL,
					-3 * SLIDER_DIVISIONS, 3 * SLIDER_DIVISIONS, 0);
			slider.setMajorTickSpacing(10);
			slider.addChangeListener(this);
			curScale = new JLabel("222%");
			curScale.setHorizontalAlignment(SwingConstants.RIGHT);
			curScale.setVerticalAlignment(SwingConstants.CENTER);
			curScaleDim = new Dimension(curScale.getPreferredSize());
			curScaleDim.height = Math.max(curScaleDim.height,
					slider.getPreferredSize().height);
			stateChanged(null);

			printerView = new JCheckBox();
			printerView.setSelected(true);

			// set up panel
			gridbag = new GridBagLayout();
			gbc = new GridBagConstraints();
			setLayout(gridbag);

			// now add components into panel
			gbc.gridy = 0;
			gbc.gridx = GridBagConstraints.RELATIVE;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.insets = new Insets(5, 0, 5, 0);
			gbc.fill = GridBagConstraints.NONE;
			addGb(new JLabel(getFromLocale("labelCircuits") + " "));
			gbc.fill = GridBagConstraints.HORIZONTAL;
			addGb(new JScrollPane(list));
			gbc.fill = GridBagConstraints.NONE;
			
			gbc.gridy++;
			addGb(new JLabel(getFromLocale("labelImageFormat") + " "));
			Box formatsPanel = new Box(BoxLayout.Y_AXIS);
			formatsPanel.add(formatPng);
			formatsPanel.add(formatGif);
			formatsPanel.add(formatJpg);
			formatsPanel.add(formatSvg);
			formatsPanel.add(formatPdf);
			addGb(formatsPanel);
			
			gbc.gridy++;
			addGb(new JLabel(getFromLocale("labelScale") + " "));
			addGb(slider);
			addGb(curScale);
			
			gbc.gridy++;
			addGb(new JLabel(getFromLocale("labelPrinterView") + " "));
			addGb(printerView);
		}
		
		private void addGb(JComponent comp) {
			gridbag.setConstraints(comp, gbc);
			add(comp);
		}

		double getScale() {
			return Math.pow(2.0, (double) slider.getValue() / SLIDER_DIVISIONS);
		}
		
		boolean getPrinterView() { return printerView.isSelected(); }
		
		int getImageFormat() {
			if (formatGif.isSelected()) return FORMAT_GIF;
			if (formatJpg.isSelected()) return FORMAT_JPG;
			if (formatSvg.isSelected()) return FORMAT_SVG;
			if (formatPdf.isSelected()) return FORMAT_PDF;
			return FORMAT_PNG;
		}

		public void stateChanged(ChangeEvent e) {
			double scale = getScale();
			curScale.setText((int) Math.round(100.0 * scale) + "%");
			if (curScaleDim != null) curScale.setPreferredSize(curScaleDim);
		}
	}
	
	private static class ImageFileFilter extends FileFilter {
		private int type;
		private String[] extensions;
		private String desc;
		
		private ImageFileFilter(int type, String desc, String[] exts) {
			this.type = type;
			this.desc = desc;
			extensions = new String[exts.length];
			for (int i = 0; i < exts.length; i++) {
				extensions[i] = "." + exts[i].toLowerCase();    
			}
		}
		
		@Override
		public boolean accept(File f) {
			String name = f.getName().toLowerCase();
			for (int i = 0; i < extensions.length; i++) {
				if (name.endsWith(extensions[i])) return true;
			}
			return f.isDirectory();
		}

		@Override
		public String getDescription() {
			return desc.toString();
		}
	}

	private static class ExportThread extends Thread {
		Frame frame;
		Canvas canvas;
		File dest;
		ImageFileFilter filter;
		List<Circuit> circuits;
		double scale;
		boolean printerView;
		ProgressMonitor monitor;

		ExportThread(Frame frame, Canvas canvas, File dest, ImageFileFilter f,
				List<Circuit> circuits, double scale, boolean printerView,
				ProgressMonitor monitor) {
			this.frame = frame;
			this.canvas = canvas;
			this.dest = dest;
			this.filter = f;
			this.circuits = circuits;
			this.scale = scale;
			this.printerView = printerView;
			this.monitor = monitor;
		}

		@Override
		public void run() {
			for (Circuit circ : circuits) {
				export(circ);
			}
		}
		
		private void export(Circuit circuit) {
          Bounds bds = circuit.getBounds(canvas.getGraphics())
              .expand(BORDER_SIZE);
          int width = (int) Math.round(bds.getWidth() * scale);
          int height = (int) Math
              .round(bds.getHeight() * scale);
          BufferedImage img = new BufferedImage(width, height,
              BufferedImage.TYPE_INT_RGB);
    
          File where;
          if (dest.isDirectory()) {
            where = new File(dest, circuit.getName()
                + filter.extensions[0]);
          } else if (filter.accept(dest)) {
            where = dest;
          } else {
            String newName = dest.getName()
                + filter.extensions[0];
            where = new File(dest.getParentFile(), newName);
          }
    
          try (FileOutputStream stream = new FileOutputStream(
              where)) {
    
            if (filter.type == FORMAT_SVG) {
              DOMImplementation domImpl = GenericDOMImplementation
                  .getDOMImplementation();
    
              // Create an instance of org.w3c.dom.Document.
              String svgNS = "http://www.w3.org/2000/svg";
              Document document = domImpl.createDocument(svgNS,
                  "svg", null);
    
              // Create an instance of the SVG Generator.
              SVGGraphics2D svgGenerator = new SVGGraphics2D(
                  document);
    
              paint(svgGenerator, bds, circuit, width, height);
    
              // Finally, stream out SVG to the standard output using
              // UTF-8 encoding.
              boolean useCSS = true; // we want to use CSS style attributes
    
              Writer out = new OutputStreamWriter(stream,
                  "UTF-8");
              svgGenerator.stream(out, useCSS);
            } else if (filter.type == FORMAT_PDF) {
              com.itextpdf.text.Document document = new com.itextpdf.text.Document(new Rectangle(width, height));
              PdfWriter writer = PdfWriter.getInstance(document, stream);
              document.open();
              PdfContentByte cb = writer.getDirectContent();
              DefaultFontMapper fontMapper = new DefaultFontMapper();
              BaseFontParameters fontParameters = new BaseFontParameters("LiberationSans-Regular.ttf");
              fontParameters.encoding = BaseFont.IDENTITY_H;
              fontMapper.putName("Dialog.plain", fontParameters);
              fontMapper.putName("SansSerif.plain", fontParameters);
              PdfGraphics2D pdfGraphics = new PdfGraphics2D(cb, width, height, fontMapper);
              paint(pdfGraphics, bds, circuit, width, height);
              pdfGraphics.dispose();
              document.close();
            } else {
              paint(img.getGraphics(), bds, circuit, width,
                  height);
              String formatName = null;
              switch (filter.type) {
              case FORMAT_GIF:
                formatName = "GIF";
                break;
              case FORMAT_PNG:
                formatName = "PNG";
                break;
              case FORMAT_JPG:
                formatName = "JPEG";
                break;
              }
              ImageIO.write(img, formatName, stream);
            }
    
          } catch (Exception e) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String stackTrace = writer.toString();
            JTextArea textArea = new JTextArea( getFromLocale("couldNotCreateFile") + "\n" + stackTrace);
            JOptionPane.showMessageDialog(frame, textArea);
            monitor.close();
            return;
          }
          monitor.close();
		}
		
		private void paint(Graphics base, Bounds bds, Circuit circuit, int width, int height) {
			Graphics g = base.create();
			g.setColor(Color.white);
			g.fillRect(0, 0, width, height);
			g.setColor(Color.black);
			if (g instanceof Graphics2D) {
				((Graphics2D) g).scale(scale, scale);
				((Graphics2D) g).translate(-bds.getX(), -bds.getY());
			} else {
				JOptionPane.showMessageDialog(frame,
						getFromLocale("couldNotCreateImage"));
				monitor.close();
			}

			CircuitState circuitState = canvas.getProject().getCircuitState(circuit);
			ComponentDrawContext context = new ComponentDrawContext(canvas,
					circuit, circuitState, base, g, printerView);
			circuit.draw(context, null);
			g.dispose();
		}
	}
}
