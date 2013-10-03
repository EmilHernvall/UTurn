package com.znaptag.ui;

import java.util.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.*;

import com.znaptag.ui.event.TreeListener;
import com.znaptag.ui.event.TreeEvent;

public class ListWrapper implements HasListEvents
{
    private JList list;

    public ListWrapper(JList list)
    {
        this.list = list;
    }

    @Override
    public void addListSelectionListener(ListSelectionListener listener)
    {
        list.addListSelectionListener(listener);
    }

    @Override
    public void addMouseListener(MouseListener listener)
    {
        list.addMouseListener(listener);
    }

    @Override
    public void addMouseMotionListener(MouseMotionListener listener)
    {
        list.addMouseMotionListener(listener);
    }
}
