package db;

import static db.StringRow.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.io.*;
import java.util.*;

import db.clause.*;
import db.columns.*;
public class Table implements Cloneable, Serializable, Comparable<Table> {
    private static final long serialVersionUID = 1L;

    private static final List<String> RESERVED = list("inner", "table", "and", "or");

    
	public static boolean NULL = true;
	public static boolean NOT_NULL = false;
	
	
	public static String from(String prefix, Set<Table> tables) {
		List<String> aliases = empty();
		for (Table table : tables) 
			aliases.add(prefix + table.from());
		return join(", ", aliases);
	}
	private final String name;
	private String alias;

	private List<ConcreteColumn<?>> columns = new ArrayList<ConcreteColumn<?>>();

	protected Table(String name) {
		this(name, null);
	}
	
	protected Table(String name, String alias) {
		this.name = name;
		this.alias = alias;
		bombIf(isReserved(alias), "alias " + alias + " is a reserved word in tsql.");
		Tables.register(this);
	}
	
	
	
	@Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Table other = (Table) obj;
        if (alias == null) {
            if (other.alias != null) return false;
        } else if (!alias.equals(other.alias)) return false;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    private boolean isReserved(String s) {
		return RESERVED.contains(s);
	}

	protected <T> void addColumn(ConcreteColumn<T> c) {
		columns.add(c);
	}

	public String aliased() {
		if(alias != null) return alias;
		return name();
	}

	/** find the column with the given name. this is not the typical way to get 
	 * a column out of the table. use the instance variables on the generated 
	 * table object in preference to using this method. */
	public ConcreteColumn<?> column(String columnName) {
		for (ConcreteColumn<?> c : columns) 
			if(c.name().equals(columnName))
				return c;
		throw bomb("no column with name " + columnName + " in table " + from());
	}
	
	@SuppressWarnings("unchecked") 
	public <T> ConcreteColumn<T> column(ConcreteColumn<T> template) {
		for (ConcreteColumn<?> c : columns) 
			if (c.name().equals(template.name())) return (ConcreteColumn<T>) c;
		throw bomb("no column matching " + template.name() + " in " + this + "\n" + copyColumns());
	}
	
	/** return the count of rows matching the given clause on this table. */
	public int count(Clause matches) {
		return countColumn().value(matches);
	}

	public CountTableColumn countColumn() {
		return new CountTableColumn(this);
	}

	/** create a temp table with the same structure (number and type of columns) as 
	 * this table given the name of the temp table. a "#" sign will be prepended 
	 * to the table name and doesn't need to be provided. */
	public Table createTemp(String tableName) {
		return createTemp(tableName, copyColumns());
	} 
	
	public Table createTemp(String tableName, List<Column<?>> select) {
		return select(select, Clause.FALSE).intoTemp(tableName);
	} 
	
	public Table createTemp(String tableName, Column<?> column) {
		return createTemp(tableName, column.asList());
	}

    /** remove EXACTLY one row corresponding to the clause passed in. if 0 or many rows are 
     * deleted, it will bomb.  */
    public void deleteOne(Clause c) {
        new Delete(this, c, true).execute();
    }
    
    /** remove ANY NUMBER of rows corresponding to the clause passed in. can safely delete 0 rows 
     * as well. */
    public void deleteAll(Clause c) {
        new Delete(this, c, false).execute();
    }
	
	/** only applicable to temp tables - drops the table, destroying all data */
	public void destroy() {
	    bombUnless(isTemp(), "can't destroy non temp tables.");
		Schema.dropTable(name());
	}

	/** produces a clause based on the existence of rows on this table matching 
	 * the given clause. This should not be confused with <code>rowExists</code> which
	 * produces a boolean result. */
	public Clause exists(Clause inner) {
		return new ExistsClause(true, inner);
	}

	private String from() {
		return name + (alias == null ? "" : " " + alias);
	}
	
	/** insert a row with the column/value pairings given as cell arguments: example: 
	 * timeSeries.insert(
	 * 		timeSeries.C_TIME_SERIES_NAME.with("appl close"), 
	 * 		timeSeries.C_TIME_SERIES_ID.with(7)
	 * )
	 */
	public void insert(Cell<?> ... values) {
		new Insert(this, values).execute();
	}
	
