package com.znaptag.ui.event;

import java.awt.Point;
import java.awt.Component;

public class TableRowSelectedEvent
{
    private int selectedRow;

    public TableRowSelectedEvent(int selectedRow)
    {
        this.selectedRow = selectedRow;
    }
    
    public int getSelectedRow()
    {
        return selectedRow;
    }
}
