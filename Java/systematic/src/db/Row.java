package db;
import static db.clause.Clause.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Map.*;

import util.*;
import db.clause.*;

public class Row implements Serializable {
    private static final long serialVersionUID = 1L;

    public Row() { }

	public Row(Row r) { 
		this.data.putAll(r.data);
	}
	
	public Row(Cell<?> ... cells) {
		this(list(cells));
	}

	public Row(List<Cell<?>> cells) {
		for (Cell<?> cell : cells)
			cell.putInto(this);		
	}

	private final Map<Column<?>, Cell<?>> data = new HashMap<Column<?>, Cell<?>>();

	public String asUpdate(String prefix) {
		List<String> setStrings = empty();
		for (Cell<?> cell : data.values())
			setStrings.add(prefix + cell.asAssignmentString());
		return join(", ", setStrings);
	}

	public <T> void put(Column<T> c, Cell<T> value) {
		data.put(c, value);
	}
	
	public <T> void put(Cell<T> cell) {
		cell.putInto(this);
	}
	
	@Override public String toString() {
		return toSring(data);
	}

	public static String toSring(Map<Column<?>, Cell<?>> map) {
		StringBuilder b = new StringBuilder();
		b.append("{\n");
		for (Entry<Column<?>, Cell<?>> entry : map.entrySet()) { 
			Column<?> column = entry.getKey();
			b.append("\t" + column + " = " + entry.getValue().string() + "\n");
		}
		b.append("}\n");
		return b.toString();
	}

	public <T> String string(Column<T> c) {
		requireKey(c);
		Cell<?> value = data.get(c);
		String result = value.string();
        if (result == null)
		    throw bomb("value of " + c + " in \n" + this + "\n was null!");
		return result;
	}

	private <T> void requireKey(Column<T> c) {
	    if (!data.containsKey(c)) throw bomb("no column " + c + "in " + this);
	}
	
	public <T> T value(Column<T> c) {
		return c.value(data);
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Row other = (Row) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}

	public Collection<Cell<?>> cells() {
		return data.values();
	}

	public Set<Column<?>> columns() {
		return data.keySet();
	}

	public int bindInto(int n, LoggingPreparedStatement st) throws SQLException {
		return bindInto(n, st, this);
	}

	public int bindInto(int n, LoggingPreparedStatement st, Row example) throws SQLException {
		for (Column<?> col : example.columns())
			n = data.get(col).bindInto(n, st);
		return n;
	}

	public void collectTables(Set<Table> tables) {
		for (Column<?> col : columns())
			col.collectTables(tables);
	}

	public Clause allMatch(Column<?> ... columns) {
	    return allMatch(list(columns));
	}
	
	public Clause allMatch(Collection<Column<?>> keys) {
	    if (keys == null) return allMatch();
	    return subMap(keys).allMatch();
	}

	public Row subMap(Collection<Column<?>> keys) {
	    Row result = new Row();
	    for (Column<?> column : keys) 
            result.put(data.get(column));
        return result;
    }

    public List<String> strings(List<Column<?>> columns) {
		List<String> result = empty();
		for (Column<?> c : columns) 
			result.add(string(c));
		return result;
	}

	public void refresh(Row newData) {
		data.clear();
		data.putAll(newData.data);
	}
	
	public boolean isEmpty(Column<?> c) {
	    return value(c) == null || Strings.isEmpty(string(c));
	}

    public Clause allMatch() {
        Clause result = TRUE;
        for (Cell<?> cell : cells()) 
            result = result.and(cell.matches());
        return result;
    }
	
	
}
