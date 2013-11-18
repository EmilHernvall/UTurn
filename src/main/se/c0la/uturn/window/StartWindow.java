package se.c0la.uturn.window;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import com.znaptag.ui.Action;
import com.znaptag.ui.HasAction;
import com.znaptag.ui.HasListEvents;
import com.znaptag.ui.HasValue;
import com.znaptag.ui.HasMouseEvents;
import com.znaptag.ui.ValueChangeListener;
import com.znaptag.ui.WindowView;
import com.znaptag.ui.event.TreeEvent;
import com.znaptag.ui.event.TreeListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;

import se.c0la.uturn.model.*;
import se.c0la.uturn.component.EditorState;
import se.c0la.uturn.component.EditorListener;

public class StartWindow
{
    public static enum ElementTool
    {
        TEXT,
        COLOR,
        DELETE,
        SPLIT;
    }

    public enum Menu
    {
        NEW,
        OPEN,
        SAVE,
        EXPORT,
        QUIT,
        DOCUMENTATION,
        ABOUT;
    }

    public enum ContextMenu
    {
        INSERT_PAGE,
        DELETE_PAGE;
    }

    public static class ElementAction
    {
        public ElementTool tool;
        public Element.SplitAxis splitAxis;
        public int splitCount;

        public ElementAction(ElementTool tool)
        {
            this.tool = tool;
        }
    }

    public interface View extends WindowView
    {
        void setMenuAction(Action<Menu> action);
        void setContextMenuAction(Action<ContextMenu> action);
        void setPreviewListModel(PagePlanListModel model);
        void addWindowListener(WindowListener listener);
        EditorState getEditor();
        HasAction getPrevPageButton();
        HasAction getNextPageButton();
        HasListEvents getPreviewList();

        ElementAction showSplitDialog(Point p);
        File showSaveProject(File dir);
        File showOpenProject(File dir);
        String getText();
        Color getColor();
        void setSelectedSpreadIndex(int spreadIdx);
        int getSelectedSpreadIndex();
        void showContextMenu(Point p);
    }

    private View view;

    private File currentDir;
    private PagePlan currentPlan;

    public StartWindow(View view)
    {
        this.view = view;
        this.currentDir = new File(".");

        loadSettings();

        bind();

        currentPlan = new PagePlan(1);

        EditorState editor = view.getEditor();
        editor.setPagePlan(currentPlan);
        editor.setPageIndex(0);

        view.setPreviewListModel(new PagePlanListModel(currentPlan));
        view.setSelectedSpreadIndex(0);
    }

