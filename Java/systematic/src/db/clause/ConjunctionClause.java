package db.clause;

import java.sql.*;
import java.util.*;

import db.*;

public class ConjunctionClause extends Clause {
    private static final long serialVersionUID = 1L;

	protected final Clause left;
	protected final Clause right;
	private final String conjunction;

	public ConjunctionClause(Clause left, Clause right, String conjunction) {
		this.left = left;
		this.right = right;
		this.conjunction = conjunction;
	}

	@Override
	public int bindInto(int n, PreparedStatement st) throws SQLException {
		int newN = left.bindInto(n, st);
		newN = right.bindInto(newN, st);
		return newN;
	}

	@Override
	public String toSql(String prefix, Set<Table> alreadyUsed) {
		return left.toSql(prefix, alreadyUsed) + " " + conjunction + " " + right.toSql(prefix, alreadyUsed);
	}

	@Override
	public void collectTables(Set<Table> tables) {
		left.collectTables(tables);
		right.collectTables(tables);
	}

	@Override
	public void collectJoins(Set<JoinClause<?>> joins) {
		left.collectJoins(joins);
		right.collectJoins(joins);
	}

}
