package com.znaptag.ui;

import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

public class FlatSplitPaneUI extends BasicSplitPaneUI
{
    @Override
    public BasicSplitPaneDivider createDefaultDivider()
    {
        return new BasicSplitPaneDivider(this) {
                public void setBorder(Border b) {}
            };
    }
}
