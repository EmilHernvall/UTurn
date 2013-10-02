package com.znaptag.ui;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ActionHandler<E> implements ActionListener
{
    private Action<E> action;
    private E param;

    public ActionHandler(Action<E> action, E param)
    {
        this.action = action;
        this.param = param;
    }

    @Override
    public void actionPerformed(ActionEvent event)
    {
        action.execute(param);
    }
}
