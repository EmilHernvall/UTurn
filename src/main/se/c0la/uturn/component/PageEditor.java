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
import java.util.List;
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

    private final static int MARGIN = 10;

    private PagePlan plan;
    private int pageIdx = 1;

    int elementDepth = 0;

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

    /*private Rectangle calculateElementRect(Element element, Rectangle pageRect)
    {
        // Put the hierarchy in a stack so we can descend rather than ascend
        Stack<Element> stack = new Stack<Element>();
        Element current = element;
        while (current != null) {
            stack.push(current);
            current = element.getParent();
        }

        Element parent = null;
        Rectangle currentRect = pageRect;
        while (stack.size() > 0) {
            current = stack.pop();
            if (parent != null) {
                Element.SplitAxis axis = current.getSplitAxis();
                int width, height;
                int stepLeft, stepTop;
                if (axis == Element.SplitAxis.HORIZONTAL) {
                    width = currentRect.width / current.getChildCount();
                    height = currentRect.height;
                    stepLeft = width;
                    stepTop = 0;
                } else if (axis == Element.SplitAxis.VERTICAL) {
                    width = currentRect.width;
                    height = currentRect.height / current.getChildCount();
                    stepLeft = 0;
                    stepTop = height;
                } else {
                    throw new IllegalStateException();
                }

                int left = currentRect.x, top = currentRect.y;
                int idx = parent.getElementIndex(current);
                left += idx * stepLeft;
                top += idx * stepTop;

                currentRect = new Rectangle(left, top, width, height);
            }
            parent = current;
        }

        return currentRect;
    }*/

    @Override
    public void paintComponent(Graphics g)
    {
        Graphics2D ctx = (Graphics2D)g;

        Page page = plan.getPage(pageIdx);
        if (page.isSpread()) {
            Page otherHalf = plan.getOtherHalfOfSpread(page);
            drawPage(ctx, page, calculatePageRect(2, 0));
            drawPage(ctx, otherHalf, calculatePageRect(2, 1));
        }
        else {
            drawPage(ctx, page, calculatePageRect(1, 0));
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

        System.out.println("draw rect: " + rect +
                           ", depth=" + elementDepth +
                           ", color=" + color);

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
                drawElement(ctx, child, childRect);
                left += stepLeft;
                top += stepTop;
            }
        }

        this.elementDepth -= 1;
    }

    private Page findPageFromPoint(Point p)
    {
        Page page = plan.getPage(pageIdx);
        if (page.isSpread()) {
            Page otherHalf = plan.getOtherHalfOfSpread(page);
            Rectangle first = calculatePageRect(2, 0);
            Rectangle second = calculatePageRect(2, 1);

            if (first.contains(p)) {
                return page;
            }
            else if (second.contains(p)) {
                return otherHalf;
            }
        }
        else {
            Rectangle rect = calculatePageRect(1, 0);
            if (rect.contains(p)) {
                return page;
            }
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

        Element.SplitAxis axis = element.getSplitAxis();
        List<Element> children = element.children();

        int left = rect.x, top = rect.y;
        for (Element child : children) {
            int stepLeft, stepTop;
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
            if (childRect.contains(p)) {
                return findElementFromPoint(child, p, childRect);
            }
            left += stepLeft;
            top += stepTop;
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
        if (page.isSpread()) {
            int idx = plan.getPageIndex(page);
            System.out.println("page index: " + idx);
            if (idx == this.pageIdx) {
                pageRect = calculatePageRect(2, 0);
            } else {
                pageRect = calculatePageRect(2, 1);
            }
        } else {
            pageRect = calculatePageRect(1, 0);
        }

        System.out.println("rect: " + pageRect);

        Element root = page.getRootElement();
        return findElementFromPoint(root, p, pageRect);
    }

    public void update()
    {
        repaint(0xCAFEBABE, 0, 0, getWidth(), getHeight());
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        Point p = e.getPoint();
        ElementAndRect match = findElementFromPoint(p);
        if (match != null) {
            Element elMatch = match.element;
            Element parent = elMatch.getParent();
            Rectangle rect = match.rect;

            boolean drag = false;
            if (parent != null) {
                int minX = (int)rect.getMinX(), maxX = (int)rect.getMaxX();
                int minY = (int)rect.getMinY(), maxY = (int)rect.getMaxY();
                int d = 10;
                if (parent.getSplitAxis() == Element.SplitAxis.VERTICAL) {
                    if (Math.abs(p.y - minY) < d) {
                        System.out.println("top drag");
                        setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
                        drag = true;
                    }
                    else if (Math.abs(p.y - maxY) < d) {
                        System.out.println("bottom drag");
                        setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
                        drag = true;
                    }
                }
                else if (parent.getSplitAxis() == Element.SplitAxis.HORIZONTAL) {
                    if (Math.abs(p.x - minX) < d) {
                        System.out.println("left drag");
                        setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
                        drag = true;
                    }
                    else if (Math.abs(p.x - maxX) < d) {
                        System.out.println("right drag");
                        setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
                        drag = true;
                    }
                }
            }

            if (!drag) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
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
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void addEditorListener(EditorListener listener)
    {
        listeners.add(listener);
    }
}