    private void bind()
    {
        view.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    quit();
                }
            });

        EditorState editor = view.getEditor();
        editor.addEditorListener(
            new EditorListener() {
                public void onElementClicked(Element element, Point p) {
                    if (element.isSplit()) {
                        return;
                    }

                    ElementAction result = view.showSplitDialog(p);
                    if (result.tool == null) {
                        return;
                    }

                    EditorState editor = view.getEditor();
                    if (result.tool == ElementTool.SPLIT) {
                        element.split(result.splitCount, result.splitAxis);
                        editor.update();
                    }
                    else if (result.tool == ElementTool.TEXT) {
                        String text = view.getText();
                        if (text == null) {
                            return;
                        }

                        element.setContent(text);
                        editor.update();
                    }
                    else if (result.tool == ElementTool.COLOR) {
                        Color color = view.getColor();
                        if (color == null) {
                            return;
                        }
                        element.setColor(color);
                        editor.update();
                    }
                    else if (result.tool == ElementTool.DELETE) {
                        System.out.println("delete");
                    }
                }
            });

        HasAction prevPageButton = view.getPrevPageButton();
        prevPageButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    EditorState editor = view.getEditor();
                    int prevIdx = currentPlan.getPrevIndex(editor.getPageIndex());
                    if (prevIdx == -1) {
                        return;
                    }

                    editor.setPageIndex(prevIdx);

                    Page page = currentPlan.getPage(prevIdx);
                    int spreadIdx = currentPlan.getSpreadIndex(page);
                    view.setSelectedSpreadIndex(spreadIdx);
                }
            });

        HasAction nextPageButton = view.getNextPageButton();
        nextPageButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    EditorState editor = view.getEditor();
                    int nextIdx = currentPlan.getNextIndex(editor.getPageIndex());
                    if (nextIdx == -1) {
                        return;
                    }

                    editor.setPageIndex(nextIdx);

                    Page page = currentPlan.getPage(nextIdx);
                    int spreadIdx = currentPlan.getSpreadIndex(page);
                    view.setSelectedSpreadIndex(spreadIdx);
                }
            });

        HasListEvents previewList = view.getPreviewList();
        previewList.addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) {
                        return;
                    }

                    int spreadIdx = view.getSelectedSpreadIndex();
                    Page page = currentPlan.getSpread(spreadIdx);
                    int pageIdx = currentPlan.getPageIndex(page);

                    EditorState editor = view.getEditor();
                    editor.setPageIndex(pageIdx);
                }
            });

        previewList.addMouseListener(
            new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() != MouseEvent.BUTTON3) {
                        return;
                    }

                    view.showContextMenu(e.getPoint());
                }
            });

        view.setMenuAction(
            new Action<Menu>() {
                public void execute(Menu item) {
                    switch (item) {
                        case NEW: break;
                        case OPEN:
                            loadProject();
                            break;
                        case SAVE:
                            saveProject();
                            break;
                        case EXPORT:
                            break;
                        case QUIT:
                            quit();
                            break;
                        case DOCUMENTATION:
                            view.showMessageBox("N/A");
                            break;
                        case ABOUT:
                            view.showMessageBox("UTurn 0.1");
                            break;
                    }
                }
            });

        view.setContextMenuAction(
            new Action<ContextMenu>() {
                public void execute(ContextMenu item) {
                    switch (item) {
                        case INSERT_PAGE:
                            insertPage();
                            break;
                        case DELETE_PAGE:
                            break;
                    }
                }
            });
    }

    private void insertPage()
    {
        int spreadIdx = view.getSelectedSpreadIndex();
        Page pg = currentPlan.getSpread(spreadIdx);
        int pageIdx = currentPlan.getPageIndex(pg);
        if (pg.isSpread()) {
            currentPlan.insertPages(pageIdx+1, 2, true);
        } else {
            currentPlan.insertPages(pageIdx, 2, true);
        }
    }

    private void saveProject()
    {
        //try {
            File saveFile = view.showSaveProject(currentDir);
            if (saveFile != null) {
                if (!saveFile.getName().endsWith(".utp")) {
                    saveFile = new File(saveFile.getParentFile(), saveFile.getName() + ".zap");
                }

                //PrintWriter writer = new PrintWriter(saveFile);
                //writer.println(saveData.toString());
                //writer.close();
            }
        //}
        //catch (IOException e) {
        //    e.printStackTrace();
        //}
        //catch (JSONException e) {
        //    e.printStackTrace();
        //}
    }

    private void loadProject()
    {
        File projectFile = view.showOpenProject(currentDir);
        if (projectFile == null) {
            return;
        }

        /*try {
            FileInputStream input = new FileInputStream(projectFile);
            JSONTokener tokener = new JSONTokener(new InputStreamReader(input));
            JSONObject data = new JSONObject(tokener);

            Map<String, Element> lookup = new HashMap<String, Element>();
            JSONArray elements = data.getJSONArray("elements");
            for (int i = 0; i < elements.length(); i++) {
                String fileName = elements.getString(i);
                File file = new File(fileName);
                try {
                    Element element = new Element(file);
                    elementsListModel.addElement(element);
                    lookup.put(fileName, element);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private void quit()
    {
        saveSettings();
        System.exit(0);
    }

    private void loadSettings()
    {
        Properties settings = new Properties();
        try {
            File settingsFile = new File("uturn.ini");
            if (!settingsFile.exists()) {
                return;
            }

            settings.load(new FileInputStream(settingsFile));

            currentDir = new File((String)settings.get("currentDir"));
        }
        catch (IOException e) {
            // We don't care
        }
    }

    private void saveSettings()
    {
        Properties settings = new Properties();
        settings.put("currentDir", currentDir.getPath());

        try {
            File settingsFile = new File("uturn.ini");
            settings.store(new FileOutputStream(settingsFile), "UTurn settings");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
