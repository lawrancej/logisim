/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import static com.cburch.logisim.util.LocaleString.*;

public abstract class JDialogOk extends JDialog {
	private class MyListener extends WindowAdapter
			implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if (src == ok) {
				okClicked();
				dispose();
			} else if (src == cancel) {
				cancelClicked();
				dispose();
			}
		}

		@Override
		public void windowClosing(WindowEvent e) {
			JDialogOk.this.removeWindowListener(this);
			cancelClicked();
			dispose();
		}
	}

	private JPanel contents = new JPanel(new BorderLayout());
	protected JButton ok = new JButton(_("dlogOkButton"));
	protected JButton cancel = new JButton(_("dlogCancelButton"));

	public JDialogOk(Dialog parent, String title, boolean model) {
		super(parent, title, true);
		configure();
	}

	public JDialogOk(Frame parent, String title, boolean model) {
		super(parent, title, true);
		configure();
	}

	private void configure() {
		MyListener listener = new MyListener();
		this.addWindowListener(listener);
		ok.addActionListener(listener);
		cancel.addActionListener(listener);

		Box buttons = Box.createHorizontalBox();
		buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttons.add(Box.createHorizontalGlue());
		buttons.add(ok);
		buttons.add(Box.createHorizontalStrut(10));
		buttons.add(cancel);
		buttons.add(Box.createHorizontalGlue());

		Container pane = super.getContentPane();
		pane.add(contents, BorderLayout.CENTER);
		pane.add(buttons, BorderLayout.SOUTH);
	}

	@Override
	public Container getContentPane() { return contents; }

	public abstract void okClicked();

	public void cancelClicked() { }

}
