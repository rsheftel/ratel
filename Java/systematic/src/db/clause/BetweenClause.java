package db.clause;


import java.sql.*;
import java.util.*;

import db.*;

public class BetweenClause extends Clause {
    private static final long serialVersionUID = 1L;

	private final Column<Timestamp> column;
	private final Timestamp start;
	private final Timestamp end;

	public BetweenClause(Column<Timestamp> column, Timestamp start, Timestamp end) {
		this.column = column;
		this.start = start;
		this.end = end;
	}

	@Override
	public int bindInto(int n, PreparedStatement st) throws SQLException {
		st.setTimestamp(n++, start);
		st.setTimestamp(n++, end);
		return n;
	}

	@Override
	public String toSql(String prefix, Set<Table> alreadyUsed) {
		return prefix + column.asSql() + " between ? and ?";
	}
	
	@Override
	public void collectTables(Set<Table> tables) {
		column.collectTables(tables);
	}

	@Override
	public void collectJoins(Set<JoinClause<?>> joins) {}

}
