package db;

import static db.StringRow.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import db.clause.*;
import db.columns.*;

/**
 * The Column represents a single column in the database for the purpose of 
 * requesting related values (e.g., <code>value</code> and <code>values</code>), creating SQL (e.g. <code>is</code>, 
 * <code>joinOn</code>, <code>in</code>, etc) and creating "Cell" objects for insert and update (e.g., <code>with</code>)
 * 
 * Column objects are typically acquired from table objects, as in 
 * new TimeSeriesTable().C_TIME_SERIES_ID. The column knows what table it lives on, so 
 * the table need not be further referenced for most usages.
 */
public abstract class Column<T> implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;
	private final String name;
	private final String type;
    private final boolean nullable;
    private final String identity;

	public Column(String name, String type, boolean nullable, String identity) {
		this.name = name;
		this.type = type;
        this.nullable = nullable;
        this.identity = identity;
	}

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identity == null) ? 0 : identity.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Column<?> other = (Column<?>) obj;
        if (identity == null) {
            if (other.identity != null) return false;
        } else if (!identity.equals(other.identity)) return false;
        return true;
    }

    /** construct a new cell with this column and a type appropriate value	 
	 * <code>C_TIME_SERIES_NAME.with("aapl close") </code> 
	 * used for updates and inserts. */
	public Cell<T> with(T t) {
		return new Cell<T>(this, t);
	}
	
	public Cell<T> withString(String s) {
		return new Cell<T>(this, valueFromString(s));
	}
	
	public T valueFromString(String s) {
		throw bomb("column " + this + " does not know how to convert " + s);
	}

	/** construct a new cell with this column and another type appropriate column 
	 * in order to make a correlated assignment in an update. */
	public ColumnAssignment<T> withColumn(final Column<T> column) {
		return new ColumnAssignment<T>(this, column);
	}

	public String name() {
		return name;
	}

	@Override public String toString() {
		return asSelect();
	}

	@SuppressWarnings("unchecked") Cell<T> cellFrom(ResultSet rs, int i) throws SQLException {
		return new Cell<T>(this, (T)rs.getObject(i));
	}
	
	
	/** given a where clause that will limit results to exactly one row, read the value 
	 * from the DB. this method *will* invoke a single query to the database in the form 
	 * select (this) from (owner) where (clause). if the result is not exactly one 
	 * element, the query will bomb with a convenient error message. */
	public T value(Clause clause) {
		return Db.value(select(clause));
	}
	
	public T valueOrNull(Clause clause) {
		return theOrNull(values(clause));
	}

	/** give a where clause, read the list of values 
	 * from the DB. this method *will* invoke a single query to the database in the form 
	 * select (this) from (owner) where (clause). */
	public List<T> values(Clause clause) {
		return Db.values(select(clause));
	}

	public SelectOne<T> select(Clause clause, boolean distinct) {
		return new SelectOne<T>(this, clause, distinct);
	}
	
	/** read the list of values 
	 * from the DB. this method *will* invoke a single query to the database in the form 
	 * select (this) from (owner). */
	public List<T> values() {
		return values(Clause.TRUE);
	}
	
	public List<T> distinct(Clause matches) {
		return select(matches, true).values();
	}

	@SuppressWarnings("unchecked")
	T value(Map<Column<?>, Cell<?>> data) {
		Cell<?> cell = data.get(this);
		if (cell == null)
			throw bomb("no column " + this + " in\n" + Row.toSring(data));
		return (T) cell.value();
	}
	
	protected T value(Row row) {
		return row.value(this);
	}
	
	public Clause lessThanOr(T t) {
        return new ComparisonClause<T>(this, t, Comparison.LE);
    }
    
	public Clause lessThan(T t) {
	    return new ComparisonClause<T>(this, t, Comparison.LT);
	}
	
    public Clause greaterThanOr(T t) {
        return new ComparisonClause<T>(this, t, Comparison.GE);
    }
    
    public Clause greaterThan(T t) {
        return new ComparisonClause<T>(this, t, Comparison.GT);
    }
	    

	/** construct a where clause in the form (this) = ? and a bind variable. 
	 *  timeSeries.C_TIME_SERIES_NAME.is("foo")
	 * */
	public Clause is(T t) {
		return new ComparisonClause<T>(this, t);
	}

	/** construct a where clause in the form (this column) = (other column). 
	 * timeSeries.C_TIME_SERIES_NAME.is(timeSeries2.C_TIME_SERIES_NAME)
	 */
	public Clause is(Column<T> otherColumn) {
		return new JoinClause<T>(this, otherColumn);
	}

	/** construct a clause from a sub select in the form (this) = (select column from ...) */
	public Clause is(SelectOne<T> subSelect) {
		return new SubSelectClause<T>(this, subSelect, "=");
	}
	
	/** create a clause to specify that this column must *not* be equal to value. */
	public Clause isNot(T value) {
		return new ComparisonClause<T>(this, value, Comparison.NE);
	}
	/** create a clause to specify that this column must *not* be null. */
	public Clause isNotNull() {
		return new IsNullClause(this, false);
	}
	
	/** create a clause to specify that this column must be null. */
	public Clause isNull() {
		return new IsNullClause(this, true);
	}
	
	public Clause isNot(Column<T> other) {
		return new JoinClause<T>(this, other, Comparison.NE);
	}

	/** construct a clause from a sub select in the form (this) IN (select column from ...) */
	public Clause in(SelectOne<T> subSelect) {
		return new SubSelectClause<T>(this, subSelect, "in");
	}
	
	/** construct a clause from a sub select in the form (this) NOT IN (select column from ...) */
	public Clause notIn(SelectOne<T> subSelect) {
	    return new SubSelectClause<T>(this, subSelect, "not in");
	}

	/** construct a clause in the form this IN (?, ?, ?...) with bind variables. */ 
	public Clause in(List<T> valueNames) {
		return new InClause<T>(this, valueNames);
	}

	public abstract void collectTables(Set<Table> tables);

	public String asSql() {
		return name();
	}
	
	public String asSelect() {
		return asSql();
	}

	public List<Column<?>> asList() {
		return columns(this);
	}

	public static List<Column<?>> columns(Column<?> ... columns) {
		List<Column<?>> result = empty();
		for (Column<?> c : columns) 
			result.add(c);
		return result;
	}

	public static <T> List<Column<?>> columns(List<Column<T>> columns) {
	    List<Column<?>> result = empty();
	    for (Column<?> c : columns) 
	        result.add(c);
	    return result;
	} 
	
	public static String asOrderBy(String prefix, List<OrderBy> order) {
        List<String>  strings = new ArrayList<String>();
        for (OrderBy o : order) 
            strings.add(prefix + o.asSql());
        return join(", ", strings);
    }

    public static String asGroupBy(String prefix, List<Column<?>> groupBy) {
		List<String>  names = new ArrayList<String>();
		for (Column<?> c : groupBy) 
			names.add(prefix + c.asSql());
		return join(", ", names);
	}
	
	public static String asSelect(String prefix, List<Column<?>> columns) {
		List<String>  names = new ArrayList<String>();
		for (Column<?> c : columns) 
			names.add(prefix + c.asSelect());
		return join(", ", names);
	}

	public static String asInsert(String prefix, Set<Column<?>> columns) {
		List<String> names = empty();
		for (Column<?> column : columns) 
			names.add(prefix + column.name());
		return join(", ", names);
	}

	public boolean typeMatches(String match) {
		return this.type.matches(".*" + match + ".*");
	}

	public int bindInto(int n, PreparedStatement st, T value) throws SQLException {
		st.setObject(n++, value);
		return n;
	}

	public Column<T> alias(String alias) {
		return new AliasColumn<T>(alias, this);
	}

	public SelectOne<T> select(Clause matches) {
		return select(matches, false);
	}

	public SchemaColumn schemaColumn() {
	    String t = type;
	    String size = "0";
	    String digits = "0";
	    if(type.matches("\\(")) {
	        t = type.replaceAll("\\(.*\\)", "");
	        String params = type.replaceAll(".*\\(", "").replaceAll("\\).*", "");
	        String[] parts = params.split(",");
	        size = first(parts);
	        if(parts.length == 2)
	            digits = second(parts);
	    }
		return new SchemaColumn(new StringRow(
		    c("COLUMN_NAME", name), 
		    c("TYPE_NAME", t),
		    c("COLUMN_SIZE", size),
		    c("DECIMAL_DIGITS", digits),
		    c("IS_NULLABLE", nullable ? "YES" : "NO ") 
		));
	}

	public String type() {
		return type;
	}

	public String leftHandSide() {
		return asSql();
	}

	public void copyOnto(Table temp) {
		new ConcreteColumn<T>(name, type, temp, nullable);
	}
	public FunctionColumn<T> max() {
		return new FunctionColumn<T>("max", this);
	}
	public FunctionColumn<T> min() {
		return new FunctionColumn<T>("min", this);
	}

	public static List<String> names(List<Column<?>> columns) {
		List<String> result = empty();
		for (Column<?> column : columns) 
			result.add(column.name());
		return result;
	}

    public CountColumnColumn countDistinctColumn() {
        return new CountColumnColumn(this, true);
    }

    public OrderBy descending() {
        return new OrderBy(this, false);
    }

    public OrderBy ascending() {
        return new OrderBy(this, true);
    }

    public boolean isOwnedBy(@SuppressWarnings("unused") Table table) {
        return false;
    }
    
    public String string(T value) {
        return String.valueOf(value);
    }

    public String identity() {
        return identity;
    }

    public abstract boolean isConcrete();

}
