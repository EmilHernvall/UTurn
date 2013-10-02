package com.znaptag.ui;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.znaptag.ui.event.*;

public interface HasMouseEvents
{
    public void addMouseListener(MouseListener ml);
    public void addMouseMotionListener(MouseMotionListener ml);
}
