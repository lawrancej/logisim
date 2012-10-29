/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.cburch.draw.toolbar.Toolbar;
import com.cburch.draw.toolbar.ToolbarModel;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitEvent;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.gui.appear.AppearanceView;
import com.cburch.logisim.gui.generic.AttrTable;
import com.cburch.logisim.gui.generic.AttrTableModel;
import com.cburch.logisim.gui.generic.BasicZoomModel;
import com.cburch.logisim.gui.generic.CanvasPane;
import com.cburch.logisim.gui.generic.CardPanel;
import com.cburch.logisim.gui.generic.LFrame;
import com.cburch.logisim.gui.generic.ZoomControl;
import com.cburch.logisim.gui.generic.ZoomModel;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectActions;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import com.cburch.logisim.proj.Projects;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.HorizontalSplitPane;
import com.cburch.logisim.util.JFileChoosers;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.VerticalSplitPane;
import static com.cburch.logisim.util.LocaleString.*;

public class Frame extends LFrame implements LocaleListener {
	public static final String EDITOR_VIEW = "editorView";
	public static final String EXPLORER_VIEW = "explorerView";
	public static final String EDIT_LAYOUT = "layout";
	public static final String EDIT_APPEARANCE = "appearance";
	public static final String VIEW_TOOLBOX = "toolbox";
	public static final String VIEW_SIMULATION = "simulation";

	private static final double[] ZOOM_OPTIONS = { 20, 50, 75, 100, 133, 150, 200, 250, 300, 400 };
	
	class MyProjectListener
			implements ProjectListener, LibraryListener, CircuitListener,
				PropertyChangeListener, ChangeListener {
		public void projectChanged(ProjectEvent event) {
			int action = event.getAction();

			if (action == ProjectEvent.ACTION_SET_FILE) {
				computeTitle();
				proj.setTool(proj.getOptions().getToolbarData().getFirstTool());
				placeToolbar();
			} else if (action == ProjectEvent.ACTION_SET_CURRENT) {
				setEditorView(EDIT_LAYOUT);
				if (appearance != null) {
					appearance.setCircuit(proj, proj.getCircuitState());
				}
				viewAttributes(proj.getTool());
				computeTitle();
			} else if (action == ProjectEvent.ACTION_SET_TOOL) {
				if (attrTable == null) return; // for startup
				Tool oldTool = (Tool) event.getOldData();
				Tool newTool = (Tool) event.getData();
				if (getEditorView().equals(EDIT_LAYOUT)) {
					viewAttributes(oldTool, newTool, false);
				}
			}
		}

		public void libraryChanged(LibraryEvent e) {
			if (e.getAction() == LibraryEvent.SET_NAME) {
				computeTitle();
			} else if (e.getAction() == LibraryEvent.DIRTY_STATE) {
				enableSave();
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

		public void propertyChange(PropertyChangeEvent event) {
			if (AppPreferences.TOOLBAR_PLACEMENT.isSource(event)) {
				placeToolbar();
			}
		}
		
		public void stateChanged(ChangeEvent event) {
			Object source = event.getSource();
			if (source == explorerPane) {
				firePropertyChange(EXPLORER_VIEW, "???", getExplorerView());
			} else if (source == mainPanel) {
				firePropertyChange(EDITOR_VIEW, "???", getEditorView());
			}
		}
	}

	class MyWindowListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			if (confirmClose(_("confirmCloseTitle"))) {
				layoutCanvas.closeCanvas();
				Frame.this.dispose();
			}
		}

		@Override
		public void windowOpened(WindowEvent e) {
			layoutCanvas.computeSize(true);
		}
	}

	private Project         proj;
	private MyProjectListener myProjectListener = new MyProjectListener();

	// GUI elements shared between views
	private LogisimMenuBar  menubar;
	private MenuListener    menuListener;
	private Toolbar         toolbar;
	private HorizontalSplitPane leftRegion;
	private VerticalSplitPane mainRegion;
	private JPanel          mainPanelSuper;
	private CardPanel       mainPanel;
	// left-side elements
	private Toolbar         projectToolbar;
	private CardPanel       explorerPane;
	private Toolbox         toolbox;
	private SimulationExplorer simExplorer;
	private AttrTable       attrTable;
	private ZoomControl     zoom;
	
	// for the Layout view
	private LayoutToolbarModel layoutToolbarModel;
	private Canvas          layoutCanvas;
	private ZoomModel       layoutZoomModel;
	private LayoutEditHandler layoutEditHandler;
	private AttrTableSelectionModel attrTableSelectionModel;
	
