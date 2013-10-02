package com.znaptag.ui;

public interface HasValue<V>
{
    public V getValue();
    public void setValue(V value);
    public void addValueChangeListener(ValueChangeListener<V> listener);
}
