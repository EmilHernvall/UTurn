package se.c0la.uturn.component;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.util.List;
import java.awt.Rectangle;
import java.awt.Dimension;

import se.c0la.uturn.model.*;

public class PageCellRenderer
    extends JComponent
    implements ListCellRenderer<Page>
{
    private ElementMeasure measure;
    private int elementDepth;
    private boolean selected;

    public PageCellRenderer()
    {
        elementDepth = 0;
        selected = false;
        measure = new ElementMeasure(5,
            new ElementMeasure.DimensionCallback() {
                public int getWidth() { return 120; }
                public int getHeight() { return 75; }
            });
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Page> list,
                                                  Page page,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus)
    {
        setPreferredSize(new Dimension(list.getWidth(), 75));
        this.selected = isSelected;

        PagePlan plan = page.getPagePlan();
        int pageIdx = plan.getPageIndex(page);
        measure.updateState(plan, pageIdx);
        repaint(0xCAFEBABE, 0, 0, getWidth(), getHeight());

        return this;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        Graphics2D ctx = (Graphics2D)g;

        if (selected) {
            ctx.setColor(Color.BLUE);
            ctx.fillRect(1, 1, getWidth()-2, getHeight()-2);
        }

        if (measure.getFirstPage() == null) {
            return;
        }

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
        ctx.fillRect(rect.x+1, rect.y+1, rect.width-2, rect.height-2);

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
}
