package com.fftw.fix.ui.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import quickfix.FieldMap;
import quickfix.Message;

public class FixMessageTableModel extends AbstractTableModel
{

    private List<Message> rowData = Collections.<Message>emptyList();

    public FixMessageTableModel (List<Message> rowData)
    {
        this.rowData = new ArrayList<Message>(rowData);
    }

    public int getColumnCount ()
    {
        return 3;
    }

    public int getRowCount ()
    {
        return rowData.size();
    }

    public Object getValueAt (int rowIndex, int columnIndex)
    {
        final Message row = rowData.get(rowIndex);

        TmpMessage tm = null;
        switch (columnIndex)
        {
            case 0:
                tm = new TmpMessage(row.getHeader());
                return tm.toString();
            case 1:
                tm = new TmpMessage(row);
                return tm.toString();
            case 2:
                tm = new TmpMessage(row.getTrailer());
                return tm.toString();
            default:
                return null;
        }

    }

    public Message getRowItem(int row) {
        return rowData.get(row);
    }
    
    private static class TmpMessage extends FieldMap
    {
        public TmpMessage (FieldMap orig)
        {
            initializeFrom(orig);
        }

        public String toString ()
        {
            StringBuffer sb = new StringBuffer(128);

            calculateString(sb, new int[0], new int[0]);
            return sb.toString();
        }
    }

    @Override
    public String getColumnName (int column)
    {
        switch (column)
        {
            case 0:
                return "Header";
            case 1:
                return "Body";
            case 2:
                return "Trailer";
            default:
                return "??";
        }
    }
}
