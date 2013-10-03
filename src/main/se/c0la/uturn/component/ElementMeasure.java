package se.c0la.uturn.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Rectangle;
import java.awt.Point;

import se.c0la.uturn.model.*;

public class ElementMeasure
{
    public interface DimensionCallback
    {
        public int getWidth();
        public int getHeight();
    }

    public static class ElementAndRect
    {
        Element element;
        Rectangle rect;

        public ElementAndRect(Element element, Rectangle rect)
        {
            this.element = element;
            this.rect = rect;
        }
    }

    private DimensionCallback callback;

    private int margin;
    private Page firstPage = null;
    private Page secondPage = null;
    private Rectangle firstPageRect = null;
    private Rectangle secondPageRect = null;
    private Map<Element, Rectangle> elementRects = null;

    public ElementMeasure(int margin, DimensionCallback callback)
    {
        this.margin = margin;
        this.callback = callback;
    }

    public Page getFirstPage()
    {
        return firstPage;
    }

    public Page getSecondPage()
    {
        return secondPage;
    }

    public Rectangle getFirstPageRect()
    {
        return firstPageRect;
    }

    public Rectangle getSecondPageRect()
    {
        return secondPageRect;
    }

    public Rectangle getRectangle(Element element)
    {
        return elementRects.get(element);
    }

    private Rectangle calculatePageRect(PagePlan plan, int num, int pos)
    {
        int height = callback.getHeight() - 2*margin;
        int width = (int)(plan.getPageRatio() * height);

        int top = margin;
        int left = (callback.getWidth() - num*width - (num-1)*margin)/2;
        for (int i = 0; i < pos; i++) {
            left += width;
            left += margin;
        }

        return new Rectangle(left, top, width, height);
    }

    public void updateState(PagePlan plan, int pageIdx)
    {
        elementRects = new HashMap<Element, Rectangle>();

        firstPage = plan.getPage(pageIdx);

        if (firstPage.isSpread()) {
            secondPage = plan.getOtherHalfOfSpread(firstPage);

            firstPageRect = calculatePageRect(plan, 2, 0);
            secondPageRect = calculatePageRect(plan, 2, 1);

            updateElement(firstPage.getRootElement(), firstPageRect);
            updateElement(secondPage.getRootElement(), secondPageRect);
        } else {
            firstPageRect = calculatePageRect(plan, 1, 0);
            updateElement(firstPage.getRootElement(), firstPageRect);

            secondPage = null;
            secondPageRect = null;
        }
    }

    private void updateElement(Element element, Rectangle rect)
    {
        elementRects.put(element, rect);

        if (element.isSplit()) {
            Element.SplitAxis axis = element.getSplitAxis();
            List<Element> children = element.children();

            int stepLeft, stepTop;
            int left = rect.x, top = rect.y;
            for (Element child : children) {
                int width, height;
                if (axis == Element.SplitAxis.HORIZONTAL) {
                    width = (int)(rect.width * element.getSizeFraction(child));
                    height = rect.height;
                    stepLeft = width;
                    stepTop = 0;
                } else if (axis == Element.SplitAxis.VERTICAL) {
                    width = rect.width;
                    height = (int)(rect.height * element.getSizeFraction(child));
                    stepLeft = 0;
                    stepTop = height;
                } else {
                    throw new IllegalStateException();
                }

                Rectangle childRect = new Rectangle(left, top, width, height);
                updateElement(child, childRect);
                left += stepLeft;
                top += stepTop;
            }
        }
    }

    private Page findPageFromPoint(Point p)
    {
        if (firstPageRect.contains(p)) {
            return firstPage;
        }
        else if (secondPage != null && secondPageRect.contains(p)) {
            return secondPage;
        }

        return null;
    }

    private ElementAndRect findElementFromPoint(Element element, Point p, Rectangle rect)
    {
        if (!element.isSplit()) {
            if (rect.contains(p)) {
                return new ElementAndRect(element, rect);
            } else {
                return null;
            }
        }

        List<Element> children = element.children();

        for (Element child : children) {
            Rectangle childRect = getRectangle(child);
            if (childRect.contains(p)) {
                return findElementFromPoint(child, p, childRect);
            }
        }

        return null;
    }

    public ElementAndRect findElementFromPoint(Point p)
    {
        Page page = findPageFromPoint(p);
        if (page == null) {
            return null;
        }

        Rectangle pageRect = null;
        if (page == firstPage) {
            pageRect = firstPageRect;
        } else if (page == secondPage) {
            pageRect = secondPageRect;
        } else {
            throw new IllegalStateException("Page is neither first nor second.");
        }

        Element root = page.getRootElement();
        return findElementFromPoint(root, p, pageRect);
    }

}
