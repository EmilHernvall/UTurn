package se.c0la.uturn.view;

import java.util.Enumeration;
import java.io.File;
import java.io.IOException;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import static javax.swing.BorderFactory.*;
import static javax.swing.TransferHandler.TransferSupport;

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
import com.znaptag.ui.HasListEvents;

import se.c0la.uturn.window.StartWindow;
import se.c0la.uturn.component.PageEditor;
import se.c0la.uturn.component.PageCellRenderer;
import se.c0la.uturn.component.EditorState;
import se.c0la.uturn.component.EditorListener;
import se.c0la.uturn.model.*;
import static se.c0la.uturn.window.StartWindow.Menu;
import static se.c0la.uturn.window.StartWindow.ContextMenu;

public class StartView extends JFrame implements StartWindow.View
{
    private static class SplitDialog extends JDialog
    {
        private StartWindow.ElementTool tool = null;
        private Element.SplitAxis axis = null;
        private SpinnerNumberModel spinnerModel;

        public SplitDialog(java.awt.Window parent)
        {
            super(parent);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(createEmptyBorder(3,3,3,3));

                JPanel optionsPanel = new JPanel();
                optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));

                    JButton textButton = new JButton("Text");
                    textButton.setPreferredSize(new Dimension(100, 30));
                    textButton.setMaximumSize(new Dimension(100, 30));
                    textButton.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                tool = StartWindow.ElementTool.TEXT;
                                setVisible(false);
                            }
                        });
                    optionsPanel.add(textButton);

                    optionsPanel.add(Box.createHorizontalStrut(5));

                    JButton colorButton = new JButton("Color");
                    colorButton.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                tool = StartWindow.ElementTool.COLOR;
                                setVisible(false);
                            }
                        });
                    colorButton.setPreferredSize(new Dimension(100, 30));
                    colorButton.setMaximumSize(new Dimension(100, 30));
                    optionsPanel.add(colorButton);

                    optionsPanel.add(Box.createHorizontalStrut(5));

                    JButton deleteButton = new JButton("Delete");
                    deleteButton.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                tool = StartWindow.ElementTool.DELETE;
                                setVisible(false);
                            }
                        });
                    deleteButton.setPreferredSize(new Dimension(100, 30));
                    deleteButton.setMaximumSize(new Dimension(100, 30));
                    optionsPanel.add(deleteButton);

                panel.add(optionsPanel);

                panel.add(Box.createVerticalStrut(5));

                JPanel splitPanel = new JPanel();
                splitPanel.setLayout(new BoxLayout(splitPanel, BoxLayout.X_AXIS));

                    spinnerModel = new SpinnerNumberModel(2, 2, 10, 1);
                    JSpinner spinner = new JSpinner(spinnerModel);
                    splitPanel.add(spinner);

                    splitPanel.add(Box.createHorizontalStrut(5));

                    JButton splitHorizontalButton = new JButton("Split Horizontal");
                    splitHorizontalButton.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                tool = StartWindow.ElementTool.SPLIT;
                                axis = Element.SplitAxis.HORIZONTAL;
                                setVisible(false);
                            }
                        });
                    splitPanel.add(splitHorizontalButton);

                    splitPanel.add(Box.createHorizontalStrut(5));

                    JButton splitVerticalButton = new JButton("Split Vertical");
                    splitVerticalButton.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                tool = StartWindow.ElementTool.SPLIT;
                                axis = Element.SplitAxis.VERTICAL;
                                setVisible(false);
                            }
                        });
                    splitPanel.add(splitVerticalButton);

                panel.add(splitPanel);

                panel.add(Box.createVerticalStrut(5));

                JPanel cancelPanel = new JPanel();
                cancelPanel.setLayout(new BoxLayout(cancelPanel, BoxLayout.X_AXIS));

                    JButton cancelButton = new JButton("Cancel");
                    cancelButton.setPreferredSize(new Dimension(310, 30));
                    cancelButton.setMaximumSize(new Dimension(310, 30));
                    cancelButton.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                tool = null;
                                setVisible(false);
                            }
                        });
                    cancelPanel.add(cancelButton);

                panel.add(cancelPanel);

            add(panel);
            setDefaultLookAndFeelDecorated(false);
            setUndecorated(true);
            setModal(true);

            pack();
        }

        public StartWindow.ElementAction getResult()
        {
            StartWindow.ElementAction action = new StartWindow.ElementAction(tool);
            if (tool == StartWindow.ElementTool.SPLIT) {
                action.splitCount = (Integer)spinnerModel.getValue();
                action.splitAxis = axis;
            }
            return action;
        }
    }

    private JMenuItem newMenu;
    private JMenuItem openMenu;
    private JMenuItem saveMenu;
    private JMenuItem exportMenu;
    private JMenuItem quitMenu;
    private JMenuItem documentationMenu;
    private JMenuItem aboutMenu;

    private JPopupMenu contextMenu;
    private JMenuItem insertMenu;
    private JMenuItem deleteMenu;
    private JMenuItem spreadMenu;

    private PageEditor editor;
    private JButton prevPageButton;
    private JButton nextPageButton;
    private JList<Page> previewList;

    private SplitDialog splitDialog;

    public StartView()
    {
        super();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(createEmptyBorder(5,5,5,5));

            editor = new PageEditor();
            mainPanel.add(editor);

            JPanel stepPanel = new JPanel();
            stepPanel.setLayout(new BoxLayout(stepPanel, BoxLayout.X_AXIS));
            stepPanel.setBorder(createEmptyBorder(5,5,5,5));

                prevPageButton = new JButton("Previous");
                stepPanel.add(prevPageButton);

                stepPanel.add(Box.createHorizontalGlue());

                nextPageButton = new JButton("Next");
                stepPanel.add(nextPageButton);

            mainPanel.add(stepPanel);

        previewList = new JList<Page>();
        previewList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        previewList.setCellRenderer(new PageCellRenderer());
        JScrollPane previewListScroller = new JScrollPane(previewList);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                              mainPanel,
                                              previewListScroller);
        splitPane.setBorder(createEmptyBorder(5, 5, 5, 5));
        splitPane.setUI(new FlatSplitPaneUI());
        splitPane.setDividerLocation(900);
        add(splitPane);

        setJMenuBar(createMenuBar());

        pack();

        setTitle("UTurn");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        this.splitDialog = new SplitDialog(this);

        this.contextMenu = createContextMenu();
    }

    private JPopupMenu createContextMenu()
    {
        JPopupMenu menu = new JPopupMenu();

            insertMenu = new JMenuItem("Insert page");
            menu.add(insertMenu);

            deleteMenu = new JMenuItem("Delete page");
            menu.add(deleteMenu);

            spreadMenu = new JMenuItem("Spread");
            menu.add(spreadMenu);

        return menu;
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

                exportMenu = new JMenuItem("Export");
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
    public void setContextMenuAction(Action<ContextMenu> action)
    {
        insertMenu.addActionListener(
                new ActionHandler<ContextMenu>(action, ContextMenu.INSERT_PAGE));
        deleteMenu.addActionListener(
                new ActionHandler<ContextMenu>(action, ContextMenu.DELETE_PAGE));
    }

    @Override
    public void setPreviewListModel(PagePlanListModel model)
    {
        previewList.setModel(model);
    }

    @Override
    public EditorState getEditor()
    {
        return editor;
    }

    @Override
    public HasAction getPrevPageButton()
    {
        return new ButtonWrapper(prevPageButton);
    }

    @Override
    public HasAction getNextPageButton()
    {
        return new ButtonWrapper(nextPageButton);
    }

    @Override
    public HasListEvents getPreviewList()
    {
        return new ListWrapper(previewList);
    }

    @Override
    public StartWindow.ElementAction showSplitDialog(Point p)
    {
        splitDialog.setLocation(p);
        splitDialog.setVisible(true);
        return splitDialog.getResult();
    }

    @Override
    public File showSaveProject(File dir)
    {
        FilePickerDialog dialog = FilePickerDialog.createSaveDialog(this);
        dialog.addFileType("UTurn project", "utp");
        dialog.setCurrentDirectory(dir);
        dialog.show();

        return dialog.getSelectedFile();
    }

    @Override
    public File showOpenProject(File dir)
    {
        FilePickerDialog dialog = FilePickerDialog.createOpenDialog(this);
        dialog.addFileType("UTurn project", "utp");
        dialog.setCurrentDirectory(dir);
        dialog.show();

        return dialog.getSelectedFile();
    }

    @Override
    public String getText()
    {
        return JOptionPane.showInputDialog(this, "Enter text:");
    }

    @Override
    public Color getColor()
    {
        ColorPickerDialog dialog = new ColorPickerDialog(this);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        return dialog.getColor();
    }

    @Override
    public int getSelectedSpreadIndex()
    {
        return previewList.getSelectedIndex();
    }

    @Override
    public void setSelectedSpreadIndex(int spreadIdx)
    {
        previewList.setSelectedIndex(spreadIdx);
        previewList.ensureIndexIsVisible(spreadIdx);
    }

    @Override
    public void showContextMenu(Point p)
    {
        contextMenu.show(previewList, (int)p.getX(), (int)p.getY());
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
        setSize(new Dimension(1200, 600));
        setLocationRelativeTo(null);
        setVisible(true);
    }
}

