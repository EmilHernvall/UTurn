package com.znaptag.ui.event;

public interface TableListener
{
    public void rowSelected(TableRowSelectedEvent e);
    public void triggerReorder(TableTriggerReorderEvent e);
    public void triggerPopup(TableTriggerPopupEvent e);
}
