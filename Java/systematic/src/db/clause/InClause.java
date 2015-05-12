package db.clause;
import static java.util.Collections.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.sql.*;
import java.util.*;

import db.*;

public class InClause<T> extends Clause {
    private static final long serialVersionUID = 1L;

	private final Column<T> column;
	private final List<T> valueNames;
	private Table tempTable;

	public InClause(Column<T> column, List<T> valueNames) {
		this.column = column;
		this.valueNames = valueNames;
		if(valueNames.size() > 500) {
			tempTable = column.select(FALSE).groupBy(column).intoTemp(column.name() + System.nanoTime());
			List<Row> rows = empty();
			for (T valueName : valueNames)
				rows.add(new Row(column.with(valueName)));
			try {
                tempTable.insert(rows);
            } catch (RuntimeException e) {
                if (!e.getCause().getMessage().contains("Cannot insert explicit value for identity"))
                    bomb("unknown error inserting", e);
                Db.execute("set identity_insert " + tempTable.name() + " on");
                tempTable.insert(rows);
            }
		}
	}

	@Override
	public int bindInto(int n, PreparedStatement st) throws SQLException {
		if(tempTable != null) return n;
		for (T value : valueNames)
			column.bindInto(n++, st, value);
		return n;
	}

	@Override public void collectJoins(Set<JoinClause<?>> joins) {}

	@Override public void collectTables(Set<Table> tables) {
		column.collectTables(tables);
	}

	@Override
	public String toSql(String prefix, Set<Table> alreadyUsed) {
		if (tempTable != null)
			return prefix + column.asSql() + " in (select * from " + tempTable.name() + ")";
		if (valueNames.size() == 1)
			return prefix + column.asSql() + " = ?";
		return prefix + column.asSql() + " in " + paren(join(", ", nCopies(valueNames.size(), "?")));
	}

}