	// for the Appearance view
	private AppearanceView appearance;

	public Frame(Project proj) {
		this.proj = proj;

		setBackground(Color.white);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new MyWindowListener());

		proj.addProjectListener(myProjectListener);
		proj.addLibraryListener(myProjectListener);
		proj.addCircuitListener(myProjectListener);
		computeTitle();
		
		// set up elements for the Layout view
		layoutToolbarModel = new LayoutToolbarModel(this, proj);
		layoutCanvas = new Canvas(proj);
		layoutZoomModel = new BasicZoomModel(AppPreferences.LAYOUT_SHOW_GRID,
				AppPreferences.LAYOUT_ZOOM, ZOOM_OPTIONS);

		layoutCanvas.getGridPainter().setZoomModel(layoutZoomModel);
		layoutEditHandler = new LayoutEditHandler(this);
		attrTableSelectionModel = new AttrTableSelectionModel(proj, this);

		// set up menu bar and toolbar
		menubar = new LogisimMenuBar(this, proj);
		menuListener = new MenuListener(this, menubar);
		menuListener.setEditHandler(layoutEditHandler);
		setJMenuBar(menubar);
		toolbar = new Toolbar(layoutToolbarModel);

		// set up the left-side components
		ToolbarModel projectToolbarModel = new ExplorerToolbarModel(this, menuListener);
		projectToolbar = new Toolbar(projectToolbarModel);
		toolbox = new Toolbox(proj, menuListener);
		simExplorer = new SimulationExplorer(proj, menuListener);
		explorerPane = new CardPanel();
		explorerPane.addView(VIEW_TOOLBOX, toolbox);
		explorerPane.addView(VIEW_SIMULATION, simExplorer);
		explorerPane.setView(VIEW_TOOLBOX);
		attrTable = new AttrTable(this);
		zoom = new ZoomControl(layoutZoomModel);

		// set up the central area
		CanvasPane canvasPane = new CanvasPane(layoutCanvas);
		mainPanelSuper = new JPanel(new BorderLayout());
		canvasPane.setZoomModel(layoutZoomModel);
		mainPanel = new CardPanel();
		mainPanel.addView(EDIT_LAYOUT, canvasPane);
		mainPanel.setView(EDIT_LAYOUT);
		mainPanelSuper.add(mainPanel, BorderLayout.CENTER);

		// set up the contents, split down the middle, with the canvas
		// on the right and a split pane on the left containing the
		// explorer and attribute values.
		JPanel explPanel = new JPanel(new BorderLayout());
		explPanel.add(projectToolbar, BorderLayout.NORTH);
		explPanel.add(explorerPane, BorderLayout.CENTER);
		JPanel attrPanel = new JPanel(new BorderLayout());
		attrPanel.add(attrTable, BorderLayout.CENTER);
		attrPanel.add(zoom, BorderLayout.SOUTH);

		leftRegion = new HorizontalSplitPane(explPanel, attrPanel,
				AppPreferences.WINDOW_LEFT_SPLIT.get().doubleValue());
		mainRegion = new VerticalSplitPane(leftRegion, mainPanelSuper,
				AppPreferences.WINDOW_MAIN_SPLIT.get().doubleValue());

		getContentPane().add(mainRegion, BorderLayout.CENTER);

		computeTitle();

		this.setSize(AppPreferences.WINDOW_WIDTH.get().intValue(),
				AppPreferences.WINDOW_HEIGHT.get().intValue());
		Point prefPoint = getInitialLocation();
		if (prefPoint != null) {
			this.setLocation(prefPoint);
		}
		this.setExtendedState(AppPreferences.WINDOW_STATE.get().intValue());
		
		menuListener.register(mainPanel);
		KeyboardToolSelection.register(toolbar);

		proj.setFrame(this);
		if (proj.getTool() == null) {
			proj.setTool(proj.getOptions().getToolbarData().getFirstTool());
		}
		mainPanel.addChangeListener(myProjectListener);
		explorerPane.addChangeListener(myProjectListener);
		AppPreferences.TOOLBAR_PLACEMENT.addPropertyChangeListener(myProjectListener);
		placeToolbar();
		((MenuListener.EnabledListener) projectToolbarModel).menuEnableChanged(menuListener);

