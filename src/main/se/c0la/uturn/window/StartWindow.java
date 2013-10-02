package se.c0la.uturn.window;

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
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.znaptag.ui.Action;
import com.znaptag.ui.HasAction;
import com.znaptag.ui.HasTreeEvents;
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
import se.c0la.uturn.component.HasEditorEvents;
import se.c0la.uturn.component.EditorListener;

public class StartWindow
{
    public static class SplitResult
    {
        Element.SplitAxis axis;
        int count;

        public SplitResult(Element.SplitAxis axis, int count)
        {
            this.axis = axis;
            this.count = count;
        }
    }

    public interface View extends WindowView
    {
        void setMenuAction(Action<Menu> action);
        void addWindowListener(WindowListener listener);
        void setPagePlan(PagePlan plan);
        HasEditorEvents getEditor();

        SplitResult showSplitDialog(Point p);
        File showSaveProject(File dir);
        File showOpenProject(File dir);
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

    private View view;

    private File currentDir;
    private PagePlan currentPlan;

    public StartWindow(View view)
    {
        this.view = view;
        this.currentDir = new File(".");

        loadSettings();

        bind();

        currentPlan = new PagePlan(20);
        view.setPagePlan(currentPlan);
    }

    private void bind()
    {
        view.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    quit();
                }
            });

        HasEditorEvents editor = view.getEditor();
        editor.addEditorListener(
            new EditorListener() {
                public void onElementClicked(Element element, Point p) {
                    System.out.println("Matching element found: " + element);
                    if (element.isSplit()) {
                        return;
                    }

                    SplitResult result = view.showSplitDialog(p);
                    if (result.axis == null) {
                        return;
                    }

                    element.split(result.count, result.axis);

                    HasEditorEvents editor = view.getEditor();
                    editor.update();
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
    }

    private void saveProject()
    {
        /*try {
            File saveFile = view.showSaveProject(currentDir);
            if (saveFile != null) {
                if (!saveFile.getName().endsWith(".zap")) {
                    saveFile = new File(saveFile.getParentFile(), saveFile.getName() + ".zap");
                }

                PrintWriter writer = new PrintWriter(saveFile);
                writer.println(saveData.toString());
                writer.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    private void loadProject()
    {
        /*File projectFile = view.showOpenProject(currentDir);
        if (projectFile == null) {
            return;
        }

        try {
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
            File settingsFile = new File("znaptool.ini");
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
            File settingsFile = new File("znaptool.ini");
            settings.store(new FileOutputStream(settingsFile), "UTurn settings");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
