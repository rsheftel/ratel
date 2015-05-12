package db.clause;
import static util.Objects.*;

import java.sql.*;
import java.util.*;

import db.*;

public class JoinClause<T> extends Clause {
    private static final long serialVersionUID = 1L;

	private final Column<T> left;
	private final Column<T> right;
	private final Comparison comparison;

	public JoinClause(Column<T> left, Column<T> right) {
		this(left, right, Comparison.EQ);
	}

	public JoinClause(Column<T> left, Column<T> right, Comparison comparison) {
		this.left = left;
		this.right = right;
		this.comparison = comparison;
	}

	@Override public int bindInto(int n, PreparedStatement st) throws SQLException {
		return n;
	}

	@Override public String toSql(String prefix, Set<Table> alreadyUsed) {
		return prefix + left.asSql() + " " + comparison.op() + " " + right.asSql();
	}
	
	@Override public void collectTables(Set<Table> tables) {
		left.collectTables(tables);
		right.collectTables(tables);
	}

	public Set<Table> tables() {
		Set<Table> tables = emptySet();
		collectTables(tables);
		return tables;
	}

	@Override
	public void collectJoins(Set<JoinClause<?>> joins) {
		joins.add(this);
	}
}