		LocaleManager.addLocaleListener(this);
	}
	
	private void placeToolbar() {
		String loc = AppPreferences.TOOLBAR_PLACEMENT.get();
		Container contents = getContentPane();
		contents.remove(toolbar);
		mainPanelSuper.remove(toolbar);
		if (AppPreferences.TOOLBAR_HIDDEN.equals(loc)) {
			; // don't place value anywhere
		} else if (AppPreferences.TOOLBAR_DOWN_MIDDLE.equals(loc)) {
			toolbar.setOrientation(Toolbar.VERTICAL);
			mainPanelSuper.add(toolbar, BorderLayout.WEST);
		} else { // it is a BorderLayout constant
			Object value = BorderLayout.NORTH;
			for (Direction dir : Direction.cardinals) {
				if (dir.toString().equals(loc)) {
					if (dir == Direction.EAST)       value = BorderLayout.EAST;
					else if (dir == Direction.SOUTH) value = BorderLayout.SOUTH;
					else if (dir == Direction.WEST)  value = BorderLayout.WEST;
					else                             value = BorderLayout.NORTH;
				}
			}

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
			setAttrTableModel(null);
		} else {
			setAttrTableModel(new AttrTableComponentModel(proj, circ, comp));
		}
	}
	
	void setAttrTableModel(AttrTableModel value) {
		attrTable.setAttrTableModel(value);
		if (value instanceof AttrTableToolModel) {
			Tool tool = ((AttrTableToolModel) value).getTool();
			toolbox.setHaloedTool(tool);
			layoutToolbarModel.setHaloedTool(tool);
		} else {
			toolbox.setHaloedTool(null);
			layoutToolbarModel.setHaloedTool(null);
		}
		if (value instanceof AttrTableComponentModel) {
			Circuit circ = ((AttrTableComponentModel) value).getCircuit();
			Component comp = ((AttrTableComponentModel) value).getComponent();
			layoutCanvas.setHaloedComponent(circ, comp);
		} else {
			layoutCanvas.setHaloedComponent(null, null);
		}
	}
	
	public void setExplorerView(String view) {
		explorerPane.setView(view);
	}
	
	public String getExplorerView() {
		return explorerPane.getView();
	}
	
	public void setEditorView(String view) {
		String curView = mainPanel.getView();
		if (curView.equals(view)) return;
		
		if (view.equals(EDIT_APPEARANCE)) { // appearance view
			AppearanceView app = appearance;
			if (app == null) {
				app = new AppearanceView();
				app.setCircuit(proj, proj.getCircuitState());
				mainPanel.addView(EDIT_APPEARANCE, app.getCanvasPane());
				appearance = app;
			}
			toolbar.setToolbarModel(app.getToolbarModel());
			app.getAttrTableDrawManager(attrTable).attributesSelected();
			zoom.setZoomModel(app.getZoomModel());
			menuListener.setEditHandler(app.getEditHandler());
			mainPanel.setView(view);
			app.getCanvas().requestFocus();
		} else { // layout view
			toolbar.setToolbarModel(layoutToolbarModel);
			zoom.setZoomModel(layoutZoomModel);
			menuListener.setEditHandler(layoutEditHandler);
			viewAttributes(proj.getTool(), true);
			mainPanel.setView(view);
			layoutCanvas.requestFocus();
		}
	}

	public String getEditorView() {
		return mainPanel.getView();
	}

	public Canvas getCanvas() {
		return layoutCanvas;
	}

	private void computeTitle() {
		String s;
		Circuit circuit = proj.getCurrentCircuit();
		String name = proj.getLogisimFile().getName();
		if (circuit != null) {
			s = _("titleCircFileKnown", circuit.getName(), name);
		} else {
			s = _("titleFileKnown", name);
		}
		this.setTitle(s);
		myProjectListener.enableSave();
	}
	
	void viewAttributes(Tool newTool) {
		viewAttributes(null, newTool, false);
	}
	
	private void viewAttributes(Tool newTool, boolean force) {
		viewAttributes(null, newTool, force);
	}

	private void viewAttributes(Tool oldTool, Tool newTool, boolean force) {
		AttributeSet newAttrs;
		if (newTool == null) {
			newAttrs = null;
			if (!force) return;
		} else {
			newAttrs = newTool.getAttributeSet(layoutCanvas);
		}
		if (newAttrs == null) {
			AttrTableModel oldModel = attrTable.getAttrTableModel();
			boolean same = oldModel instanceof AttrTableToolModel
				&& ((AttrTableToolModel) oldModel).getTool() == oldTool;
			if (!force && !same && !(oldModel instanceof AttrTableCircuitModel)) {
				return;
			}
		}
		if (newAttrs == null) {
			Circuit circ = proj.getCurrentCircuit();
			if (circ != null) {
				setAttrTableModel(new AttrTableCircuitModel(proj, circ));
			} else if (force) {
				setAttrTableModel(null);
			}
		} else if (newAttrs instanceof SelectionAttributes) {
			setAttrTableModel(attrTableSelectionModel);
		} else {
			setAttrTableModel(new AttrTableToolModel(proj, newTool));
		}
	}

	public void localeChanged() {
		computeTitle();
	}
	
	public void savePreferences() {
		AppPreferences.TICK_FREQUENCY.set(Double.valueOf(proj.getSimulator().getTickFrequency()));
		AppPreferences.LAYOUT_SHOW_GRID.setBoolean(layoutZoomModel.getShowGrid());
		AppPreferences.LAYOUT_ZOOM.set(Double.valueOf(layoutZoomModel.getZoomFactor()));
		if (appearance != null) {
			ZoomModel aZoom = appearance.getZoomModel();
			AppPreferences.APPEARANCE_SHOW_GRID.setBoolean(aZoom.getShowGrid());
			AppPreferences.APPEARANCE_ZOOM.set(Double.valueOf(aZoom.getZoomFactor()));
		}
		int state = getExtendedState() & ~JFrame.ICONIFIED;
		AppPreferences.WINDOW_STATE.set(Integer.valueOf(state));
		Dimension dim = getSize();
		AppPreferences.WINDOW_WIDTH.set(Integer.valueOf(dim.width));
		AppPreferences.WINDOW_HEIGHT.set(Integer.valueOf(dim.height));
		Point loc;
		try {
			loc = getLocationOnScreen();
		} catch (IllegalComponentStateException e) {
			loc = Projects.getLocation(this);
		}
		if (loc != null) {
			AppPreferences.WINDOW_LOCATION.set(loc.x + "," + loc.y);
		}
		AppPreferences.WINDOW_LEFT_SPLIT.set(Double.valueOf(leftRegion.getFraction()));
		AppPreferences.WINDOW_MAIN_SPLIT.set(Double.valueOf(mainRegion.getFraction()));
		AppPreferences.DIALOG_DIRECTORY.set(JFileChoosers.getCurrentDirectory());
	}
	
	public boolean confirmClose() {
		return confirmClose(_("confirmCloseTitle"));
	}
	
	// returns true if user is OK with proceeding
	public boolean confirmClose(String title) {
		String message = _("confirmDiscardMessage", proj.getLogisimFile().getName());
		
		if (!proj.isFileDirty()) return true;
		toFront();
		String[] options = { _("saveOption"), _("discardOption"), _("cancelOption") };
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
	
	private static Point getInitialLocation() {
		String s = AppPreferences.WINDOW_LOCATION.get();
		if (s == null) return null;
		int comma = s.indexOf(',');
		if (comma < 0) return null;
		try {
			int x = Integer.parseInt(s.substring(0, comma));
			int y = Integer.parseInt(s.substring(comma + 1));
			while (isProjectFrameAt(x, y)) {
				x += 20;
				y += 20;
			}
			Rectangle desired = new Rectangle(x, y, 50, 50);
		
			int gcBestSize = 0;
			Point gcBestPoint = null;
			GraphicsEnvironment ge;
			ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			for (GraphicsDevice gd : ge.getScreenDevices()) {
				for (GraphicsConfiguration gc : gd.getConfigurations()) {
					Rectangle gcBounds = gc.getBounds();
					if (gcBounds.intersects(desired)) {
						Rectangle inter = gcBounds.intersection(desired);
						int size = inter.width * inter.height;
						if (size > gcBestSize) {
							gcBestSize = size;
							int x2 = Math.max(gcBounds.x, Math.min(inter.x,
									inter.x + inter.width - 50));
							int y2 = Math.max(gcBounds.y, Math.min(inter.y,
									inter.y + inter.height - 50));
							gcBestPoint = new Point(x2, y2);
						}
					}
				}
			}
			if (gcBestPoint != null) {
				if (isProjectFrameAt(gcBestPoint.x, gcBestPoint.y)) {
					gcBestPoint = null;
				}
			}
			return gcBestPoint;
		} catch (Throwable t) {
			return null;
		}
	}

	private static boolean isProjectFrameAt(int x, int y) {
		for (Project current : Projects.getOpenProjects()) {
			Frame frame = current.getFrame();
			if (frame != null) {
				Point loc = frame.getLocationOnScreen();
				int d = Math.abs(loc.x - x) + Math.abs(loc.y - y);
				if (d <= 3) return true;
			}
		}
		return false;
	}
}
