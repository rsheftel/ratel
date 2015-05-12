package activemq.ui.table;

import javax.swing.JTable;

@SuppressWarnings("serial")
public class PreferredSizeTable extends JTable {

    private boolean autoSized;

    public PreferredSizeTable(JmsMessageTableModel table) {
        super(table);
    }

    public void autoSize() {
        if (!autoSized) {
            TableUtils.autoFitColumns(this);
            //autoSized = true;
        }
        autoSized =false;
    }

    /* (non-Javadoc)
     * @see javax.swing.JTable#resizeAndRepaint()
     */
    @Override
    protected void resizeAndRepaint() {
        autoSize();
        super.resizeAndRepaint();
    }

}
