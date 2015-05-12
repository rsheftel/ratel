package db.clause;

import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.sql.*;
import java.util.*;

import db.*;
public class SubSelectClause<T> extends Clause {
    private static final long serialVersionUID = 1L;

	private static final List<String> OPERATORS = list("=", "in", "not in");
	private final Column<T> column;
	private final SelectOne<T> subSelect;
	private final String operator;

	public SubSelectClause(Column<T> column, SelectOne<T> subSelect, String operator) {
		bombUnless(OPERATORS.contains(operator), operator + " unsupported, use '=', 'in' or 'not in'");
		this.column = column;
		this.subSelect = subSelect;
		this.operator = operator;
	}

	@Override
	public int bindInto(int n, PreparedStatement st) throws SQLException {
		return subSelect.bindInto(n, st);
	}

	@Override
	public String toSql(String prefix, Set<Table> alreadyUsed) {
		return prefix + column.asSql() + " " + operator + " " + paren(subSelect.toSql(prefix + "    ", alreadyUsed) + prefix);
	}
	
	@Override
	public void collectTables(Set<Table> tables) {
		column.collectTables(tables);
	}

	@Override
	public void collectJoins(Set<JoinClause<?>> joins) {
		subSelect.collectJoins(joins);
	}

}