	/** insert a list of Row objects using a fast JDBC batch insert. */
	public void insert(List<Row> rows) {
		new BatchInsert(this, rows).execute();
	}
	
	/** insert a single Row object using a fast JDBC batch insert. */
	public void insert(Row row) {
		new Insert(this, row).execute();
	}

	/** insert a set of rows selected from a subselect */
	public void insert(SelectMultiple select) {
		new InsertSelect(this, select).execute();
	}	
	
	protected Clause joinTo(Table temp, ConcreteColumn<?> ... joinColumns) {
		Clause matches = Clause.TRUE;
		for (ConcreteColumn<?> column : joinColumns) 
			matches = column.joinOn(temp).and(matches);
		return matches;
	}

    public String shortName() {
        return name.replaceAll(".*\\.\\.", "");
    }

    public String name() {
		return name;
	}

    /** return a clause specifying the opposite of the "exists" method above. */
	public Clause notExists(Clause inner) {
		return new ExistsClause(false, inner);
	}

	/** returns a single row object matching the given clause. this method will 
	 * bomb if the results from the query is not EXACTLY one row. */
	public Row row(Clause clause) {
		return Db.row(select(copyColumns(), clause));
	}

	public Row rowDistinct(Clause clause) {
		return Db.row(selectDistinct(copyColumns(), clause));
	}
	
	public List<Row> rowsDistinct(Clause clause) {
		return Db.rows(selectDistinct(copyColumns(), clause));
	}
	
	/** return true if there is at least one row matching the given clause. not to 
	 * be confused with the "exists" method above. */
	public boolean rowExists(Clause matches) {
    	return count(matches) != 0;
    }

	/** returns the list of rows matching the given clause. */
	public List<Row> rows(Clause clause) {
		return Db.rows(select(copyColumns(), clause));
	}
	
	public List<Row> rows() {
		return rows(Clause.TRUE);
	}
	
	/** create a select clause for this table given the match clause, usable for 
	 * complex queries. */
	public SelectMultiple select(Clause match) {
		return select(match, false);
	}

	public SelectMultiple selectDistinct(Clause match) {
		return select(match, true);
	}

    private SelectMultiple select(Clause match, boolean distinct) {
        return new SelectMultiple(copyColumns(), match, distinct);
    }
	
	@Deprecated
	public List<Column<?>> copyColumns() {
		List<Column<?>> result = empty();
		for (ConcreteColumn<?> c : columns) {
		    if (c.exists())
		        result.add(c);
		}
		return result;
	}

	/** create a select clause for this table given the match clause, usable for 
	 * complex queries. */
	public SelectMultiple select(List<Column<?>> select, Clause match) {
		return new SelectMultiple(select, match);
	}
	
	public SelectMultiple selectDistinct(List<Column<?>> select, Clause match) {
		return new SelectMultiple(select, match, true);
	}

	@Override public String toString() {
		return aliased();
	}
	
	public void updateOne(Row replacements, Clause matches) {
	    new Update(this, replacements, matches, true).execute();
	}

	public void updateAll(Row replacements, Clause matches) {
	    new Update(this, replacements, matches, false).execute();
	}
	
	void update(Cell<?> replacement, Clause matches, boolean expectOne) {
		new Update(this, new Row(replacement), matches, expectOne).execute();
	}
	
	public void collectTables(Set<Table> tables) {
		if (!tables.add(this)) return;
	}

	public SchemaTable schemaTable() {
		List<SchemaColumn> schemaColumns = empty();
		for (Column<?> c : columns) 
			schemaColumns.add(c.schemaColumn());
		return new SchemaTable(new StringRow(c("TABLE_NAME", shortName()), c("TABLE_CAT", dbName())), schemaColumns );
	}

    private String dbName() {
        if (isTemp() && ! name().contains(".")) return "";
        bombUnless(name().contains("."), "no db name on table name " + name());
        return name().replaceAll("\\.\\..*", "");
    }

    boolean isTemp() {
        return name().contains("#");
    }

    public boolean owns(Row r) {
        for (Column<?> c : r.columns())
            if (!c.isOwnedBy(this)) return false;
        return true;
    }

    @Override public int compareTo(Table other) {
        String t = name;
        if(alias != null) t = t + alias;
        String o = other.name;
        if(other.alias != null) o = o + other.alias;
        return t.compareTo(o);
    }

}
