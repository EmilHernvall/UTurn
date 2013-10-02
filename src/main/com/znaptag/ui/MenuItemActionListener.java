package com.znaptag.ui;

import java.awt.event.ActionListener;

public abstract class MenuItemActionListener implements ActionListener
{
    private String name;
    private int counter;
    private boolean state;

    public MenuItemActionListener(String name)
    {
        this.name = name;
        this.counter = 0;
        this.state = false;
    }

    public MenuItemActionListener(String name, int counter, boolean state)
    {
        this.name = name;
        this.counter = counter;
        this.state = state;
    }

    public String getName()
    {
        return name;
    }

    public int getCounter()
    {
        return counter;
    }

    public boolean getState()
    {
        return state;
    }
}
