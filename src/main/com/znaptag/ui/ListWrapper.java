package com.znaptag.ui;

import java.util.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

import com.znaptag.ui.event.TreeListener;
import com.znaptag.ui.event.TreeEvent;

public class ListWrapper implements HasMouseEvents
{
    private JList list;

    public ListWrapper(JList list)
    {
        this.list = list;
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
