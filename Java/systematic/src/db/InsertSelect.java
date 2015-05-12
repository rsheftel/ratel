package db;

import java.sql.*;

public class InsertSelect implements Executable {

	private final SelectMultiple select;
	private final Table table;

	public InsertSelect(Table table, SelectMultiple select) {
		this.table = table;
		this.select = select;
	}

	@Override
	public int bindInto(int n, LoggingPreparedStatement st) throws SQLException {
		return select.bindInto(n, st);
	}

	@Override
	public void execute() {
		Db.execute(this, false);
	}

	@Override
	public String toSql() {
		return "insert into " + table.name() + select.toSql("\n    ");
	}

}
