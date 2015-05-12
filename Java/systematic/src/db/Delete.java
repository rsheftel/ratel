package db;

import java.sql.*;
import java.util.*;

import db.clause.*;

public class Delete implements Executable {

	private final Table table;
	private final Clause matches;
	private final boolean expectOne;

	public Delete(Table table, Clause matches, boolean expectOne) {
		this.table = table;
		this.matches = matches;
		this.expectOne = expectOne;
	}
	
	private Set<Table> collectTables(Clause maybeFiltered) {
		Set<Table> tables = maybeFiltered.collectTables();
		table.collectTables(tables);
		return tables;
	}

	public void execute() {
		Db.execute(this, expectOne);
	}

	@Override
	public int bindInto(int n, LoggingPreparedStatement st) throws SQLException {
		return matches.bindInto(n, st);
	}

	@Override
	public String toSql() {
		Set<Table> tables = collectTables(matches);
		String delete = "\ndelete\n    " + table.aliased() + "\nfrom " + Table.from("\n    ", tables);
		String where = matches.toSql("\n    ", new HashSet<Table>());
		return delete + "\nwhere " + where;
	}

}
