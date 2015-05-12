package db.clause;
import static util.Errors.*;
import java.sql.*;
import java.util.*;

import db.*;

public class CommentClause extends Clause {
    private static final long serialVersionUID = 1L;

	private final String comment;
	private final Clause clause;

	public CommentClause(String comment, Clause clause) {
		this.comment = comment;
		bombIf(comment.contains("?"), "comments cannot contain ? without borking the replace");
		this.clause = clause;
	}

	@Override public int bindInto(int n, PreparedStatement st) throws SQLException {
		return clause.bindInto(n, st);
	}

	@Override public void collectJoins(Set<JoinClause<?>> joins) {
		clause.collectJoins(joins);
	}

	@Override public void collectTables(Set<Table> tables) {
		clause.collectTables(tables);
	}

	@Override public String toSql(String prefix, Set<Table> alreadyUsed) {
		return prefix + "-- " + comment + clause.toSql(prefix, alreadyUsed);
	}

}
