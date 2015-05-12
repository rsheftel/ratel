package db;

import java.sql.*;
import java.util.*;

import db.clause.*;

public class Update implements Executable {

	private final Table table;
	private final Row replacements;
	private final Clause matches;
	private final boolean expectOne;

	public Update(Table table, Row replacements, Clause matches, boolean expectOne) {
		this.table = table;
		this.replacements = replacements;
		this.expectOne = expectOne;
		this.matches = matches;
	}
	
	private Set<Table> collectTables(Clause maybeFiltered) {
		Set<Table> tables = maybeFiltered.collectTables();
		replacements.collectTables(tables);
		table.collectTables(tables);
		return tables;
	}

	@Override
	public int bindInto(int n, LoggingPreparedStatement st) throws SQLException {
		n = replacements.bindInto(n, st);
		n = matches.bindInto(n, st);
		return n;
	}

	@Override
	public void execute() {
		Db.execute(this, expectOne);
	}

	@Override
	public String toSql() {
		Set<Table> tables = collectTables(matches);
		return 
		    "\nupdate \n    " + table.name() + 
		    "\nset " + replacements.asUpdate("\n   ") +
		    "\nfrom " + Table.from("\n    ", tables) +
		    "\nwhere " + matches.toSql("\n    ", new HashSet<Table>());
	}

}
