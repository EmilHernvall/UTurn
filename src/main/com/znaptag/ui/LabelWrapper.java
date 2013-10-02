package com.znaptag.ui;

import java.util.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

import com.znaptag.ui.event.TreeListener;
import com.znaptag.ui.event.TreeEvent;

public class LabelWrapper implements HasMouseEvents
{
    private JLabel label;

    public LabelWrapper(JLabel label)
    {
        this.label = label;
    }

    @Override
    public void addMouseListener(MouseListener listener)
    {
        label.addMouseListener(listener);
    }

    @Override
    public void addMouseMotionListener(MouseMotionListener listener)
    {
        label.addMouseMotionListener(listener);
    }
}
