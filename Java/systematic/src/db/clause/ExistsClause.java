package db.clause;

import static util.Strings.*;

import java.sql.*;
import java.util.*;

import db.*;
import db.columns.*;

public class ExistsClause extends Clause {
    private static final long serialVersionUID = 1L;

	private static final Column<Integer> ONE = ConstantColumn.constant(1);
	
	private final SelectOne<Integer> inner;
	private final boolean exists;

	public ExistsClause(boolean exists, Clause inner) {
		this.exists = exists;
		this.inner = new SelectOne<Integer>(ONE, inner, false);
	}

	@Override
	public int bindInto(int n, PreparedStatement st) throws SQLException {
		return inner.bindInto(n, st);
	}

	@Override
	public String toSql(String prefix, Set<Table> alreadyUsed) {
		return prefix + (exists ? "" : "not ") + "exists " + paren(inner.toSql(prefix + "    ", alreadyUsed) + prefix);
	}

	@Override
	public void collectTables(Set<Table> tables) {}

	@Override
	public void collectJoins(Set<JoinClause<?>> joins) {
		inner.collectJoins(joins);
	}

}
