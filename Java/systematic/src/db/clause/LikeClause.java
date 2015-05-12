package db.clause;

import java.sql.*;
import java.util.*;

import db.*;
import db.columns.*;

public class LikeClause extends Clause {
    private static final long serialVersionUID = 1L;

	private final StringColumn column;
	private final String like;

	public LikeClause(StringColumn column, String like) {
		this.column = column;
		this.like = like;
	}

	@Override public int bindInto(int n, PreparedStatement st) throws SQLException {
		return column.bindInto(n, st, like);
	}

	@Override public void collectJoins(Set<JoinClause<?>> joins) { }
	@Override public void collectTables(Set<Table> tables) {
		column.collectTables(tables);
	}

	@Override public String toSql(String prefix, Set<Table> alreadyUsed) {
		return prefix + column.asSql() + " like ?";
	}

}
