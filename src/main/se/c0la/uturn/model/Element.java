package se.c0la.uturn.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Element
{
    public static int TOTAL_SIZE = 360;

    public enum SplitAxis
    {
        HORIZONTAL,
        VERTICAL;
    }

    private Page page = null;
    private Element parent = null;
    private List<Element> elements = null;
    private int[] sizes = null;
    private SplitAxis axis = null;
    private String content = null;

    public Element(Page page, Element parent)
    {
        this.page = page;
        this.parent = parent;
    }

    public Element getParent()
    {
        return parent;
    }

    public boolean isRoot()
    {
        return parent == null;
    }

    public boolean isSplit()
    {
        return elements != null;
    }

    public SplitAxis getSplitAxis()
    {
        return axis;
    }

    public void split(int count)
    {
        if (parent == null) {
            split(count, SplitAxis.VERTICAL);
        }
        else if (parent.getSplitAxis() == SplitAxis.VERTICAL) {
            split(count, SplitAxis.HORIZONTAL);
        }
        else {
            split(count, SplitAxis.VERTICAL);
        }
    }

    public void split(int count, SplitAxis axis)
    {
        if (isSplit()) {
            throw new IllegalStateException("Already split");
        }

        this.axis = axis;

        elements = new ArrayList<Element>();
        sizes = new int[count];
        int step = TOTAL_SIZE / count;
        int sum = 0;
        for (int i = 0; i < count; i++) {
            if (i != count - 1) {
                sizes[i] = step;
                sum += step;
            } else {
                sizes[i] = TOTAL_SIZE - sum;
            }
            elements.add(new Element(page, this));
        }

        PagePlan plan = page.getPagePlan();
        plan.firePageChanged(page);
    }

    public int getElementIndex(Element child)
    {
        if (!isSplit()) {
            throw new IllegalStateException("Unsplit element has no children");
        }

        return elements.indexOf(child);
    }

    public int getChildCount()
    {
        if (!isSplit()) {
            throw new IllegalStateException("Unsplit element has no children");
        }

        return elements.size();
    }

    public int getMySize()
    {
        int idx = parent.getElementIndex(this);
        return parent.sizes[idx];
    }

    public double getSizeFraction(Element element)
    {
        int idx = getElementIndex(element);
        return sizes[idx] / (double)TOTAL_SIZE;
    }

    public boolean setSizeFraction(Element element, boolean fartherEdge, double newSize)
    {
        int idx = getElementIndex(element);
        int otherIdx;
        if (fartherEdge) {
            if (idx + 1 >= getChildCount()) {
                throw new IllegalStateException("Can't size outwards");
            }

            otherIdx = idx + 1;
        } else {
            if (idx - 1 < 0) {
                throw new IllegalStateException("Can't size outwards");
            }

            otherIdx = idx - 1;
        }

        double totalFraction = getSizeFraction(element) + getSizeFraction(otherIdx);
        if (totalFraction < newSize) {
            return false;
        }

        int total = sizes[idx] + sizes[otherIdx];
        int otherSize = (int)(TOTAL_SIZE*(totalFraction - newSize));
        sizes[idx] = total - otherSize;
        sizes[otherIdx] = otherSize;

        PagePlan plan = page.getPagePlan();
        plan.firePageChanged(page);

        return true;
    }

    public double getSizeFraction(int idx)
    {
        return sizes[idx] / (double)TOTAL_SIZE;
    }

    public List<Element> children()
    {
        if (!isSplit()) {
            throw new IllegalStateException("Unsplit element has no children");
        }

        return Collections.unmodifiableList(elements);
    }
}
