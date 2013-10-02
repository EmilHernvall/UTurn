package com.znaptag.ui;

import com.znaptag.ui.event.*;

public interface HasTreeEvents<V>
{
    public void addTreeListener(TreeListener<V> tl);
}
