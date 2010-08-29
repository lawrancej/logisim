/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.proj;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import com.cburch.logisim.Main;
import com.cburch.logisim.gui.start.Startup;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.PropertyChangeWeakSupport;

public class LogisimPreferences {
	public static final int TEMPLATE_UNKNOWN = -1;
	public static final int TEMPLATE_EMPTY = 0;
	public static final int TEMPLATE_PLAIN = 1;
	public static final int TEMPLATE_CUSTOM = 2;
	
	public static final String TEMPLATE = "template";
	public static final String TEMPLATE_TYPE = "templateType";
	public static final String TEMPLATE_FILE = "templateFile";
	public static final String ACCENTS_REPLACE = "accentsReplace"; 
	public static final String GATE_SHAPE = "gateShape";
	public static final String GRAPHICS_ACCELERATION = "graphicsAcceleration";
	public static final String STRETCH_WIRES = "stretchWires";
	public static final String AFTER_ADD = "afterAdd";
	public static final String SHOW_GHOSTS = "showGhosts";
	public static final String SHOW_PROJECT_TOOLBAR = "showProjectToolbar";
	public static final String LOCALE_OPTION = "locale";
	
	public static final String SHAPE_SHAPED = "shaped";
	public static final String SHAPE_RECTANGULAR = "rectangular";
	public static final String SHAPE_DIN40700 = "din40700";
	
	public static final String ACCEL_DEFAULT = "default";
	public static final String ACCEL_NONE = "none";
	public static final String ACCEL_OPENGL = "opengl";
	public static final String ACCEL_D3D = "d3d";
	
	public static final String AFTER_ADD_UNCHANGED = "unchanged";
	public static final String AFTER_ADD_EDIT = "edit";
	
	// class variables for holding individual preferences
	private static int templateType = TEMPLATE_PLAIN;
	private static File templateFile = null;
	private static PrefMonitorString locale;
	private static PrefMonitorBoolean accentsReplace;
	private static PrefMonitorStringOpts gateShape;
	private static PrefMonitorBoolean stretchWires;
	private static PrefMonitorStringOpts graphicsAccel;
	private static PrefMonitorStringOpts afterAdd;
	private static PrefMonitorBoolean showGhosts;
	private static PrefMonitorBoolean showProjectToolbar;
	
	// class variables for maintaining consistency between properties,
	// internal variables, and other classes
	private static Preferences prefs = null;
	private static MyListener myListener = null;
	private static PropertyChangeWeakSupport propertySupport
		= new PropertyChangeWeakSupport(LogisimPreferences.class);

	// class variables for holding the current template
	private static Template plainTemplate = null;
	private static Template emptyTemplate = null;
	private static Template customTemplate = null;
	private static File customTemplateFile = null;
	
	//
	// methods for accessing preferences
	//
	private static class MyListener implements PreferenceChangeListener {
		public void preferenceChange(PreferenceChangeEvent event) {
			Preferences prefs = event.getNode();
			String prop = event.getKey();
			if (prop.equals(LOCALE_OPTION)) {
				if (locale != null) {
					String chosen = locale.get();
					Locale found = findLocale(chosen);
					if (found != null) {
						LocaleManager.setLocale(found);
					}
				}
			} else if (prop.equals(ACCENTS_REPLACE)) {
				getPrefs();
				LocaleManager.setReplaceAccents(accentsReplace.get());
			} else if (prop.equals(TEMPLATE_TYPE)) {
				int oldValue = templateType;
				int value = prefs.getInt(TEMPLATE_TYPE, TEMPLATE_UNKNOWN);
				if (value != oldValue) {
					templateType = value;
					propertySupport.firePropertyChange(TEMPLATE, oldValue, value);
					propertySupport.firePropertyChange(TEMPLATE_TYPE, oldValue, value);
				}
			} else if (prop.equals(TEMPLATE_FILE)) {
				File oldValue = templateFile;
				File value = convertFile(prefs.get(TEMPLATE_FILE, null));
				if (value == null ? oldValue != null : !value.equals(oldValue)) {
					templateFile = value;
					if (templateType == TEMPLATE_CUSTOM) {
						customTemplate = null;
						propertySupport.firePropertyChange(TEMPLATE, oldValue, value);
					}
					propertySupport.firePropertyChange(TEMPLATE_FILE, oldValue, value);
				}
			}
		}
	}
	
