package activemq.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class TableUtils {

    public static int columnHeaderWidth(JTable table, TableColumn column) {
        TableCellRenderer renderer = column.getHeaderRenderer();
        Component comp = renderer.getTableCellRendererComponent(table, column.getHeaderValue(), false, false,
                0, 0);

        return comp.getPreferredSize().width;
    }

    public static int widestCellInColumn(JTable table, TableColumn column) {
        int columnIndex = column.getModelIndex();
        int width = 0;
        int maxWidth = 0;

        for (int i = 0; i < table.getRowCount(); ++i) {
            TableCellRenderer renderer = table.getCellRenderer(i, columnIndex);
            Component comp = renderer.getTableCellRendererComponent(table, table.getValueAt(i, columnIndex),
                    false, false, i, columnIndex);
            width = comp.getPreferredSize().width;
            maxWidth = width > maxWidth ? width : maxWidth;
        }

        return maxWidth;
    }
}
