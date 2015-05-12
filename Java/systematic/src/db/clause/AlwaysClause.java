package db.clause;

import java.sql.*;
import java.util.*;

import db.*;

final class AlwaysClause extends Clause {
    private static final long serialVersionUID = 1L;
	
	private final boolean isTrue;

	public AlwaysClause(boolean isTrue) {
		this.isTrue = isTrue;
	}

	@Override public int bindInto(int n, PreparedStatement st) throws SQLException {
		return n;
	}

	@Override public String toSql(String prefix, Set<Table> alreadyUsed) {
		return prefix + "1=" + (isTrue ? "1" : "0");
	}

	@Override public void collectTables(Set<Table> tables) {}

	@Override public void collectJoins(Set<JoinClause<?>> joins) {}
	
	@Override public Clause and(Clause clause) {
		return isTrue ? clause : Clause.FALSE;
	}
}