package db.clause;

import java.sql.*;
import java.util.*;

import db.*;

public class NotClause extends Clause {
    private static final long serialVersionUID = 1L;

	private final Clause inner;

	public NotClause(Clause inner) {
		this.inner = inner;
	}

	@Override
	public int bindInto(int n, PreparedStatement st) throws SQLException {
		return inner.bindInto(n, st);
	}

	@Override
	public String toSql(String prefix, Set<Table> alreadyUsed) {
		return prefix + "not (" + inner.toSql(prefix + "    ", alreadyUsed) + prefix + ")";
	}
	
	@Override
	public void collectTables(Set<Table> tables) {
		inner.collectTables(tables);
	}

	@Override
	public void collectJoins(Set<JoinClause<?>> joins) {
		inner.collectJoins(joins);
	}

}
