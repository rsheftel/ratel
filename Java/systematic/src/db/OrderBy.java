package db;

import java.io.*;


public class OrderBy implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Column<?> column;
    private final boolean isAscending;

    public OrderBy(Column<?> c, boolean isAscending) {
        this.column = c;
        this.isAscending = isAscending;
    }

    public String asSql() {
        return column.asSql() + (isAscending ? "" : " desc");
    }

}
