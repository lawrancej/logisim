/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.tools;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.border.Border;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.gui.main.Selection;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.base.Text;

/**
 * This {@link Action} allows for pop up labeling of components. This class was designed with
 * the intention of adding the feature of double click labeling to Logisim. 
 * @author Drew E. Buckley
 * 
 */
public class PopUpLabelAction extends Action {

	private static class LabelFrame extends JFrame {
		
		private class LabelProcedure implements Runnable {
			private final AttributeSet as;
			private final Project proj;
			private final Attribute<String> attrId;
		
			LabelProcedure(AttributeSet as, Project proj, Attribute<String> attrId) {
				this.as = as;
				this.proj = proj;
				this.attrId = attrId;
			}
		
			@Override
			public void run() {
				String label = null;
				try {
					label = promptForLabel();
				} 
				catch (InterruptedException e) {
					return;
				}
				if (label != null) {
					as.setValue(attrId, label);
					proj.repaintCanvas();
				}
			}
		}
		
		private class PopUpFocusListener implements WindowFocusListener {
			@Override
			public void windowLostFocus(WindowEvent e) {
				newLabel = textArea.getText();
				LabelFrame.this.dispose();
				setActive(false);
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
				// Empty
			}
		}

		private class LabelFrameKeyListener implements KeyListener 
		{
			private boolean specialBackspace = false;
			
			@Override
			public void keyTyped(KeyEvent e) {
				resize();
			}
		
			@Override
			public void keyReleased(KeyEvent e) {
				if(specialBackspace) {
					resize();
				}
			}
		
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK)
							== KeyEvent.CTRL_DOWN_MASK || 
							textArea.getSelectionStart() 
							- textArea.getSelectionEnd() != 0) {
						specialBackspace = true;
					}
				}
				else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					newLabel = textArea.getText();
					LabelFrame.this.dispose();
					setActive(false);
				}
			}
			
			
		}
		
		private static final long serialVersionUID = -8072223959722789844L;
		private static final int HEIGHT = 25;
		private static final int FONT_MARGIN = 25;
		private static final Border BORDER = BorderFactory.createLineBorder(Color.BLACK, 1);
		
		private final JTextField textArea;
		private final Attribute<String> attrId;

		private volatile String newLabel = null;
		private volatile boolean active = false;

		private final int x, y;
		private FontMetrics fm = null;
		
		LabelFrame(int x, int y, String label, Attribute<String> attrId) {
			setType(Type.UTILITY); 
			setUndecorated(true);
			addWindowFocusListener(new PopUpFocusListener());
			
			textArea = new JTextField(label);
			textArea.setVisible(true);
			textArea.setBorder(BORDER);
			textArea.setHorizontalAlignment(JTextField.CENTER);
			textArea.addKeyListener(new LabelFrameKeyListener());
			add(textArea);
			
			this.attrId = attrId;
			this.x = x;
			this.y = y;
		}

		void launchLabelProcedure(AttributeSet as, Project proj) {
			Thread thread = new Thread(new LabelProcedure(as, proj, attrId));
			thread.setDaemon(true);
			thread.setName("PopUpLabel-Procedure-Thread");
			thread.start();
		}

		String promptForLabel() throws InterruptedException {
			setActive(true);
			setVisible(true);
			textArea.setSelectionStart(0);
			textArea.setSelectionEnd(textArea.getText().length());
			resize();
			spinUntilInactive();
			return newLabel;
		}

		private void spinUntilInactive() throws InterruptedException {
			while (active) {
				Thread.sleep(100);
			}
		}

		private void setActive(boolean newActiveState) {
			active = newActiveState;
		}
		
		private void resize()
		{
			if( fm == null ) {
				this.fm = textArea.getGraphics().getFontMetrics(textArea.getFont());
			}
			int width = fm.stringWidth(textArea.getText()) + FONT_MARGIN;
			int newX = x - (width / 2);

			setLocation(newX, y - (HEIGHT / 2));
			setSize(width, HEIGHT);
			
			revalidate();
	        repaint();
		}
	}

	private final int x;
	private final int y;
	private final AttributeSet as;
	private final String formerLabel;
	private final Attribute<String> attrId;

	private PopUpLabelAction(int x, int y, AttributeSet as,
			String currentLabel, Attribute<String> attrId ) {
		this.x = x;
		this.y = y;
		this.as = as;
		this.formerLabel = currentLabel;
		this.attrId = attrId;
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void doIt(Project proj) {
		LabelFrame lf = new LabelFrame(x, y, formerLabel, attrId);
		lf.launchLabelProcedure(as, proj);
	}

	@Override
	public void undo(Project proj) {
		as.setValue(attrId, formerLabel);
	}

	/**
	 * Sets up action and adds to project if selection contains only 1 component and 
	 * selected component has label attribute. If the component does not possess a 
	 * label attribute, or selection is empty or contains multiple components, this 
	 * function does nothing.
	 * 
	 * @param proj
	 *            current {@link Project} object
	 * @param selection
	 * 			  selection to pull component from
	 * @param xAbs
	 *            absolute display x value
	 * @param yAbs
	 *            absolute display y value
	 * @throws IllegalArgumentException
	 * 			  if <code>proj</code> or <code>selection</code> are <code>null</code>
	 * @throws IllegalArgumentException
	 * 			  if <code>xAbs</code> or <code>yAbs</code> are negative
	 */
	public static void triggerLabel(Project proj, Selection selection, int xAbs,
			int yAbs) {
		if (proj == null || selection == null) {
			throw new IllegalArgumentException(
					"Function \"DoubleClickLabelAction.turnOnLabel\" does not accept "
				  + "null parameters.");
		}
		if (xAbs < 0 || yAbs < 0) {
			throw new IllegalArgumentException(
					"xAbs and yAbs must be greater than zero.");
		}
		
		Set<Component> components = selection.getComponents();
		Component component;
		if (components.size() != 1) {
			return;
		} else {
			component = components.iterator().next();
		}
		triggerLabel(proj, component, xAbs, yAbs);
	}
	
	/**
	 * Sets up action and adds to project if selected component has label attribute. 
	 * If the component does not possess a label attribute, this function does nothing.
	 * 
	 * @param proj
	 *            current {@link Project} object
	 * @param component
	 * 			  {@link Component} to change label of 
	 * @param xAbs
	 *            absolute display x value
	 * @param yAbs
	 *            absolute display y value
	 *            
	 * @throws IllegalArgumentException
	 * 			  if <code>proj</code> or <code>component</code> are <code>null</code>
	 * @throws IllegalArgumentException
	 * 			  if <code>xAbs</code> or <code>yAbs</code> are negative
	 */
	public static void triggerLabel(Project proj, Component component, int xAbs, int yAbs) {
		if(proj == null || component == null ) {
			throw new IllegalArgumentException(
					"Function \"DoubleClickLabelAction.turnOnLabel\" does not accept "
				  + "null parameters.");
		}
		if (xAbs < 0 || yAbs < 0) {
			throw new IllegalArgumentException(
					"xAbs and yAbs must be greater than zero.");
		}
		
		AttributeSet as = component.getAttributeSet();
		String currentLabel = as.getValue(StdAttr.LABEL);
		if (currentLabel != null) {
			proj.doAction(new PopUpLabelAction(xAbs, yAbs, as, currentLabel, StdAttr.LABEL));
		}
		else {
			currentLabel = as.getValue(Text.ATTR_TEXT);
			if (currentLabel != null) {
				proj.doAction(new PopUpLabelAction(xAbs, yAbs, as, currentLabel, Text.ATTR_TEXT));
			}
		}
	}
}
