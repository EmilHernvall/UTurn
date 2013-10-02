package com.znaptag.ui.event;

import java.awt.Point;
import java.awt.Component;

public class TableTriggerPopupEvent
{
    private Component comp;
    private Point point;

    public TableTriggerPopupEvent(Component comp, Point point)
    {
        this.comp = comp;
        this.point = point;
    }

    public Component getComponent()
    {
        return comp;
    }

    public Point getPoint()
    {
        return point;
    }
}
