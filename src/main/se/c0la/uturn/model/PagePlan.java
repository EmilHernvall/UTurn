package se.c0la.uturn.model;

import java.util.List;
import java.util.ArrayList;

public class PagePlan
{
    private List<Page> pages;

    public PagePlan()
    {
        pages = new ArrayList<Page>();
    }

    public PagePlan(int numPages)
    {
        this();

        if (numPages % 2 != 0) {
            numPages += 1;
        }

        for (int i = 0; i < numPages; i++) {
            if (i == 0) {
                pages.add(new Page(false));
            }
            else if (i == numPages-1) {
                pages.add(new Page(false));
            }
            else {
                pages.add(new Page());
            }
        }
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
}
