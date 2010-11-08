package org.ratel.tsdb;

import org.ratel.db.tables.TSDB.*;

public class FinancialCenterTable extends FinancialCenterBase {
    private static final long serialVersionUID = 1L;
    public static final FinancialCenterTable CENTER = new FinancialCenterTable();
    
    public FinancialCenterTable() {
        super("center");
    }

    public void insert(int i, int id, String name) {
        insert(C_ID.with(i), C_TIME_SERIES_ID.with(id), C_NAME.with(name));
    }
    
    public String name(int id) {
        return C_NAME.value(C_ID.is(id));
    }
}
