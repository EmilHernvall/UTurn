package se.c0la.uturn;

import javax.swing.UIManager;

import se.c0la.uturn.window.StartWindow;
import se.c0la.uturn.view.StartView;

public class Main
{
    public static void main(String[] args)
    throws Exception
    {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            System.out.println(info.getClassName());
        }

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (Exception e) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }

        StartWindow.View startView = new StartView();
        StartWindow startWindow = new StartWindow(startView);
        startView.display();
    }
}
