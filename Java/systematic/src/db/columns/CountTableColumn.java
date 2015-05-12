package db.columns;


import java.util.*;

import db.*;

public class CountTableColumn extends SyntheticColumn<Integer> {
    private static final long serialVersionUID = 1L;

	private final Table table;

	public CountTableColumn(Table table) {
		super("cnt", "int", Table.NOT_NULL, table.aliased() + ":_cnt"); 
		this.table = table;
	}
	
	@Override public String asSql() {
		return "count(*)";
	}

	@Override public String asSelect() {
		return asSql() + " as " + name();
	}

	@Override public void collectTables(Set<Table> tables) {
		table.collectTables(tables);
	}
	
	@Override public String string(Integer t) {
	    return String.valueOf(t);
	}
}
