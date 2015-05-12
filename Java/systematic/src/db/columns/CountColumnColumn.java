package db.columns;


import java.util.*;

import db.*;

public class CountColumnColumn extends SyntheticColumn<Integer> {
    private static final long serialVersionUID = 1L;

    private final Column<?> column;
    private final boolean distinct;

	public CountColumnColumn(Column<?> column, boolean distinct) {
		super("cnt_" + column.name(), "int", Table.NOT_NULL, "cnt_" + column.identity());
        this.column = column;
        this.distinct = distinct; 
	}
	
	@Override public String asSql() {
		return "count(" + (distinct ? "distinct " : "") + column.leftHandSide() + ")";
	}

	@Override public String asSelect() {
		return asSql() + " as " + name();
	}

	@Override public void collectTables(Set<Table> tables) {
		column.collectTables(tables);
	}
	
	@Override public String string(Integer t) {
	    return String.valueOf(t);
	}
}
