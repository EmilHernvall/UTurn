package se.c0la.uturn.component;

import se.c0la.uturn.model.*;

public interface EditorState
{
    public void setPagePlan(PagePlan plan);
    public int getPageIndex();
    public void setPageIndex(int pageIdx);
    public void addEditorListener(EditorListener listener);
    public void update();
}
