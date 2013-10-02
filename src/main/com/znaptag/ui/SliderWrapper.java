package com.znaptag.ui;

import java.util.List;
import java.util.ArrayList;

import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class SliderWrapper implements HasValue<Integer>, ChangeListener
{
    private JSlider slider;
    private List<ValueChangeListener<Integer>> listeners;

    public SliderWrapper(JSlider slider)
    {
        this.slider = slider;
        this.listeners = new ArrayList<ValueChangeListener<Integer>>();

        slider.addChangeListener(this);
    }

    @Override
    public Integer getValue()
    {
        return slider.getValue();
    }

    @Override
    public void setValue(Integer value)
    {
        slider.setValue(value);
    }

    @Override
    public void addValueChangeListener(ValueChangeListener<Integer> listener)
    {
        listeners.add(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        for (ValueChangeListener<Integer> listener : listeners) {
            listener.onValueChange(slider.getValue());
        }
    }
}
