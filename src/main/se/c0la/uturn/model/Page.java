package se.c0la.uturn.model;

import org.json.*;

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

    public JSONObject toJson()
    throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put("spread", isSpread);
        obj.put("rootElement", rootElement.toJson());

        return obj;
    }

    public static Page fromJson(JSONObject obj, PagePlan plan)
    throws JSONException
    {
        Page page = new Page(plan);
        page.isSpread = obj.getBoolean("spread");
        page.rootElement =
            Element.fromJson(obj.getJSONObject("rootElement"), page, null);
        return page;
    }
}