	static Preferences getPrefs() {
		if (prefs == null) {
			synchronized(LogisimPreferences.class) {
				if (prefs == null) {
					Preferences p = Preferences.userNodeForPackage(Main.class);
					myListener = new MyListener();
					p.addPreferenceChangeListener(myListener);
					prefs = p;

					setTemplateFile(convertFile(p.get(TEMPLATE_FILE, null)));
					setTemplateType(p.getInt(TEMPLATE_TYPE, TEMPLATE_PLAIN));
					Locale loc = Locale.getDefault();
					String lang = loc == null ? "en" : loc.getLanguage();
					locale = new PrefMonitorString(LOCALE_OPTION, lang);
					accentsReplace = new PrefMonitorBoolean(ACCENTS_REPLACE, false);
					stretchWires = new PrefMonitorBoolean(STRETCH_WIRES, false);
					gateShape = new PrefMonitorStringOpts(GATE_SHAPE,
						    new String[] { SHAPE_SHAPED, SHAPE_RECTANGULAR, SHAPE_DIN40700 });
					graphicsAccel = new PrefMonitorStringOpts(GRAPHICS_ACCELERATION,
						    new String[] { ACCEL_DEFAULT, ACCEL_NONE, ACCEL_OPENGL, ACCEL_D3D });
					afterAdd = new PrefMonitorStringOpts(AFTER_ADD,
						    new String[] { AFTER_ADD_EDIT, AFTER_ADD_UNCHANGED });
					showGhosts = new PrefMonitorBoolean(SHOW_GHOSTS, true);
					showProjectToolbar = new PrefMonitorBoolean(SHOW_PROJECT_TOOLBAR, false);
					
					loc = findLocale(getLocale());
					if (loc != null) LocaleManager.setLocale(loc);
				}
			}
		}
		return prefs;
	}
	
	private static File convertFile(String fileName) {
		if (fileName == null || fileName.equals("")) {
			return null;
		} else {
			File file = new File(fileName);
			return file.canRead() ? file : null;
		}
	}
	
