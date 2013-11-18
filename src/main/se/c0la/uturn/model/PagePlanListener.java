package se.c0la.uturn.model;

public interface PagePlanListener
{
    public void onPageAdded(PagePlan plan, int pageIdx);
    public void onPageDeleted(PagePlan plan, int pageIdx);
    public void onPageChange(Page page);
}
