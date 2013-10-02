package com.znaptag.ui;

import java.beans.PropertyChangeListener;

public interface ProgressListener extends PropertyChangeListener
{
    void addText(String text);
}
