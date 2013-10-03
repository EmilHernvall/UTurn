package se.c0la.uturn.model;

import javax.swing.AbstractListModel;

public class PagePlanListModel
    extends AbstractListModel<Page>
    implements PagePlanListener
{
    private PagePlan plan;

    public PagePlanListModel(PagePlan plan)
    {
        this.plan = plan;

        plan.addListener(this);
    }

    @Override
    public Page getElementAt(int idx)
    {
        return plan.getSpread(idx);
    }

    @Override
    public int getSize()
    {
        return plan.getSpreadCount();
    }

    @Override
    public void onPageChange(Page pg)
    {
        int idx = plan.getSpreadIndex(pg);
        fireContentsChanged(this, idx, idx);
    }
}
