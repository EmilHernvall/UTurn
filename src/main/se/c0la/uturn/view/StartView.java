package se.c0la.uturn.view;

import java.util.Enumeration;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.activation.DataHandler;
import javax.swing.JSpinner;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JWindow;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import static javax.swing.BorderFactory.*;
import static javax.swing.TransferHandler.TransferSupport;
import javax.swing.SpinnerNumberModel;

import com.znaptag.ui.HasAction;
import com.znaptag.ui.HasValue;
import com.znaptag.ui.ButtonWrapper;
import com.znaptag.ui.TextFieldWrapper;
import com.znaptag.ui.FlatSplitPaneUI;
import com.znaptag.ui.HasTreeEvents;
import com.znaptag.ui.TreeWrapper;
import com.znaptag.ui.SliderWrapper;
import com.znaptag.ui.Action;
import com.znaptag.ui.ActionHandler;
import com.znaptag.ui.ListWrapper;
import com.znaptag.ui.HasMouseEvents;

import se.c0la.uturn.window.StartWindow;
import se.c0la.uturn.component.PageEditor;
import se.c0la.uturn.component.HasEditorEvents;
import se.c0la.uturn.component.EditorListener;
import se.c0la.uturn.model.*;
import static se.c0la.uturn.window.StartWindow.Menu;

public class StartView extends JFrame implements StartWindow.View
{
    private static class SplitDialog extends JDialog
    {
        private Element.SplitAxis result = null;
        private SpinnerNumberModel spinnerModel;

        public SplitDialog(java.awt.Window parent)
        {
            super(parent);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

                spinnerModel = new SpinnerNumberModel(2, 2, 10, 1);
                JSpinner spinner = new JSpinner(spinnerModel);
                panel.add(spinner);

                JButton splitHorizontalButton = new JButton("Split Horizontal");
                splitHorizontalButton.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            result = Element.SplitAxis.HORIZONTAL;
                            setVisible(false);
                        }
                    });
                panel.add(splitHorizontalButton);

                JButton splitVerticalButton = new JButton("Split Vertical");
                splitVerticalButton.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            result = Element.SplitAxis.VERTICAL;
                            setVisible(false);
                        }
                    });
                panel.add(splitVerticalButton);

                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            result = null;
                            setVisible(false);
                        }
                    });
                panel.add(cancelButton);

            add(panel);
            setDefaultLookAndFeelDecorated(false);
            setUndecorated(true);
            setModal(true);

            pack();
        }

        public StartWindow.SplitResult getResult()
        {
            return new StartWindow.SplitResult(result, (Integer)spinnerModel.getValue());
        }
    }

    private JMenuItem newMenu;
    private JMenuItem openMenu;
    private JMenuItem saveMenu;
    private JMenuItem exportMenu;
    private JMenuItem quitMenu;
    private JMenuItem documentationMenu;
    private JMenuItem aboutMenu;

    private PageEditor editor;

    private SplitDialog splitDialog;

    public StartView()
    {
        super();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(createEmptyBorder(5,5,5,5));

            editor = new PageEditor();
            mainPanel.add(editor);

        add(mainPanel);

        setJMenuBar(createMenuBar());

        pack();

        setTitle("UTurn");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        this.splitDialog = new SplitDialog(this);
    }

    private JMenuBar createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

            JMenu fileMenu = new JMenu("File");

                newMenu = new JMenuItem("New project");
                fileMenu.add(newMenu);

                openMenu = new JMenuItem("Open project");
                fileMenu.add(openMenu);

                saveMenu = new JMenuItem("Save project");
                fileMenu.add(saveMenu);

                exportMenu = new JMenuItem("Export as .zan");
                fileMenu.add(exportMenu);

                quitMenu = new JMenuItem("Quit UTurn");
                fileMenu.add(quitMenu);

            menuBar.add(fileMenu);

            JMenu helpMenu = new JMenu("Help");

                documentationMenu = new JMenuItem("Documentation");
                helpMenu.add(documentationMenu);

                aboutMenu = new JMenuItem("About UTurn");
                helpMenu.add(aboutMenu);

            menuBar.add(helpMenu);

        return menuBar;
    }

    @Override
    public void setMenuAction(Action<Menu> action)
    {
        newMenu.addActionListener(new ActionHandler<Menu>(action, Menu.NEW));
        openMenu.addActionListener(new ActionHandler<Menu>(action, Menu.OPEN));
        saveMenu.addActionListener(new ActionHandler<Menu>(action, Menu.SAVE));
        exportMenu.addActionListener(new ActionHandler<Menu>(action, Menu.EXPORT));
        quitMenu.addActionListener(new ActionHandler<Menu>(action, Menu.QUIT));
        documentationMenu.addActionListener(new ActionHandler<Menu>(action, Menu.DOCUMENTATION));
        aboutMenu.addActionListener(new ActionHandler<Menu>(action, Menu.ABOUT));
    }

    @Override
    public void setPagePlan(PagePlan plan)
    {
        editor.setPagePlan(plan);
    }

    @Override
    public HasEditorEvents getEditor()
    {
        return editor;
    }

    @Override
    public StartWindow.SplitResult showSplitDialog(Point p)
    {
        splitDialog.setLocation(p);
        splitDialog.setVisible(true);
        return splitDialog.getResult();
    }

    @Override
    public File showSaveProject(File dir)
    {
        /*JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(dir);
        FileNameExtensionFilter filter =
            new FileNameExtensionFilter("UTurn project", "zap");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }*/

        FileDialog dialog = new FileDialog(this, "Save project");
        dialog.setMode(FileDialog.SAVE);
        dialog.setDirectory(dir.getPath());
        dialog.setFilenameFilter(
            new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".zap");
                }
            });
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        String fileName = dialog.getFile();
        if (fileName == null) {
            return null;
        }

        File fileDir = new File(dialog.getDirectory());
        return new File(fileDir, fileName);
    }

    @Override
    public File showOpenProject(File dir)
    {
        /*JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(dir);
        FileNameExtensionFilter filter =
            new FileNameExtensionFilter("UTurn project", "zap");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }*/

        FileDialog dialog = new FileDialog(this, "Open project");
        dialog.setMode(FileDialog.LOAD);
        dialog.setDirectory(dir.getPath());
        dialog.setFilenameFilter(
            new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".zap");
                }
            });
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        String fileName = dialog.getFile();
        if (fileName == null) {
            return null;
        }

        File fileDir = new File(dialog.getDirectory());
        return new File(fileDir, fileName);
    }

    @Override
    public void showMessageBox(String message)
    {
        JOptionPane.showMessageDialog(this, message);
    }

    @Override
    public JFrame getSwingFrame()
    {
        return this;
    }

    @Override
    public void display()
    {
        setSize(new Dimension(1400, 1000));
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
