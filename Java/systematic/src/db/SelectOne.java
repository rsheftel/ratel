package db;

import static util.Objects.*;

import java.util.*;

import db.clause.*;

public class SelectOne<T> extends Select {
    private static final long serialVersionUID = 1L;

	private final Column<T> column;

	public SelectOne(Column<T> column, Clause clause, boolean distinct) {
		super(clause, distinct);
		this.column = column;
	}

	public static <T> SelectOne<T> select(Column<T> column, Clause clause) {
		return new SelectOne<T>(column, clause, false);
	}

	public T value() {
		return Db.value(this);
	}

	public boolean typeMatches(String typeRegex) {
		return column.typeMatches(typeRegex);
	}

	@Override protected void collectTables(Set<Table> tables) {
		column.collectTables(tables);
	}

	@Override protected String selectColumnsString(String prefix) {
		return prefix + column.asSelect();
	}

	public T value(Row r) {
		return r.value(column);
	}

	@Override public List<Column<?>> columns() {
		List<Column<?>> result = empty();
		result.add(column);
		return result;
	}

	public List<T> values() {
		return Db.values(this);
	}

	public List<T> values(List<Row> rows) {
		List<T> result = empty();
		for (Row r : rows) 
			result.add(value(r));
		return result ;
	}

	public int count() { 
	    return column.countDistinctColumn().value(super.clause);
	}
	
}
