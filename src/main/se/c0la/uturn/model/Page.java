package se.c0la.uturn.model;

public class Page
{
    private PagePlan plan = null;
    private Element rootElement = null;
    private boolean isSpread;

    public Page(PagePlan plan)
    {
        this(plan, true);
    }

    public Page(PagePlan plan, boolean isSpread)
    {
        this.plan = plan;
        this.rootElement = new Element(this, null);
        this.isSpread = isSpread;
    }

    public PagePlan getPagePlan()
    {
        return plan;
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
