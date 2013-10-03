package com.znaptag.ui;

import javax.swing.event.ListSelectionListener;

public interface HasListEvents extends HasMouseEvents
{
    public void addListSelectionListener(ListSelectionListener listener);
}
