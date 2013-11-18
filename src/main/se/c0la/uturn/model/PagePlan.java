package se.c0la.uturn.model;

import java.util.List;
import java.util.ArrayList;

import org.json.*;

public class PagePlan
{
    private List<Page> pages;
    private List<PagePlanListener> listeners;

    public PagePlan()
    {
        pages = new ArrayList<Page>();
        listeners = new ArrayList<PagePlanListener>();
    }

    public PagePlan(int numPages)
    {
        this();

        for (int i = 0; i < numPages; i++) {
            if (i == 0) {
                pages.add(new Page(this, false));
            }
            else if (i == numPages-1 && i % 2 == 1) {
                pages.add(new Page(this, false));
            }
            else {
                pages.add(new Page(this));
            }
        }
    }

    public void addListener(PagePlanListener listener)
    {
        listeners.add(listener);
    }

    public float getPageWidth()
    {
        return 0.210f;
    }

    public float getPageHeight()
    {
        return 0.297f;
    }

    public float getPageRatio()
    {
        return getPageWidth() / getPageHeight();
    }

    public int getPageIndex(Page page)
    {
        return pages.indexOf(page);
    }

    public int getSpreadCount()
    {
        int count = 0;
        for (int i = 0; i < pages.size(); ) {
            Page pg = pages.get(i);
            if (pg.isSpread()) {
                i += 2;
            } else {
                i += 1;
            }
            count += 1;
        }

        return count;
    }

    public Page getSpread(int spreadIdx)
    {
        int count = 0;
        for (int i = 0; i < pages.size(); ) {
            Page pg = pages.get(i);
            if (count == spreadIdx) {
                return pg;
            }

            if (pg.isSpread()) {
                i += 2;
            } else {
                i += 1;
            }
            count += 1;
        }

        return null;
    }

    public int getSpreadIndex(Page pg)
    {
        int count = 0;
        for (int i = 0; i < pages.size(); ) {
            Page current = pages.get(i);
            if (pg == current) {
                return count;
            }

            if (current.isSpread()) {
                i += 2;
            } else {
                i += 1;
            }
            count += 1;
        }

        return -1;
    }

    public Page getPage(int pageIdx)
    {
        if (pageIdx >= pages.size()) {
            throw new ArrayIndexOutOfBoundsException();
        }

        return pages.get(pageIdx);
    }

    public Page getOtherHalfOfSpread(Page page)
    {
        if (!page.isSpread()) {
            throw new IllegalStateException("Page is not part of spread.");
        }

        int pageIdx = getPageIndex(page);
        return pages.get(pageIdx+1);
    }

    public int getPrevIndex(int currentIdx)
    {
        Page currentPage = pages.get(currentIdx);
        int newIdx = 0;
        if (currentIdx - 1 >= 0) {
            Page prev = pages.get(currentIdx-1);
            if (prev.isSpread()) {
                if (currentIdx - 2 >= 0) {
                    return currentIdx - 2;
                } else {
                    return -1;
                }
            } else {
                return currentIdx - 1;
            }
        } else {
            return -1;
        }
    }

    public int getNextIndex(int currentIdx)
    {
        Page currentPage = pages.get(currentIdx);
        int newIdx = 0;
        if (currentPage.isSpread()) {
            newIdx = currentIdx + 2;
        } else {
            newIdx = currentIdx + 1;
        }

        if (newIdx < pages.size()) {
            return newIdx;
        }

        return -1;
    }

    public void insertPagesLast(int count, boolean spread)
    {
        insertPages(pages.size() - 1, count, spread);
    }

    public void insertPages(int idx, int count, boolean spread)
    {
        List<Page> newPages = new ArrayList<Page>();

        int i = 0;
        for (Page current : pages) {
            newPages.add(current);
            if (i == idx) {
                for (int j = 0; j < count; j++) {
                    newPages.add(new Page(this, spread));
                }
            }
            i++;
        }

        pages = newPages;

        firePageAdded(idx);
    }

    public void firePageAdded(int pageIdx)
    {
        for (PagePlanListener listener : listeners) {
            listener.onPageAdded(this, pageIdx);
        }
    }

    public void firePageChanged(Page page)
    {
        for (PagePlanListener listener : listeners) {
            listener.onPageChange(page);
        }
    }

    public JSONObject toJson()
    throws JSONException
    {
        JSONObject obj = new JSONObject();
        JSONArray pagesArr = new JSONArray();
        for (Page page : pages) {
            pagesArr.put(page.toJson());
        }
        obj.put("pages", pagesArr);
        return obj;
    }

    public static PagePlan fromJson(JSONObject obj)
    throws JSONException
    {
        PagePlan plan = new PagePlan();

        JSONArray pagesArr = obj.getJSONArray("pages");
        for (int i = 0; i < pagesArr.length(); i++) {
            plan.pages.add(Page.fromJson(pagesArr.getJSONObject(i), plan));
        }

        return plan;
    }
}