	//
	// PropertyChangeSource methods
	//
	public static void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}
	public static void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(propertyName, listener);
	}
	public static void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}
	public static void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
	}
	
	static void firePropertyChange(String property, boolean oldVal, boolean newVal) {
		propertySupport.firePropertyChange(property, oldVal, newVal);
	}
	static void firePropertyChange(String property, Object oldVal, Object newVal) {
		propertySupport.firePropertyChange(property, oldVal, newVal);
	}

	//
	// accessor methods
	//
	public static int getTemplateType() {
		getPrefs();
		int ret = templateType;
		if (ret == TEMPLATE_CUSTOM && templateFile == null) {
			ret = TEMPLATE_UNKNOWN;
		}
		return ret;
	}
	
	public static void setTemplateType(int value) {
		getPrefs();
		if (value != TEMPLATE_PLAIN && value != TEMPLATE_EMPTY && value != TEMPLATE_CUSTOM) {
			value = TEMPLATE_UNKNOWN;
		}
		if (value != TEMPLATE_UNKNOWN && templateType != value) {
			getPrefs().putInt(TEMPLATE_TYPE, value);
		}
	}
	
	public static File getTemplateFile() {
		getPrefs();
		return templateFile;
	}
	
	public static void setTemplateFile(File value) {
		getPrefs();
		setTemplateFile(value, null);
	}
	
	public static void setTemplateFile(File value, Template template) {
		getPrefs();
		if (value != null && !value.canRead()) value = null;
		if (value == null ? templateFile != null : !value.equals(templateFile)) {
			try {
				customTemplateFile = template == null ? null : value;
				customTemplate = template;
				getPrefs().put(TEMPLATE_FILE, value == null ? "" : value.getCanonicalPath());
			} catch (IOException ex) { }
		}
	}
	
	public static String getGraphicsAcceleration() {
		getPrefs();
		return graphicsAccel.get();
	}

	public static void setGraphicsAcceleration(String value) {
		getPrefs();
		graphicsAccel.set(value);
	}
	
	public static void handleGraphicsAcceleration() {
		String accel = getGraphicsAcceleration();
		try {
			if (accel == ACCEL_NONE) {
				System.setProperty("sun.java2d.opengl", "False");
				System.setProperty("sun.java2d.d3d", "False");
			} else if (accel == ACCEL_OPENGL) {
				System.setProperty("sun.java2d.opengl", "True");
				System.setProperty("sun.java2d.d3d", "False");
			} else if (accel == ACCEL_D3D) {
				System.setProperty("sun.java2d.opengl", "False");
				System.setProperty("sun.java2d.d3d", "True");
			}
		} catch (Throwable t) { }
	}
	
	public static boolean getStretchWires() {
		getPrefs();
		return stretchWires.get();
	}
	
	public static void setStretchWires(boolean value) {
		getPrefs();
		stretchWires.set(value);
	}
	
	public static String getLocale() {
		getPrefs();
		return locale.get();
	}
	
	public static void setLocale(String value) {
		if (findLocale(value) != null) {
			getPrefs();
			locale.set(value);
		}
	}
	
	private static Locale findLocale(String lang) {
		Locale[] check;
		for (int set = 0; set < 2; set++) {
			if (set == 0) check = new Locale[] { Locale.getDefault(), Locale.ENGLISH };
			else check = Locale.getAvailableLocales();
			for (int i = 0; i < check.length; i++) {
				Locale loc = check[i];
				if (loc != null && loc.getLanguage().equals(lang)) {
					return loc;
				}
			}
		}
		return null;
	}
	
	public static boolean getAccentsReplace() {
		getPrefs();
		return accentsReplace.get();
	}
	
	public static void setAccentsReplace(boolean value) {
		getPrefs();
		accentsReplace.set(value);
	}
	
	public static String getGateShape() {
		getPrefs();
		return gateShape.get();
	}
	
	public static void setGateShape(String value) {
		getPrefs();
		gateShape.set(value);
	}
	
	public static String getAfterAdd() {
		getPrefs();
		return afterAdd.get();
	}
	
	public static void setAfterAdd(String value) {
		getPrefs();
		afterAdd.set(value);
	}
	
	public static boolean getShowGhosts() {
		getPrefs();
		return showGhosts.get();
	}
	
	public static void setShowGhosts(boolean value) {
		getPrefs();
		showGhosts.set(value);
	}
	
	public static boolean getShowProjectToolbar() {
		getPrefs();
		return showProjectToolbar.get();
	}
	
	public static void setShowProjectToolbar(boolean value) {
		getPrefs();
		showProjectToolbar.set(value);
	}
	
	//
	// template methods
	//
	public static Template getTemplate() {
		getPrefs();
		switch (templateType) {
		case TEMPLATE_PLAIN: return getPlainTemplate();
		case TEMPLATE_EMPTY: return getEmptyTemplate();
		case TEMPLATE_CUSTOM: return getCustomTemplate();
		default: return getPlainTemplate();
		}
	}
	
	public static Template getEmptyTemplate() {
		if (emptyTemplate == null) emptyTemplate = Template.createEmpty();
		return emptyTemplate;
	}
	
	private static Template getPlainTemplate() {
		if (plainTemplate == null) {
			ClassLoader ld = Startup.class.getClassLoader();
			InputStream in = ld.getResourceAsStream("resources/logisim/default.templ");
			if (in == null) {
				plainTemplate = getEmptyTemplate(); 
			} else {
				try {
					try {
						plainTemplate = Template.create(in);
					} finally {
						in.close();
					}
				} catch (Throwable e) {
					plainTemplate = getEmptyTemplate();
				}
			}
		}
		return plainTemplate;
	}
	
	private static Template getCustomTemplate() {
		File toRead = templateFile;
		if (customTemplateFile == null || !(customTemplateFile.equals(toRead))) {
			if (toRead == null) {
				customTemplate = null;
				customTemplateFile = null;
			} else {
				FileInputStream reader = null;
				try {
					reader = new FileInputStream(toRead);
					customTemplate = Template.create(reader);
					customTemplateFile = templateFile;
				} catch (Throwable t) {
					setTemplateFile(null);
					customTemplate = null;
					customTemplateFile = null;
				} finally {
					if (reader != null) {
						try { reader.close(); } catch (IOException e) { }
					}
				}
			}
		}
		return customTemplate == null ? getPlainTemplate() : customTemplate;
	}
}
