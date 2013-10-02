package se.c0la.uturn.model;

public class Page
{
    private Element rootElement = null;
    private boolean isSpread;

    public Page()
    {
        this(true);
    }

    public Page(boolean isSpread)
    {
        this.rootElement = new Element(null);
        this.isSpread = isSpread;
    }

    public Element getRootElement()
    {
        return rootElement;
    }

    public boolean isSpread()
    {
        return isSpread;
    }
}
