/*
 * A font chooser JavaBean component.
 * Copyright (C) 2009 Dr Christos Bohoris
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3 as published by the Free Software Foundation;
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * swing@connectina.com
 */
package com.connectina.swing.fontchooser;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 * A generic implementation of <code>FontSelectionModel</code>.
 *
 * @author Christos Bohoris
 * @see java.awt.Font
 */
public class DefaultFontSelectionModel implements FontSelectionModel {

    /**
     * Only one <code>ChangeEvent</code> is needed per model instance
     * since the event's only (read-only) state is the source property.
     * The source of events generated here is always "this".
     */
    protected transient ChangeEvent changeEvent = null;

    /**
     * A list of registered event listeners.
     */
    protected EventListenerList listenerList = new EventListenerList();

    private Font selectedFont;

    private List availableFontNames = new ArrayList();

    /**
     * Creates a <code>DefaultFontSelectionModel</code> with the
     * current font set to <code>new Font(Font.SANS_SERIF, Font.PLAIN, 12)
     * </code>. This is the default constructor.
     */
    public DefaultFontSelectionModel() {
        this(new Font(JFontChooser.SANS_SERIF, Font.PLAIN, 12));
    }

    /**
     * Creates a <code>DefaultFontSelectionModel</code> with the
     * current font set to <code>font</code>, which should be
     * non-<code>null</code>. Note that setting the font to
     * <code>null</code> is undefined and may have unpredictable
     * results.
     *
     * @param font the new <code>Font</code>
     */
    public DefaultFontSelectionModel(Font font) {
        selectedFont = font;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] families = ge.getAvailableFontFamilyNames();
        for(int i = 0; i < families.length; i++) {
        	availableFontNames.add(families[i]);
        }
    }

    /**
     * Returns the selected <code>Font</code> which should be
     * non-<code>null</code>.
     *
     * @return the selected <code>Font</code>
     */
    public Font getSelectedFont() {
        return selectedFont;
    }

    /**
     * Sets the selected font to <code>font</code>.
     * Note that setting the font to <code>null</code>
     * is undefined and may have unpredictable results.
     * This method fires a state changed event if it sets the
     * current font to a new non-<code>null</code> font;
     * if the new font is the same as the current font,
     * no event is fired.
     *
     * @param font the new <code>Font</code>
     */
    public void setSelectedFont(Font font) {
        if (font != null && !selectedFont.equals(font)) {
            selectedFont = font;
            fireStateChanged();
        }
    }

    /**
     * Gets the available font names.
     * Returns a list containing the names of all font families in this
     * <code>GraphicsEnvironment</code> localized for the default locale,
     * as returned by <code>Locale.getDefault()</code>.
     *
     * @return a list of String containing font family names localized for the
     *         default locale, or a suitable alternative name if no name exists
     *         for this locale
     */
    public List getAvailableFontNames() {
        return availableFontNames;
    }

    /**
     * Adds a <code>ChangeListener</code> to the model.
     *
     * @param l the <code>ChangeListener</code> to be added
     */
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    /**
     * Removes a <code>ChangeListener</code> from the model.
     * @param l the <code>ChangeListener</code> to be removed
     */
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    /**
     * Returns an array of all the <code>ChangeListener</code>s added
     * to this <code>DefaultFontSelectionModel</code> with
     * <code>addChangeListener</code>.
     *
     * @return all of the <code>ChangeListener</code>s added, or an empty
     *         array if no listeners have been added
     */
    public ChangeListener[] getChangeListeners() {
        return (ChangeListener[]) listenerList.getListeners(
                ChangeListener.class);
    }

    /**
     * Runs each <code>ChangeListener</code>'s
     * <code>stateChanged</code> method.
     */
    protected void fireStateChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }
    
}
