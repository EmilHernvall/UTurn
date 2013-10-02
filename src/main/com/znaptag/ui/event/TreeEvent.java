package com.znaptag.ui.event;

public class TreeEvent<V>
{
    private V value;

    public TreeEvent(V value)
    {
        this.value = value;
    }

    public V getValue()
    {
        return value;
    }
}
