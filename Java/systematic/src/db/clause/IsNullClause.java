package db.clause;

import java.sql.*;
import java.util.*;

import db.*;

public class IsNullClause extends Clause {
    private static final long serialVersionUID = 1L;

	private final Column<?> column;
	private final boolean requireNull;

	public IsNullClause(Column<?> column, boolean requireNull) {
		this.column = column;
		this.requireNull = requireNull;
	}

	@Override public int bindInto(int n, PreparedStatement st) throws SQLException {
		return n;
	}

	@Override public void collectJoins(Set<JoinClause<?>> joins) {}

	@Override public void collectTables(Set<Table> tables) {
		column.collectTables(tables);
	}

	@Override public String toSql(String prefix, Set<Table> alreadyUsed) {
		return prefix + column.asSelect() + " is " + (!requireNull ? "not " : "") + "null";
	}

}
