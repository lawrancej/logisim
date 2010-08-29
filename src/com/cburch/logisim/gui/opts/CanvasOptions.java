/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.opts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.cburch.logisim.circuit.RadixOption;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.util.StringGetter;
import com.cburch.logisim.util.TableLayout;

class CanvasOptions extends OptionsPanel {
	private static class ZoomOption {
		private String title;
		private Double ratio;
		
		ZoomOption(String title, double ratio) {
			this.title = title;
			this.ratio = new Double(ratio);
		}
		
		@Override
		public String toString() {
			return title;
		}
	}
	
	private static class RadixOpt {
		private RadixOption value;
		
		RadixOpt(RadixOption value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value.toDisplayString();
		}
	}
	
	private class MyListener implements ActionListener, AttributeListener {
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			if (source == zoom) {
				ZoomOption opt = (ZoomOption) zoom.getSelectedItem();
				if (opt != null) {
					AttributeSet attrs = getOptions().getAttributeSet();
					getProject().doAction(OptionsActions.setAttribute(attrs,
						    Options.zoom_attr, opt.ratio));
				}
			} else if (source == radix1) {
				RadixOpt opt = (RadixOpt) radix1.getSelectedItem();
				if (opt != null) {
					AttributeSet attrs = getOptions().getAttributeSet();
					getProject().doAction(OptionsActions.setAttribute(attrs,
						    Options.ATTR_RADIX_1, opt.value));
				}
			} else if (source == radix2) {
				RadixOpt opt = (RadixOpt) radix2.getSelectedItem();
				if (opt != null) {
					AttributeSet attrs = getOptions().getAttributeSet();
					getProject().doAction(OptionsActions.setAttribute(attrs,
						    Options.ATTR_RADIX_2, opt.value));
				}
			}
		}
		
		public void attributeListChanged(AttributeEvent e) { }
		public void attributeValueChanged(AttributeEvent e) {
			for (int i = 0; i < checks.length; i++) {
				checks[i].attributeChanged(e);
			}

			Attribute<?> attr = e.getAttribute();
			Object val = e.getValue();
			if (attr == Options.zoom_attr) {
				loadZoom((Double) val);
			}
		}
		
		private void loadZoom(Double val) {
			double value = val.doubleValue();
			ComboBoxModel model = zoom.getModel();
			for (int i = 0; i < model.getSize(); i++) {
				ZoomOption opt = (ZoomOption) model.getElementAt(i);
				if (Math.abs(opt.ratio.doubleValue() - value) < 0.01) {
					zoom.setSelectedItem(opt);
				}
			}
		}
	}

	private class BooleanOption extends JCheckBox
			implements ActionListener {
		Attribute<Boolean> attr;
		StringGetter title;
		
		BooleanOption(Attribute<Boolean> attr, StringGetter title) {
			super(title.get());
			this.attr = attr;
			this.title = title;
		
			addActionListener(this);
			Boolean cur = getLogisimFile().getOptions().getAttributeSet().getValue(attr);
			if (cur != null) setSelected(cur.booleanValue());
		}
		
		void localeChanged() {
			setText(title.get());
		}
		
		public void actionPerformed(ActionEvent e) {
			AttributeSet attrs = getLogisimFile().getOptions().getAttributeSet();
			getProject().doAction(OptionsActions.setAttribute(attrs, attr, Boolean.valueOf(isSelected())));
		}
		
		public void attributeChanged(AttributeEvent e) {
			if (e.getAttribute() == attr) {
				setSelected(((Boolean) e.getValue()).booleanValue());
			}
		}
	}
	
	private MyListener myListener = new MyListener();

	private BooleanOption[] checks = {
		new BooleanOption(Options.preview_attr,
			Strings.getter("canvasPrinterView")),
		new BooleanOption(Options.showgrid_attr,
			Strings.getter("canvasShowGrid")),
		new BooleanOption(Options.showhalo_attr,
			Strings.getter("canvasShowHalo")),
		new BooleanOption(Options.showtips_attr,
			Strings.getter("canvasShowTips")),
		new BooleanOption(Options.ATTR_CONNECT_ON_MOVE,
				Strings.getter("canvasConnectOnMove")),
	};
	private JLabel zoomLabel = new JLabel();
	private JComboBox zoom = new JComboBox(new ZoomOption[] {
			new ZoomOption("1:1", 1.0),
			new ZoomOption("1:1.33", 1.33),
			new ZoomOption("1:1.5", 1.5),
			new ZoomOption("1:2", 2.0),
			new ZoomOption("1.33:1", 0.75),
			new ZoomOption("2:1", 0.5),
			new ZoomOption("5:1", 0.2),
	});
	private JLabel radix1Label = new JLabel();
	private JComboBox radix1;
	private JLabel radix2Label = new JLabel();
	private JComboBox radix2;

	public CanvasOptions(OptionsFrame window) {
		super(window);
		
		AttributeSet attrs = window.getProject().getOptions().getAttributeSet();
		for (int i = 0; i < 2; i++) {
			RadixOption opt = attrs.getValue(i == 0 ? Options.ATTR_RADIX_1 : Options.ATTR_RADIX_2);
			RadixOption[] opts = RadixOption.OPTIONS;
			RadixOpt[] items = new RadixOpt[opts.length];
			RadixOpt item = null;
			for (int j = 0; j < RadixOption.OPTIONS.length; j++) {
				items[j] = new RadixOpt(opts[j]);
				if (opts[j] == opt) item = items[j];
			}
			JComboBox box = new JComboBox(items);
			if (item != null) box.setSelectedItem(item);
			box.addActionListener(myListener);
			if (i == 0) radix1 = box; else radix2 = box;
		}
		
		JPanel panel = new JPanel(new TableLayout(2));
		panel.add(zoomLabel);
		panel.add(zoom);
		zoom.addActionListener(myListener);
		
		panel.add(radix1Label);
		panel.add(radix1);
		
		panel.add(radix2Label);
		panel.add(radix2);

		setLayout(new TableLayout(1));
		for (int i = 0; i < checks.length; i++) {
			add(checks[i]);
		}
		add(panel);
		
		window.getOptions().getAttributeSet().addAttributeListener(myListener);
		myListener.loadZoom(getOptions().getAttributeSet().getValue(Options.zoom_attr));
	}

	@Override
	public String getTitle() {
		return Strings.get("canvasTitle");
	}

	@Override
	public String getHelpText() {
		return Strings.get("canvasHelp");
	}
	
	@Override
	public void localeChanged() {
		for (int i = 0; i < checks.length; i++) {
			checks[i].localeChanged();
		}
		zoomLabel.setText(Strings.get("canvasZoom"));
		radix1Label.setText(Strings.get("canvasRadix1"));
		radix2Label.setText(Strings.get("canvasRadix2"));
	}
}
