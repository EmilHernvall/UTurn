package com.znaptag.ui;

import javax.swing.JFrame;

public interface WindowView
{
    JFrame getSwingFrame();
    void showMessageBox(String message);
    void setTitle(String title);
    void display();
}
