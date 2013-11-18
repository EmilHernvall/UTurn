package se.c0la.uturn.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.Color;

import org.json.*;

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
    private Color color = null;

    public Element(Page page, Element parent)
    {
        this.page = page;
        this.parent = parent;
    }

    public String getContent() { return content; }
    public Color getColor() { return color; }

    public void setContent(String v)
    {
        this.content = v;

        PagePlan plan = page.getPagePlan();
        plan.firePageChanged(page);
    }

    public void setColor(Color v)
    {
        this.color = v;

        PagePlan plan = page.getPagePlan();
        plan.firePageChanged(page);
    }

    public Element getParent() { return parent; }
    public boolean isRoot() { return parent == null; }
    public boolean isSplit() { return elements != null; }
    public SplitAxis getSplitAxis() { return axis; }

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

    public JSONObject toJson()
    throws JSONException
    {
        JSONObject obj = new JSONObject();

        JSONArray elementsArr = new JSONArray();
        if (elements != null) {
            for (Element element : elements) {
                elementsArr.put(element.toJson());
            }
        }

        obj.put("elements", elementsArr);

        JSONArray sizesArr = new JSONArray();
        if (sizes != null) {
            for (int size : sizes) {
                sizesArr.put(size);
            }
        }

        obj.put("sizes", sizesArr);
        obj.put("axis", axis != null ? axis.toString() : null);
        obj.put("content", content);
        obj.put("color", colorToHex(color));

        return obj;
    }

    private static String colorToHex(Color color)
    {
        if (color == null) {
            return "FFFFFF";
        }

        return String.format("%02X%02X%02X",
                             color.getRed(),
                             color.getGreen(),
                             color.getBlue());
    }

    public static Element fromJson(JSONObject obj, Page page, Element parent)
    throws JSONException
    {
        Element element = new Element(page, parent);

        if (!obj.isNull("axis")) {
            String axis = obj.getString("axis");
            if ("HORIZONTAL".equals(axis)) {
                element.axis = SplitAxis.HORIZONTAL;
            } else if ("VERTICAL".equals(axis)) {
                element.axis = SplitAxis.VERTICAL;
            } else {
                throw new RuntimeException();
            }

            JSONArray elementsArr = obj.getJSONArray("elements");
            element.elements = new ArrayList<Element>();
            for (int i = 0; i < elementsArr.length(); i++) {
                JSONObject elObj = elementsArr.getJSONObject(i);
                element.elements.add(Element.fromJson(elObj, page, element));
            }

            JSONArray sizesArr = obj.getJSONArray("sizes");
            element.sizes = new int[sizesArr.length()];
            for (int i  = 0; i < sizesArr.length(); i++) {
                element.sizes[i] = sizesArr.getInt(i);
            }
        }

        if (!obj.isNull("content")) {
            element.content = obj.getString("content");
        }

        if (!obj.isNull("color")) {
            String color = obj.getString("color");
            if (color != null && color.length() == 6) {
                int r = Integer.parseInt(color.substring(0, 2), 16);
                int g = Integer.parseInt(color.substring(2, 4), 16);
                int b = Integer.parseInt(color.substring(4, 6), 16);
                element.color = new Color(r, g, b);
            }
        }

        return element;
    }
}
