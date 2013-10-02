package com.znaptag.ui;

import javax.swing.JButton;
import java.awt.event.ActionListener;

public class ButtonWrapper implements HasAction
{
    private JButton button;

    public ButtonWrapper(JButton button)
    {
        this.button = button;
    }

    public JButton getButton()
    {
        return button;
    }

    @Override
    public void setEnabled(boolean v)
    {
        button.setEnabled(v);
    }

    @Override
    public void addActionListener(ActionListener al)
    {
        button.addActionListener(al);
    }
}
