package db;

import static util.Objects.*;

import java.util.*;

import db.clause.*;
import file.*;

public class SelectMultiple extends Select {
    private static final long serialVersionUID = 1L;

	private final List<Column<?>> columns;

	public SelectMultiple(List<Column<?>> columns, Clause match) {
		this(columns, match, false);
	}
	
	public SelectMultiple(List<Column<?>> columns, Clause match, boolean distinct) {
		super(match, distinct);
		this.columns = columns;
	}

	@Override protected void collectTables(Set<Table> tables) {
		for (Column<?> column : columns()) column.collectTables(tables);
	}
	
	@Override protected String selectColumnsString(String prefix) {
		return Column.asSelect(prefix, columns);
	}

	@Override public List<Column<?>> columns() {
		return columns;
	}
	
	public void add(Column<?> c) {
		columns.add(c);
	}

	public Csv asCsv() {
		Csv result = new Csv(true);
		result.add(Column.names(columns()));
		for (Row r : rows()) 
			result.add(r.strings(columns()));
		return result;
	}

	public String asCsvText() {
		return asCsv().asText();
	}

    public <K, V> Map<K, V> asMap(Column<K> key, Column<V> value) {
        Map<K, V> result = emptyMap();
        for(Row r : rows()) result.put(r.value(key), r.value(value));
        return result;
    }
	


}
