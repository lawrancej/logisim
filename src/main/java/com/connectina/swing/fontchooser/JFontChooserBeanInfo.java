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

import java.awt.Image;
import java.beans.*;

/**
 * The bean information for the <code>JFontChooser</code> JavaBean.
 *
 * @author Christos Bohoris
 * @see JFontChooser
 */
public class JFontChooserBeanInfo extends SimpleBeanInfo {
    
    /* 16x16 color icon. */
    private final Image iconColor16 = loadImage("resources/connectina/FontChooser16Color.png");
    /* 32x32 color icon. */
    private final Image iconColor32 = loadImage("resources/connectina/FontChooser32Color.png");
    /* 16x16 mono icon. */
    private final Image iconMono16 = loadImage("resources/connectina/FontChooser16Mono.png");
    /* 32x32 mono icon. */
    private final Image iconMono32 = loadImage("resources/connectina/FontChooser32Mono.png");
    /* The bean descriptor. */
    private JFontChooserBeanDescriptor descriptor = new JFontChooserBeanDescriptor();

    /**
     * Get the bean descriptor.
     *
     * @return the bean descriptor
     */
    //Java5 @Override
    public BeanDescriptor getBeanDescriptor() {
        return descriptor;
    }

    /**
     * Get the appropriate icon.
     *
     * @param iconKind the icon kind
     * @return the image
     */
    //Java5 @Override
    public Image getIcon(int iconKind) {
        switch (iconKind) {
            case ICON_COLOR_16x16:
                return iconColor16;
            case ICON_COLOR_32x32:
                return iconColor32;
            case ICON_MONO_16x16:
                return iconMono16;
            case ICON_MONO_32x32:
                return iconMono32;
        }

        return null;
    }

}
