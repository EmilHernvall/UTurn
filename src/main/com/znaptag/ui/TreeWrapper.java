package com.znaptag.ui;

import java.util.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import com.znaptag.ui.event.TreeListener;
import com.znaptag.ui.event.TreeEvent;

public class TreeWrapper
extends MouseAdapter
implements HasTreeEvents<TreeNode>, TreeSelectionListener
{
    private JTree tree;
    private List<TreeListener<TreeNode>> listeners;

    public TreeWrapper(JTree tree)
    {
        this.tree = tree;
        this.listeners = new ArrayList<TreeListener<TreeNode>>();

        tree.addMouseListener(this);
        tree.addTreeSelectionListener(this);
    }

    @Override
    public void addTreeListener(TreeListener<TreeNode> listener)
    {
        listeners.add(listener);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e)
    {
        TreePath selPath = tree.getSelectionPath();
        if (selPath == null) {
            return;
        }

        TreeNode node = (TreeNode)selPath.getLastPathComponent();
        if (node == null) {
            return;
        }

        for (TreeListener<TreeNode> listener : listeners) {
            listener.nodeSelected(new TreeEvent<TreeNode>(node));
        }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        if (e.getClickCount() == 2) {
            e.consume();

            int selRow = tree.getRowForLocation(e.getX(), e.getY());
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
            if (selPath == null) {
                return;
            }

            TreeNode node = (TreeNode)selPath.getLastPathComponent();

            for (TreeListener<TreeNode> listener : listeners) {
                listener.nodeAction(new TreeEvent<TreeNode>(node));
            }
        }
    }
}
