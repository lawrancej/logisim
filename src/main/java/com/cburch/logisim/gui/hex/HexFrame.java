/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.hex;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.cburch.hex.HexEditor;
import com.cburch.hex.HexModel;
import com.cburch.logisim.gui.generic.LFrame;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.util.JFileChoosers;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.WindowMenuItemManager;
import static com.cburch.logisim.util.LocaleString.*;

public class HexFrame extends LFrame {
	private class WindowMenuManager extends WindowMenuItemManager
			implements LocaleListener {
		WindowMenuManager() {
			super(_("hexFrameMenuItem"), false);
			LocaleManager.addLocaleListener(this);
		}
		
		@Override
		public JFrame getJFrame(boolean create) {
			return HexFrame.this;
		}
		
		public void localeChanged() {
			setText(_("hexFrameMenuItem"));
		}
	}

	private class MyListener
			implements ActionListener, LocaleListener {
		private File lastFile = null;
		
		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();
			if (src == open) {
				JFileChooser chooser = JFileChoosers.createSelected(lastFile);
				chooser.setDialogTitle(_("openButton"));
				int choice = chooser.showOpenDialog(HexFrame.this);
				if (choice == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					try {
						HexFile.open(model, f);
						lastFile = f;
					} catch (IOException e) {
						JOptionPane.showMessageDialog(HexFrame.this, e.getMessage(),
								_("hexOpenErrorTitle"), JOptionPane.ERROR_MESSAGE);
					}
				}
			} else if (src == save) {
				JFileChooser chooser = JFileChoosers.createSelected(lastFile);
				chooser.setDialogTitle(_("saveButton"));
				int choice = chooser.showSaveDialog(HexFrame.this);
				if (choice == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					try {
						HexFile.save(f, model);
						lastFile = f;
					} catch (IOException e) {
						JOptionPane.showMessageDialog(HexFrame.this, e.getMessage(),
							_("hexSaveErrorTitle"), JOptionPane.ERROR_MESSAGE);
					}
				}
			} else if (src == close) {
				WindowEvent e = new WindowEvent(HexFrame.this,
						WindowEvent.WINDOW_CLOSING);
				HexFrame.this.processWindowEvent(e);
			}
		}
		
		public void localeChanged() {
			setTitle(_("hexFrameTitle"));
			open.setText(_("openButton"));
			save.setText(_("saveButton"));
			close.setText(_("closeButton"));
		}
	}
	
	private class EditListener implements ActionListener, ChangeListener {
		private Clip clip = null;
		
		private Clip getClip() {
			if (clip == null) clip = new Clip(editor);
			return clip;
		}
		
		private void register(LogisimMenuBar menubar) {
			menubar.addActionListener(LogisimMenuBar.CUT, this);
			menubar.addActionListener(LogisimMenuBar.COPY, this);
			menubar.addActionListener(LogisimMenuBar.PASTE, this);
			menubar.addActionListener(LogisimMenuBar.DELETE, this);
			menubar.addActionListener(LogisimMenuBar.SELECT_ALL, this);
			enableItems(menubar);
		}
		
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if (src == LogisimMenuBar.CUT) {
				getClip().copy();
				editor.delete();
			} else if (src == LogisimMenuBar.COPY) {
				getClip().copy();
			} else if (src == LogisimMenuBar.PASTE) {
				getClip().paste();
			} else if (src == LogisimMenuBar.DELETE) {
				editor.delete();
			} else if (src == LogisimMenuBar.SELECT_ALL) {
				editor.selectAll();
			}
		}
		
		private void enableItems(LogisimMenuBar menubar) {
			boolean sel = editor.selectionExists();
			boolean clip = true; //TODO editor.clipboardExists();
			menubar.setEnabled(LogisimMenuBar.CUT, sel);
			menubar.setEnabled(LogisimMenuBar.COPY, sel);
			menubar.setEnabled(LogisimMenuBar.PASTE, clip);
			menubar.setEnabled(LogisimMenuBar.DELETE, sel);
			menubar.setEnabled(LogisimMenuBar.SELECT_ALL, true);
		}

		public void stateChanged(ChangeEvent e) {
			enableItems((LogisimMenuBar) getJMenuBar());
		}
	}

	private WindowMenuManager windowManager = new WindowMenuManager();
	private EditListener editListener = new EditListener();
	private MyListener myListener = new MyListener();
	private HexModel model;
	private HexEditor editor;
	private JButton open = new JButton();
	private JButton save = new JButton();
	private JButton close = new JButton();

	public HexFrame(Project proj, HexModel model) {
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		LogisimMenuBar menubar = new LogisimMenuBar(this, proj);
		setJMenuBar(menubar);
		
		this.model = model;
		this.editor = new HexEditor(model);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(open);
		buttonPanel.add(save);
		buttonPanel.add(close);
		open.addActionListener(myListener);
		save.addActionListener(myListener);
		close.addActionListener(myListener);
		
		Dimension pref = editor.getPreferredSize();
		JScrollPane scroll = new JScrollPane(editor,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		pref.height = Math.min(pref.height, pref.width * 3 / 2);
		scroll.setPreferredSize(pref);
		scroll.getViewport().setBackground(editor.getBackground());

		Container contents = getContentPane();
		contents.add(scroll, BorderLayout.CENTER);
		contents.add(buttonPanel, BorderLayout.SOUTH);

		LocaleManager.addLocaleListener(myListener);
		myListener.localeChanged();
		pack();
		
		Dimension size = getSize();
		Dimension screen = getToolkit().getScreenSize();
		if (size.width > screen.width || size.height > screen.height) {
			size.width = Math.min(size.width, screen.width);
			size.height = Math.min(size.height, screen.height);
			setSize(size);
		}

		editor.getCaret().addChangeListener(editListener);
		editor.getCaret().setDot(0, false);
		editListener.register(menubar);
	}
	
	@Override
	public void setVisible(boolean value) {
		if (value && !isVisible()) {
			windowManager.frameOpened(this);
		}
		super.setVisible(value);
	}
}
