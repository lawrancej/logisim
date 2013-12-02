/* Copyright (c) 2011, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import com.cburch.logisim.comp.ComponentFactory;

public abstract class SimulationTreeNode implements TreeNode {
    public abstract ComponentFactory getComponentFactory();
    public boolean isCurrentView(SimulationTreeModel model) {
        return false;
    }

    @Override
    public abstract Enumeration<?> children();
    @Override
    public abstract boolean getAllowsChildren();
    @Override
    public abstract TreeNode getChildAt(int childIndex);
    @Override
    public abstract int getChildCount();
    @Override
    public abstract int getIndex(TreeNode node);
    @Override
    public abstract TreeNode getParent();
    @Override
    public abstract boolean isLeaf();
}
