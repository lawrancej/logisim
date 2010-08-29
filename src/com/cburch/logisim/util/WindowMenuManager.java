/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.util.ArrayList;
import java.util.List;

class WindowMenuManager {
	private WindowMenuManager() { }
	
	private static ArrayList<WindowMenu> menus = new ArrayList<WindowMenu>();
	private static ArrayList<WindowMenuItemManager> managers
		= new ArrayList<WindowMenuItemManager>();
	private static WindowMenuItemManager currentManager = null;
	
	public static void addMenu(WindowMenu menu) {
		for (WindowMenuItemManager manager : managers) {
			manager.createMenuItem(menu);
		}
		menus.add(menu);
	}
	
	// TODO frames should call removeMenu when they're destroyed
	
	public static void addManager(WindowMenuItemManager manager) {
		for (WindowMenu menu : menus) {
			manager.createMenuItem(menu);
		}
		managers.add(manager);
	}
	
	public static void removeManager(WindowMenuItemManager manager) {
		for (WindowMenu menu : menus) {
			manager.removeMenuItem(menu);
		}
		managers.remove(manager);
	}
	
	static List<WindowMenu> getMenus() {
		return menus;
	}
	
	static WindowMenuItemManager getCurrentManager() {
		return currentManager;
	}
	
	static void setCurrentManager(WindowMenuItemManager value) {
		if (value == currentManager) return;
		
		boolean doEnable = (currentManager == null) != (value == null);
		if (currentManager == null) setNullItems(false); else currentManager.setSelected(false);
		currentManager = value;
		if (currentManager == null) setNullItems(true); else currentManager.setSelected(true);
		if (doEnable) enableAll();
	}
	
	static void unsetCurrentManager(WindowMenuItemManager value) {
		if (value != currentManager) return;
		setCurrentManager(null);
	}
	
	private static void setNullItems(boolean value) {
		for (WindowMenu menu : menus) {
			menu.setNullItemSelected(value);
		}
	}
	
	private static void enableAll() {
		for (WindowMenu menu : menus) {
			menu.computeEnabled();
		}
	}
}
