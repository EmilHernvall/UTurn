package se.c0la.uturn.component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.JComponent;

import se.c0la.uturn.model.*;

public class PageEditor
    extends JComponent
    implements MouseListener, MouseMotionListener, HasEditorEvents
{
    private static class ElementAndRect
    {
        Element element;
        Rectangle rect;

        public ElementAndRect(Element element, Rectangle rect)
        {
            this.element = element;
            this.rect = rect;
        }
    }

    public enum DragDirection
    {
        TOP(new Cursor(Cursor.N_RESIZE_CURSOR), false),
        BOTTOM(new Cursor(Cursor.S_RESIZE_CURSOR), true),
        LEFT(new Cursor(Cursor.W_RESIZE_CURSOR), false),
        RIGHT(new Cursor(Cursor.E_RESIZE_CURSOR), true);

        Cursor cursor;
        boolean farther;

        private DragDirection(Cursor c, boolean farther)
        {
            this.cursor = c;
            this.farther = farther;
        }
    }

    private final static int MARGIN = 10;

    private PagePlan plan;
    private int pageIdx = 1;

    // all of these are managed by updateState()
    private Page firstPage = null;
    private Page secondPage = null;
    private Rectangle firstPageRect = null;
    private Rectangle secondPageRect = null;
    private Map<Element, Rectangle> elementRects = null;

    int elementDepth = 0;

    private Element dragElement = null;
    private DragDirection dragDir = null;
    private int originalSize = 0;
    private Point dragStart = null;
    private Rectangle dragRect = null;
    private Rectangle dragParentRect = null;

    private List<EditorListener> listeners;

    public PageEditor()
    {
        this.listeners = new ArrayList<EditorListener>();

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void setPagePlan(PagePlan plan)
    {
        this.plan = plan;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        if (firstPage == null) {
            updateState();
        }

        Graphics2D ctx = (Graphics2D)g;

        drawPage(ctx, firstPage, firstPageRect);
        if (secondPage != null) {
            drawPage(ctx, secondPage, secondPageRect);
        }
    }

    private void drawPage(Graphics2D ctx, Page page, Rectangle rect)
    {
        ctx.setColor(Color.WHITE);
        ctx.fillRect(rect.x, rect.y, rect.width, rect.height);

        drawElement(ctx, page.getRootElement(), rect);
    }

    private void drawElement(Graphics2D ctx, Element element, Rectangle rect)
    {
        this.elementDepth += 1;

        int color = 0x80 + 0x10 * elementDepth;
        ctx.setColor(new Color(color, color, color));
        ctx.fillRect(rect.x+5, rect.y+5, rect.width-10, rect.height-10);

        ctx.setColor(Color.BLACK);
        ctx.drawRect(rect.x, rect.y, rect.width, rect.height);

        System.out.println("draw rect: " + rect +
                           ", depth=" + elementDepth +
                           ", color=" + color);

        if (element.isSplit()) {
            List<Element> children = element.children();
            for (Element child : children) {
                Rectangle childRect = elementRects.get(child);
                drawElement(ctx, child, childRect);
            }
        }

        this.elementDepth -= 1;
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
            Rectangle childRect = elementRects.get(child);
            if (childRect.contains(p)) {
                return findElementFromPoint(child, p, childRect);
            }
        }

        return null;
    }

    private ElementAndRect findElementFromPoint(Point p)
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

    public void update()
    {
        updateState();
        repaint(0xCAFEBABE, 0, 0, getWidth(), getHeight());
    }

    private Rectangle calculatePageRect(int num, int pos)
    {
        int height = getHeight() - 2*MARGIN;
        int width = (int)(plan.getPageRatio() * height);

        int top = MARGIN;
        int left = (getWidth() - num*width - (num-1)*MARGIN)/2;
        for (int i = 0; i < pos; i++) {
            left += width;
            left += MARGIN;
        }

        return new Rectangle(left, top, width, height);
    }

    private void updateState()
    {
        elementRects = new HashMap<Element, Rectangle>();

        firstPage = plan.getPage(pageIdx);

        if (firstPage.isSpread()) {
            secondPage = plan.getOtherHalfOfSpread(firstPage);

            firstPageRect = calculatePageRect(2, 0);
            secondPageRect = calculatePageRect(2, 1);

            updateElement(firstPage.getRootElement(), firstPageRect);
            updateElement(secondPage.getRootElement(), secondPageRect);
        } else {
            firstPageRect = calculatePageRect(1, 0);
            updateElement(firstPage.getRootElement(), firstPageRect);
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

    private DragDirection classifyDrag(Element.SplitAxis axis,
                                       Point p,
                                       Rectangle rect)
    {
        int minX = (int)rect.getMinX(), maxX = (int)rect.getMaxX();
        int minY = (int)rect.getMinY(), maxY = (int)rect.getMaxY();
        int d = 10;
        if (axis == Element.SplitAxis.VERTICAL) {
            if (Math.abs(p.y - minY) < d) {
                return DragDirection.TOP;
            }
            else if (Math.abs(p.y - maxY) < d) {
                return DragDirection.BOTTOM;
            }
        }
        else if (axis == Element.SplitAxis.HORIZONTAL) {
            if (Math.abs(p.x - minX) < d) {
                return DragDirection.LEFT;
            }
            else if (Math.abs(p.x - maxX) < d) {
                return DragDirection.RIGHT;
            }
        }

        return null;
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        Point p = e.getPoint();
        ElementAndRect match = findElementFromPoint(p);
        if (match == null) {
            return;
        }

        Element elMatch = match.element;
        Element parent = elMatch.getParent();
        Rectangle rect = match.rect;

        boolean drag = false;
        if (parent == null) {
            return;
        }

        DragDirection dir = classifyDrag(parent.getSplitAxis(), p, rect);
        if (dir != null) {
            setCursor(dir.cursor);
        } else {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        Point p = e.getPoint();
        ElementAndRect match = findElementFromPoint(p);
        if (match == null) {
            return;
        }

        Element elMatch = match.element;
        Element parent = elMatch.getParent();
        Rectangle rect = match.rect;

        if (parent == null) {
            return;
        }

        DragDirection dir = classifyDrag(parent.getSplitAxis(), p, rect);
        if (dir == null) {
            return;
        }

        dragElement = elMatch;
        dragDir = dir;
        originalSize = elMatch.getMySize();
        dragStart = p;
        dragRect = elementRects.get(elMatch);
        dragParentRect = elementRects.get(elMatch.getParent());
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        if (dragElement == null) {
            e.consume();
            return;
        }

        Point p = e.getPoint();

        System.out.println("drag: " + e.getPoint() +
                           ", dir=" + dragDir +
                           ", rect=" + dragRect +
                           ", parent=" + dragParentRect);

        int delta = 0;
        int size = 0;
        int parentSize = 0;
        switch (dragDir) {
            case TOP:
                delta = (int)(dragStart.getY() - p.getY());
                size = dragRect.height;
                parentSize = dragParentRect.height;
                break;
            case BOTTOM:
                delta = -(int)(dragStart.getY() - p.getY());
                size = dragRect.height;
                parentSize = dragParentRect.height;
                break;
            case LEFT:
                delta = (int)(dragStart.getX() - p.getX());
                size = dragRect.width;
                parentSize = dragParentRect.width;
                break;
            case RIGHT:
                delta = -(int)(dragStart.getX() - p.getX());
                size = dragRect.width;
                parentSize = dragParentRect.width;
                break;
        }

        if (delta == 0) {
            return;
        }

        float fraction = (size + delta) / (float)parentSize;

        System.out.println("fraction: " + fraction);

        Element parent = dragElement.getParent();
        parent.setSizeFraction(dragElement, dragDir.farther, fraction);
        update();
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        System.out.println("released");
        dragElement = null;
        dragDir = null;
        originalSize = 0;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        Point p = e.getPoint();
        ElementAndRect match = findElementFromPoint(p);
        if (match != null) {
            Element elMatch = match.element;
            for (EditorListener listener : listeners) {
                listener.onElementClicked(elMatch, e.getLocationOnScreen());
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void addEditorListener(EditorListener listener)
    {
        listeners.add(listener);
    }
}
