package db.columns;


import java.util.*;

import db.*;

public class AliasColumn<T> extends SyntheticColumn<T> {
    private static final long serialVersionUID = 1L;

	private final String alias;
	private final Column<T> base;

	public AliasColumn(String alias, Column<T> base) {
		super(alias, base.type(), Table.NOT_NULL, base.identity() + "_" + alias);
		this.alias = alias;
		this.base = base;
	}
	
	@Override public String asSql() {
		return alias;
	}

	@Override public String asSelect() {
		return base.asSql() + " as " + alias;
	}

	@Override public void collectTables(Set<Table> tables) {
		base.collectTables(tables);
	}
	
	@Override public String string(T t) {
	    return base.string(t);
	}
}
