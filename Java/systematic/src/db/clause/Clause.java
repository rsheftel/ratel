package db.clause;

import java.io.*;
import java.sql.*;

import static util.Errors.*;
import static util.Objects.*;
import java.util.*;

import db.*;


/** represents a where clause in a sql statement. */ 
public abstract class Clause  implements Serializable {

	/** evaluates to the identity for AND in sql - "1 = 1" */
	public static final Clause TRUE = new AlwaysClause(true);
	public static final Clause FALSE = new AlwaysClause(false);

	public abstract String toSql(String prefix, Set<Table> alreadyUsed);

	public abstract int bindInto(int n, PreparedStatement st) throws SQLException;

	public abstract void collectJoins(Set<JoinClause<?>> joins);
	public abstract void collectTables(Set<Table> tables);
	
	public final Set<Table> collectTables() {
		Set<Table> result = new HashSet<Table>();
		collectTables(result);
		return result;
	}
	
	public static Clause comment(String comment, Clause clause) {
		return new CommentClause(comment, clause);
	}

	public static Clause not(Clause clause) {
		return new NotClause(clause);
	}
	
	/** join this clause to the given clause using the AND keyword. 
	 * SERIES.C_TIME_SERIES_NAME.is("aapl").and(SERIES.C_TIME_SERIES_ID.id(17))
	 * NOTE: if your query mixes "and" and "or" it is highly advisable that you
	 * use the "parenGroup" method to force parentheses appropriately. 
	 * */
	public Clause and(Clause clause) {
		return new AndClause(this, clause);
	}
	
	public static Clause parenGroup(Clause inner) {
		return new ParenClause(inner);
	}
	
	@Override public String toString() {
		return toSql("", new HashSet<Table>());
	}

	/** join this clause to the given clause using the AND keyword. 
	 * SERIES.C_TIME_SERIES_NAME.is("aapl").or(SERIES.C_TIME_SERIES_ID.id(17))
	 * NOTE: if your query mixes "and" and "or" it is highly advisable that you
	 * use the "parenGroup" method to force parentheses appropriately. 
	 * */
	public Clause or(Clause other) {
		return new OrClause(this, other);
	}

	public int count() {
	    Table arbitrary;
		try {
            arbitrary = first(collectTables());
		} catch (RuntimeException e) {
		    throw bomb("can't find arbitraty table using collectTables in \n" + this.toSql("\n", new HashSet<Table>()), e);
		}
        return arbitrary.count(this);
	}

	public boolean isEmpty() {
		return count() == 0;
	}

    public boolean exists() {
        return !isEmpty();
    }


}
