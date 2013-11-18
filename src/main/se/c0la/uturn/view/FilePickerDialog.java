package se.c0la.uturn.view;

import java.awt.Frame;
import javax.swing.UIManager;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.FileDialog;
import java.io.FilenameFilter;

public abstract class FilePickerDialog
{
    public enum Type
    {
        OPEN,
        SAVE;
    }

    private static class SwingImpl extends FilePickerDialog
    {
        private JFileChooser chooser;
        private File selected = null;

        public SwingImpl(Frame parent, Type type)
        {
            super(parent, type);

            chooser = new JFileChooser();
        }

        @Override
        public void addFileType(String name, String ext)
        {
            FileNameExtensionFilter filter =
                new FileNameExtensionFilter(name, ext);
            chooser.setFileFilter(filter);
        }

        @Override
        public void setCurrentDirectory(File dir)
        {
            chooser.setCurrentDirectory(dir);
        }

        @Override
        public void show()
        {
            int returnVal;
            if (Type.SAVE == type) {
                returnVal = chooser.showSaveDialog(parent);
            } else if (Type.OPEN == type) {
                returnVal = chooser.showOpenDialog(parent);
            } else {
                return;
            }

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                selected = chooser.getSelectedFile();
            }
        }

        @Override
        public File getSelectedFile()
        {
            return selected;
        }
    }

    private static class AWTImpl extends FilePickerDialog
    {
        private FileDialog dialog;

        public AWTImpl(Frame parent, Type type)
        {
            super(parent, type);

            if (Type.SAVE == type) {
                dialog = new FileDialog(parent, "Save");
                dialog.setMode(FileDialog.SAVE);
            } else if (Type.OPEN == type) {
                dialog = new FileDialog(parent, "Open");
                dialog.setMode(FileDialog.LOAD);
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public void addFileType(String name, final String ext)
        {
            dialog.setFilenameFilter(
                new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(ext);
                    }
                });
        }

        @Override
        public void setCurrentDirectory(File dir)
        {
            dialog.setDirectory(dir.getPath());
        }

        @Override
        public void show()
        {
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
        }

        @Override
        public File getSelectedFile()
        {
            String fileName = dialog.getFile();
            if (fileName == null) {
                return null;
            }

            File fileDir = new File(dialog.getDirectory());
            return new File(fileDir, fileName);
        }
    }

    public static FilePickerDialog createSaveDialog(Frame parent)
    {
        String gtkLaf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        if (gtkLaf.equals(UIManager.getLookAndFeel().getClass().getName())) {
            return new AWTImpl(parent, Type.SAVE);
        } else {
            return new SwingImpl(parent, Type.SAVE);
        }
    }

    public static FilePickerDialog createOpenDialog(Frame parent)
    {
        String gtkLaf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        if (gtkLaf.equals(UIManager.getLookAndFeel().getClass().getName())) {
            return new AWTImpl(parent, Type.OPEN);
        } else {
            return new SwingImpl(parent, Type.OPEN);
        }
    }

    protected Type type;
    protected Frame parent;

    private FilePickerDialog(Frame parent, Type type)
    {
        this.parent = parent;
        this.type = type;
    }

    public abstract void addFileType(String name, String ext);
    public abstract void setCurrentDirectory(File file);
    public abstract void show();
    public abstract File getSelectedFile();
}
