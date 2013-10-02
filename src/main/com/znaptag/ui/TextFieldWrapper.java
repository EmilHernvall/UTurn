package com.znaptag.ui;

import javax.swing.text.JTextComponent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

public class TextFieldWrapper implements HasValue<String>, HasKeyEvents
{
    private JTextComponent field;

    public TextFieldWrapper(JTextComponent field)
    {
        this.field = field;
    }

    @Override
    public void addKeyListener(KeyListener kl)
    {
        field.addKeyListener(kl);
    }

    @Override
    public String getValue()
    {
        return field.getText();
    }

    @Override
    public void setValue(String value)
    {
        field.setText(value);
    }

    @Override
    public void addValueChangeListener(ValueChangeListener<String> listener)
    {
        throw new UnsupportedOperationException();
    }
}
