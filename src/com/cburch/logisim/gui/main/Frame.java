/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitEvent;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectActions;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import com.cburch.logisim.tools.SetAttributeAction;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.HorizontalSplitPane;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.MacCompatibility;
import com.cburch.logisim.util.StringUtil;
import com.cburch.logisim.util.VerticalSplitPane;

public class Frame extends JFrame
		implements LocaleListener {
	class MyProjectListener
			implements ProjectListener, LibraryListener, CircuitListener,
				AttributeListener {
		public void projectChanged(ProjectEvent event) {
			int action = event.getAction();

			if (action == ProjectEvent.ACTION_COMPLETE
					|| action == ProjectEvent.UNDO_COMPLETE
					|| action == ProjectEvent.ACTION_SET_FILE) {
				enableSave();
			}

			if (action == ProjectEvent.ACTION_SET_FILE) {
				computeTitle();
				proj.setTool(proj.getOptions().getToolbarData().getFirstTool());
				
				AttributeSet attrs = proj.getOptions().getAttributeSet();
				attrs.addAttributeListener(this);
				placeToolbar(attrs.getValue(Options.ATTR_TOOLBAR_LOC));
			} else if (action == ProjectEvent.ACTION_SET_CURRENT) {
				viewAttributes(proj.getTool());
				computeTitle();
			} else if (action == ProjectEvent.ACTION_SET_TOOL) {
				if (attrTable == null) return; // for startup
				viewAttributes((Tool) event.getOldData(), (Tool) event.getData());
			}
		}

		public void libraryChanged(LibraryEvent e) {
			if (e.getAction() == LibraryEvent.SET_NAME) {
				computeTitle();
			}
		}

		public void circuitChanged(CircuitEvent event) {
			if (event.getAction() == CircuitEvent.ACTION_SET_NAME) {
				computeTitle();
			}
		}

		private void enableSave() {
			Project proj = getProject();
			boolean ok = proj.isFileDirty();
			getRootPane().putClientProperty("windowModified", Boolean.valueOf(ok));
		}

		public void attributeListChanged(AttributeEvent e) { }

		public void attributeValueChanged(AttributeEvent e) {
			if (e.getAttribute() == Options.ATTR_TOOLBAR_LOC) {
				placeToolbar(e.getValue());
			}
		}
	}

	class MyWindowListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			if (confirmClose(Strings.get("confirmCloseTitle"))) {
				canvas.closeCanvas();
				Frame.this.dispose();
			}
		}

		@Override
		public void windowOpened(WindowEvent e) {
			canvas.computeSize();
		}
	}

	private static class ComponentAttributeListener
			implements AttributeTableListener {
		Project proj;
		Circuit circ;
		Component comp;

		ComponentAttributeListener(Project proj, Circuit circ,
				Component comp) {
			this.proj = proj;
			this.circ = circ;
			this.comp = comp;
		}

		public void valueChangeRequested(AttributeTable table,
				AttributeSet attrs, Attribute<?> attr, Object value) {
			if (!proj.getLogisimFile().contains(circ)) {
				JOptionPane.showMessageDialog(proj.getFrame(),
					Strings.get("cannotModifyCircuitError"));
			} else {
				SetAttributeAction act = new SetAttributeAction(circ,
						Strings.getter("changeAttributeAction"));
				act.set(comp, attr, value);
				proj.doAction(act);
			}
		}
	}
	
	private Project         proj;

	private LogisimMenuBar  menubar;
	private MenuListener    menuListener;
	private Toolbar         toolbar;
	private Canvas          canvas;
	private JPanel          canvasPanel;
	private ProjectToolbar  projectToolbar;
	private Explorer        explorer;
	private AttributeTable  attrTable;
	private ZoomControl     zoom;
	private MyProjectListener myProjectListener = new MyProjectListener();

	public Frame(Project proj) {
		this.proj = proj;
		proj.setFrame(this);

		setBackground(Color.white);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new MyWindowListener());

		proj.addProjectListener(myProjectListener);
		proj.addLibraryListener(myProjectListener);
		proj.addCircuitListener(myProjectListener);
		proj.getOptions().getAttributeSet().addAttributeListener(myProjectListener);
		computeTitle();
		
		projectToolbar = new ProjectToolbar();

		// set up menu bar
		menubar = new LogisimMenuBar(this, proj);
		setJMenuBar(menubar);
		menuListener = new MenuListener(this, menubar, projectToolbar);
		menuListener.register();

		// set up the content-bearing components
		toolbar = new Toolbar(proj);
		explorer = new Explorer(proj);
		explorer.setListener(new ExplorerManip(proj, explorer));
		canvasPanel = new JPanel(new BorderLayout());
		canvas = new Canvas(proj);
		zoom = new ZoomControl(canvas);
		attrTable = new AttributeTable(this);

		// set up the contents, split down the middle, with the canvas
		// on the right and a split pane on the left containing the
		// explorer and attribute values.
		JScrollPane canvasPane = new JScrollPane(canvas);
		if (MacCompatibility.mrjVersion >= 0.0) {
			canvasPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			canvasPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		}
		canvas.setScrollPane(canvasPane);
		canvasPanel.add(canvasPane, BorderLayout.CENTER);
		
		JPanel explPanel = new JPanel(new BorderLayout());
		explPanel.add(projectToolbar, BorderLayout.NORTH);
		explPanel.add(new JScrollPane(explorer), BorderLayout.CENTER);
		JPanel attrPanel = new JPanel(new BorderLayout());
		attrPanel.add(new JScrollPane(attrTable), BorderLayout.CENTER);
		attrPanel.add(zoom, BorderLayout.SOUTH);

		VerticalSplitPane contents = new VerticalSplitPane(
			new HorizontalSplitPane(explPanel, attrPanel, 0.5),
			canvasPanel, 0.25);

		placeToolbar(proj.getOptions().getAttributeSet().getValue(Options.ATTR_TOOLBAR_LOC));
		getContentPane().add(contents, BorderLayout.CENTER);

		computeTitle();

		this.setSize(640, 480);
		toolbar.registerShortcuts(canvas);
		toolbar.registerShortcuts(toolbar);
		toolbar.registerShortcuts(explorer);
		toolbar.registerShortcuts(attrTable);

		if (proj.getTool() == null) {
			proj.setTool(proj.getOptions().getToolbarData().getFirstTool());
		}
		LocaleManager.addLocaleListener(this);
	}
	
	private void placeToolbar(Object loc) {
		
		Container contents = getContentPane();
		contents.remove(toolbar);
		canvasPanel.remove(toolbar);
		if (loc == Options.TOOLBAR_HIDDEN) {
			; // don't place value anywhere
		} else if (loc == Options.TOOLBAR_DOWN_MIDDLE) {
			toolbar.setOrientation(Toolbar.VERTICAL);
			canvasPanel.add(toolbar, BorderLayout.WEST);
		} else { // it is a BorderLayout constant
			Object value;
			if (loc == Direction.EAST)       value = BorderLayout.EAST;
			else if (loc == Direction.SOUTH) value = BorderLayout.SOUTH;
			else if (loc == Direction.WEST)  value = BorderLayout.WEST;
			else                            value = BorderLayout.NORTH;

			contents.add(toolbar, value);
			boolean vertical = value == BorderLayout.WEST || value == BorderLayout.EAST;
			toolbar.setOrientation(vertical ? Toolbar.VERTICAL : Toolbar.HORIZONTAL);
		}
		contents.validate();
	}
	
	public Project getProject() {
		return proj;
	}

	public void viewComponentAttributes(Circuit circ, Component comp) {
		if (comp == null) {
			attrTable.setAttributeSet(null, null);
			canvas.setHaloedComponent(null, null);
		} else {
			attrTable.setAttributeSet(comp.getAttributeSet(),
				new ComponentAttributeListener(proj, circ, comp));
			canvas.setHaloedComponent(circ, comp);
		}
		toolbar.setHaloedTool(null);
		explorer.setHaloedTool(null);
	}

	boolean getShowHalo() {
		return canvas.getShowHalo();
	}

	public AttributeTable getAttributeTable() {
		return attrTable;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	private void computeTitle() {
		String s;
		Circuit circuit = proj.getCurrentCircuit();
		String name = proj.getLogisimFile().getName();
		if (circuit != null) {
			s = StringUtil.format(Strings.get("titleCircFileKnown"),
				circuit.getName(), name);
		} else {
			s = StringUtil.format(Strings.get("titleFileKnown"), name);
		}
		this.setTitle(s);
	}
	
	void viewAttributes(Tool newTool) {
		viewAttributes(null, newTool);
	}

	private void viewAttributes(Tool oldTool, Tool newTool) {
		if (newTool == null) return;
		AttributeSet newAttrs = newTool.getAttributeSet();
		if (newAttrs == null) {
			AttributeSet oldAttrs = oldTool == null ? null : oldTool.getAttributeSet();
			AttributeTableListener listen = attrTable.getAttributeTableListener();
			if (attrTable.getAttributeSet() != oldAttrs
					&& !(listen instanceof CircuitAttributeListener)) {
				return;
			}
		}
		if (newAttrs == null) {
			Circuit circ = proj.getCurrentCircuit();
			if (circ != null) {
				attrTable.setAttributeSet(circ.getStaticAttributes(),
						new CircuitAttributeListener(proj, circ));
			}
		} else {
			attrTable.setAttributeSet(newAttrs, newTool.getAttributeTableListener(proj));
		}
		if (newAttrs != null && newAttrs.getAttributes().size() > 0) {
			toolbar.setHaloedTool(newTool);
			explorer.setHaloedTool(newTool);
		} else {
			toolbar.setHaloedTool(null);
			explorer.setHaloedTool(null);
		}
		canvas.setHaloedComponent(null, null);
	}

	public void localeChanged() {
		computeTitle();
	}
	
	public boolean confirmClose() {
		return confirmClose(Strings.get("confirmCloseTitle"));
	}
	
	// returns true if user is OK with proceeding
	public boolean confirmClose(String title) {
		String message = StringUtil.format(Strings.get("confirmDiscardMessage"),
				proj.getLogisimFile().getName());
		
		if (!proj.isFileDirty()) return true;
		toFront();
		String[] options = { Strings.get("saveOption"), Strings.get("discardOption"), Strings.get("cancelOption") };
		int result = JOptionPane.showOptionDialog(this,
				message, title, 0, JOptionPane.QUESTION_MESSAGE, null,
				options, options[0]);
		boolean ret;
		if (result == 0) {
			ret = ProjectActions.doSave(proj);
		} else if (result == 1) {
			ret = true;
		} else {
			ret = false;
		}
		if (ret) {
			dispose();
		}
		return ret;
	}
}
