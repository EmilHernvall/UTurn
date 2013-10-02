package com.znaptag.ui.event;

public interface TreeListener<V>
{
    public void nodeSelected(TreeEvent<V> e);
    public void nodeAction(TreeEvent<V> e);
}
