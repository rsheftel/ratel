package db;

import static java.util.Collections.*;
import static util.Strings.*;

import java.sql.*;
import java.util.*;

public class Insert implements Executable  {

	private final Table table;
	private final Row row;

	public Insert(Table table, Cell<?> ... values) {
		this(table, new Row(values));
	}

	public Insert(Table table, Row row) {
		this.table = table;
		this.row = row;
	}

	public void execute() {
		Db.execute(this, true);
	}

	public String toSql() {
		Set<Column<?>> columns = row.columns();
		String insert = "\ninsert into " + table.name();
		String columnsString = paren(Column.asInsert("\n    ", columns) + "\n");
		String valuesString = paren(join(", ", nCopies(columns.size(), "\n    ?")) + "\n");
		return insert + " " + columnsString + " values " + valuesString;
	}

	public int bindInto(int n, LoggingPreparedStatement st) throws SQLException {
		return row.bindInto(n, st);
	}

}
