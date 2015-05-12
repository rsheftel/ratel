package db.clause;

import java.sql.*;
import static db.Comparison.*;
import java.util.*;

import db.*;

public class ComparisonClause<T> extends Clause {
    private static final long serialVersionUID = 1L;

	private final Column<T> column;
	private final T value;
	private final Comparison comparison;

	public ComparisonClause(Column<T> column, T value) {
		this(column, value, EQ);
	}

	public ComparisonClause(Column<T> column, T value, Comparison comparison) {
		this.column = column;
		this.value = value;
		this.comparison = comparison;
	}

	@Override public String toSql(String prefix, Set<Table> alreadyUsed) {
	    if (isNull()) return prefix + column.leftHandSide() + " is null";
	    if (isNotNull()) return prefix + column.leftHandSide() + " is not null";
		return prefix + column.leftHandSide() + " " + comparison.op() + " ?";
	}

    private boolean isNotNull() {
        return value == null && comparison == NE;
    }

    private boolean isNull() {
        return value == null && comparison == EQ;
    }

	@Override public int bindInto(int n, PreparedStatement st) throws SQLException {
	    if (isNull() || isNotNull()) return n;
		st.setObject(n, value);
		return n + 1;
	}
	
	@Override public void collectTables(Set<Table> tables) {
		column.collectTables(tables);
	}

	@Override
	public void collectJoins(Set<JoinClause<?>> joins) {}

}
