package com.znaptag.ui;

import java.util.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.Point;

import com.znaptag.ui.event.TableListener;
import com.znaptag.ui.event.TableRowSelectedEvent;
import com.znaptag.ui.event.TableTriggerReorderEvent;
import com.znaptag.ui.event.TableTriggerPopupEvent;

public class TableWrapper extends MouseAdapter implements HasTableEvents
{
    private JTable table;
    private JTableHeader header;
    private List<TableListener> listeners;

    private Point pressPoint = null;

    public TableWrapper(JTable _table)
    {
        this.table = _table;
        this.header = table.getTableHeader();
        this.listeners = new ArrayList<TableListener>();

        table.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e)
                {
                    int row = table.getSelectedRow();
                    for (TableListener listener : listeners) {
                        listener.rowSelected(new TableRowSelectedEvent(row));
                    }
                }
            });
        header.addMouseListener(this);
    }

    @Override
    public void addTableListener(TableListener tl)
    {
        listeners.add(tl);
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        pressPoint = new Point(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        if (e.getButton() == MouseEvent.BUTTON1) {
            triggerReorder(e);
        } else /*if (e.getButton() == MouseEvent.BUTTON2)*/ {
            triggerPopup(e);
        }
    }

    private void triggerReorder(MouseEvent e)
    {
        Point mousePoint = new Point(e.getX(), e.getY());
        if (!mousePoint.equals(pressPoint)) {
            return;
        }

        int column = header.columnAtPoint(mousePoint);

        TableModel tableModel = table.getModel();
        String name = tableModel.getColumnName(column);

        for (TableListener listener : listeners) {
            listener.triggerReorder(new TableTriggerReorderEvent(name));
        }
    }

    private void triggerPopup(MouseEvent e)
    {
        Point mousePoint = new Point(e.getX(), e.getY());
        for (TableListener listener : listeners) {
            listener.triggerPopup(new TableTriggerPopupEvent((java.awt.Component)e.getSource(), mousePoint));
        }
    }
}
