package com.znaptag.ui.event;

public class TableTriggerReorderEvent
{
    private String column;

    public TableTriggerReorderEvent(String column)
    {
        this.column = column;
    }

    public String getColumn()
    {
        return column;
    }
}
