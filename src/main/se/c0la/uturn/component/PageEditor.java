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
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.JComponent;

import se.c0la.uturn.model.*;

public class PageEditor
    extends JComponent
    implements MouseListener,
               MouseMotionListener,
               ComponentListener,
               EditorState,
               ElementMeasure.DimensionCallback
{
    private static class ElementAndDir
    {
        Element element;
        DragDirection dir;

        public ElementAndDir(Element element, DragDirection dir)
        {
            this.element = element;
            this.dir = dir;
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

    // constants
    private final static int MARGIN = 10;

    // shared state
    private PagePlan plan;
    private int pageIdx = 0;
    private List<EditorListener> listeners;
    private ElementMeasure measure;

    // used when drawing
    int elementDepth = 0;

    // resize by drag state
    private Element dragElement = null;
    private DragDirection dragDir = null;
    private int originalSize = 0;
    private Point dragStart = null;
    private Rectangle dragRect = null;
    private Rectangle dragParentRect = null;

    public PageEditor()
    {
        this.listeners = new ArrayList<EditorListener>();
        this.measure = new ElementMeasure(MARGIN, this);

        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void setPagePlan(PagePlan plan)
    {
        this.plan = plan;
        update();
    }

    @Override
    public int getPageIndex()
    {
        return pageIdx;
    }

    @Override
    public void setPageIndex(int pageIdx)
    {
        this.pageIdx = pageIdx;
        update();
    }

    @Override
    public void addEditorListener(EditorListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        if (measure.getFirstPage() == null) {
            return;
        }

        Graphics2D ctx = (Graphics2D)g;

        drawPage(ctx, measure.getFirstPage(), measure.getFirstPageRect());
        if (measure.getSecondPage() != null) {
            drawPage(ctx, measure.getSecondPage(), measure.getSecondPageRect());
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

        if (element.isSplit()) {
            List<Element> children = element.children();
            for (Element child : children) {
                Rectangle childRect = measure.getRectangle(child);
                drawElement(ctx, child, childRect);
            }
        }

        this.elementDepth -= 1;
    }

    public void update()
    {
        if (plan == null) {
            return;
        }

        measure.updateState(plan, pageIdx);
        repaint(0xCAFEBABE, 0, 0, getWidth(), getHeight());
    }

    private ElementAndDir classifyDrag(Element element,
                                       Point p)
    {
        Element parent = element.getParent();
        if (parent == null) {
            return null;
        }

        Rectangle rect = measure.getRectangle(element);

        int minX = (int)rect.getMinX(), maxX = (int)rect.getMaxX();
        int minY = (int)rect.getMinY(), maxY = (int)rect.getMaxY();
        int d = 10;
        if (parent.getSplitAxis() == Element.SplitAxis.VERTICAL) {
            if (Math.abs(p.y - minY) < d) {
                return new ElementAndDir(element, DragDirection.TOP);
            }
            else if (Math.abs(p.y - maxY) < d) {
                return new ElementAndDir(element, DragDirection.BOTTOM);
            }
        }
        else if (parent.getSplitAxis() == Element.SplitAxis.HORIZONTAL) {
            if (Math.abs(p.x - minX) < d) {
                return new ElementAndDir(element, DragDirection.LEFT);
            }
            else if (Math.abs(p.x - maxX) < d) {
                return new ElementAndDir(element, DragDirection.RIGHT);
            }
        }

        return classifyDrag(parent, p);
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        Point p = e.getPoint();
        ElementMeasure.ElementAndRect match = measure.findElementFromPoint(p);
        if (match == null) {
            return;
        }

        Element elMatch = match.element;
        if (elMatch.getParent() == null) {
            return;
        }

        ElementAndDir result = classifyDrag(elMatch, p);
        if (result != null) {
            setCursor(result.dir.cursor);
        } else {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        Point p = e.getPoint();
        ElementMeasure.ElementAndRect match = measure.findElementFromPoint(p);
        if (match == null) {
            return;
        }

        Element elMatch = match.element;
        ElementAndDir result = classifyDrag(elMatch, p);
        if (result == null) {
            return;
        }

        if (result.element.getParent() == null) {
            return;
        }

        dragElement = result.element;
        dragDir = result.dir;
        originalSize = dragElement.getMySize();
        dragStart = p;
        dragRect = measure.getRectangle(dragElement);
        dragParentRect = measure.getRectangle(dragElement.getParent());
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        if (dragElement == null) {
            e.consume();
            return;
        }

        Point p = e.getPoint();

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

        Element parent = dragElement.getParent();
        parent.setSizeFraction(dragElement, dragDir.farther, fraction);
        update();
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        dragElement = null;
        dragDir = null;
        originalSize = 0;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        Point p = e.getPoint();
        ElementMeasure.ElementAndRect match = measure.findElementFromPoint(p);
        if (match != null) {
            Element elMatch = match.element;
            for (EditorListener listener : listeners) {
                listener.onElementClicked(elMatch, e.getLocationOnScreen());
            }
        }
    }

    @Override
    public void componentResized(ComponentEvent e)
    {
        update();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void componentHidden(ComponentEvent e) {}

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}
}
