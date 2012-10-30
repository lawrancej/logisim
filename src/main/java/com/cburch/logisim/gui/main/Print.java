/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.proj.Project;

import static com.cburch.logisim.util.LocaleString.*;

public class Print {
	private Print() { }
	
	public static void doPrint(Project proj) {
		CircuitJList list = new CircuitJList(proj, true);
		Frame frame = proj.getFrame();
		if (list.getModel().getSize() == 0) {
			JOptionPane.showMessageDialog(proj.getFrame(),
					_("printEmptyCircuitsMessage"),
					_("printEmptyCircuitsTitle"),
					JOptionPane.YES_NO_OPTION);
			return;
		}
		ParmsPanel parmsPanel = new ParmsPanel(list);
		int action = JOptionPane.showConfirmDialog(frame,
				parmsPanel, _("printParmsTitle"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (action != JOptionPane.OK_OPTION) return;
		List<Circuit> circuits = list.getSelectedCircuits();
		if (circuits.isEmpty()) return;
		
		PageFormat format = new PageFormat();
		Printable print = new MyPrintable(proj, circuits,
				parmsPanel.getHeader(),
				parmsPanel.getRotateToFit(),
				parmsPanel.getPrinterView());
		
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPrintable(print, format);
		if (job.printDialog() == false) return;
		try {
			job.print();
		} catch (PrinterException e) {
			JOptionPane.showMessageDialog(proj.getFrame(),
					_("printError", e.toString()),
					_("printErrorTitle"),
					JOptionPane.ERROR_MESSAGE);
		}
	}
		
	private static class ParmsPanel extends JPanel {
		JCheckBox rotateToFit;
		JCheckBox printerView;
		JTextField header;
		GridBagLayout gridbag;
		GridBagConstraints gbc;
		
		ParmsPanel(JList list) {
			// set up components
			rotateToFit = new JCheckBox();
			rotateToFit.setSelected(true);
			printerView = new JCheckBox();
			printerView.setSelected(true);
			header = new JTextField(20);
			header.setText("%n (%p of %P)");
			
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
			addGb(new JLabel(_("labelCircuits") + " "));
			gbc.fill = GridBagConstraints.HORIZONTAL;
			addGb(new JScrollPane(list));
			gbc.fill = GridBagConstraints.NONE;
			
			gbc.gridy++;
			addGb(new JLabel(_("labelHeader") + " "));
			addGb(header);
			
			gbc.gridy++;
			addGb(new JLabel(_("labelRotateToFit") + " "));
			addGb(rotateToFit);
			
			gbc.gridy++;
			addGb(new JLabel(_("labelPrinterView") + " "));
			addGb(printerView);
		}
		
		private void addGb(JComponent comp) {
			gridbag.setConstraints(comp, gbc);
			add(comp);
		}
		
		boolean getRotateToFit() { return rotateToFit.isSelected(); }
		boolean getPrinterView() { return printerView.isSelected(); }
		String getHeader() { return header.getText(); }
	}
	
	private static class MyPrintable implements Printable {
		Project proj;
		List<Circuit> circuits;
		String header;
		boolean rotateToFit;
		boolean printerView;
		
		MyPrintable(Project proj, List<Circuit> circuits, String header,
				boolean rotateToFit, boolean printerView) {
			this.proj = proj;
			this.circuits = circuits;
			this.header = header;
			this.rotateToFit = rotateToFit;
			this.printerView = printerView;
		}
		
		public int print(Graphics base, PageFormat format, int pageIndex) {
			if (pageIndex >= circuits.size()) return Printable.NO_SUCH_PAGE;
			
			Circuit circ = circuits.get(pageIndex);
			CircuitState circState = proj.getCircuitState(circ);
			Graphics g = base.create();
			Graphics2D g2 = g instanceof Graphics2D ? (Graphics2D) g : null;
			FontMetrics fm = g.getFontMetrics();
			String head = (header != null && !header.equals(""))
				? format(header, pageIndex + 1, circuits.size(),
						circ.getName())
				: null;
			int headHeight = (head == null ? 0 : fm.getHeight());

			// Compute image size
			double imWidth = format.getImageableWidth();
			double imHeight = format.getImageableHeight();
			
			// Correct coordinate system for page, including
			// translation and possible rotation.
			Bounds bds = circ.getBounds(g).expand(4);
			double scale = Math.min(imWidth / bds.getWidth(),
					(imHeight - headHeight) / bds.getHeight());
			if (g2 != null) {
				g2.translate(format.getImageableX(), format.getImageableY());
				if (rotateToFit && scale < 1.0 / 1.1) {
					double scale2 = Math.min(imHeight / bds.getWidth(),
							(imWidth - headHeight) / bds.getHeight());
					if (scale2 >= scale * 1.1) { // will rotate
						scale = scale2;
						if (imHeight > imWidth) { // portrait -> landscape
							g2.translate(0, imHeight);
							g2.rotate(-Math.PI / 2);
						} else { // landscape -> portrait
							g2.translate(imWidth, 0);
							g2.rotate(Math.PI / 2);
						}
						double t = imHeight;
						imHeight = imWidth;
						imWidth = t;
					}
				}
			}
			
			// Draw the header line if appropriate
			if (head != null) {
				g.drawString(head,
						(int) Math.round((imWidth - fm.stringWidth(head)) / 2),
						fm.getAscent());
				if (g2 != null) {
					imHeight -= headHeight;
					g2.translate(0, headHeight);
				}
			}
			
			// Now change coordinate system for circuit, including
			// translation and possible scaling
			if (g2 != null) {
				if (scale < 1.0) {
					g2.scale(scale, scale);
					imWidth /= scale;
					imHeight /= scale;
				}
				double dx = Math.max(0.0, (imWidth - bds.getWidth()) / 2);
				g2.translate(-bds.getX() + dx, -bds.getY());
			}
			
			// Ensure that the circuit is eligible to be drawn
			Rectangle clip = g.getClipBounds();
			clip.add(bds.getX(), bds.getY());
			clip.add(bds.getX() + bds.getWidth(),
					bds.getY() + bds.getHeight());
			g.setClip(clip);
			
			// And finally draw the circuit onto the page
			ComponentDrawContext context = new ComponentDrawContext(
					proj.getFrame().getCanvas(), circ, circState,
					base, g, printerView);
			Collection<Component> noComps = Collections.emptySet();
			circ.draw(context, noComps);
			g.dispose();
			return Printable.PAGE_EXISTS;
		}
	}
	
	private static String format(String header, int index, int max,
			String circName) {
		header = header.replace("%n", "%1$s");
		header = header.replace("%p", "%2$d");
		header = header.replace("%P", "%3$d");
		return String.format(header, circName, index, max);
	}
}
