package activemq.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class TableUtils {

    public static int columnHeaderWidth(JTable table, TableColumn column) {
        TableCellRenderer renderer = column.getHeaderRenderer();
        Component comp = renderer.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, 0, 0);

        return comp.getPreferredSize().width;
    }

    public static int widestCellInColumn(JTable table, TableColumn column) {
        int columnIndex = column.getModelIndex();
        int width = 0;
        int maxWidth = 0;

        for (int i = 0; i < table.getRowCount(); ++i) {
            TableCellRenderer renderer = table.getCellRenderer(i, columnIndex);
            Component comp = renderer.getTableCellRendererComponent(table, table.getValueAt(i, columnIndex), false, false, i, columnIndex);
            width = comp.getPreferredSize().width;
            maxWidth = width > maxWidth ? width : maxWidth;
        }

        return maxWidth;
    }
    
    public static int autoFitColumns(JTable table) {
        TableColumnModel cm = table.getColumnModel();
        for (int i = 0; i < cm.getColumnCount(); i++) {
            TableColumn currentColumn = cm.getColumn(i);
            currentColumn.setMinWidth(widestCellInColumn(table, currentColumn));
        }
        
        return cm.getColumnCount();
    }
    
    public static int setColumnPreferredWidths(JTable table, int columns[], int widths[]) {
        TableColumnModel cm = table.getColumnModel();
        for (int i = 0; i < columns.length; i++) {
            TableColumn currentColumn = cm.getColumn(columns[i]);
            currentColumn.setPreferredWidth(widths[i]);
        }

        return columns.length;
    }
}
