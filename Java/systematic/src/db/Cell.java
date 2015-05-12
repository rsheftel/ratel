package db;
import static util.Objects.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import db.clause.*;

public class Cell<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    protected final Column<T> column;
	private final T value;

	Cell(Column<T> column, T value) {
		this.column = column;
		this.value = value;
	}

	public String string() {
		return column.string(value);
	}

	public void putInto(Row row) {
		row.put(column, this);
	}

	public T value() {
		return value;
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")	@Override 
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Cell<T> other = (Cell<T>) obj;
		if (column == null) {
			if (other.column != null) return false;
		} else if (!column.equals(other.column))
			return false;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public static List<Column<?>> columns(Collection<Cell<?>> values) {
		List<Column<?>> result = empty();
		for (Cell<?> cell : values) 
			result.add(cell.column);
		return result;
	}

	public int bindInto(int n, LoggingPreparedStatement st) throws SQLException {
		return column.bindInto(n, st, value);
	}

	public String asAssignmentString() {
		return column.name() + " = ?";
	}
	
	@Override public String toString() {
		return column.name() + " = " + value;
	}

	public Clause matches() {
		return column.is(value);
	}
	
}
