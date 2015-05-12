package db;

import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import amazon.*;

import db.clause.*;
public abstract class Select implements Serializable, S3Cacheable<List<Row>> {
    private static final long serialVersionUID = 1L;
	protected final Clause clause;
	private final boolean distinct;
	private List<Column<?>> groupBy = empty();
    private Integer count = null;
    private List<OrderBy> order = empty();

	public Select(Clause clause, boolean distinct) {
		this.clause = clause;
		this.distinct = distinct;
	}
	
	public Select(Clause clause) {
		this(clause, false);
	}

	public int bindInto(int n, PreparedStatement st) throws SQLException {
		return clause.bindInto(n, st);
	}
	
	protected abstract void collectTables(Set<Table> tables);

	private Set<JoinClause<?>> collectJoins() {
		Set<JoinClause<?>> joins = emptySet();
		collectJoins(joins);
		return joins;
	}

	public void collectJoins(Set<JoinClause<?>> joins) {
		clause.collectJoins(joins);
	}
	
	public String toSql(String prefix, Set<Table> alreadyUsed, String into) {
		Set<Table> allTables = collectTables();
		Set<Table> tables = new TreeSet<Table>(allTables);
		tables.removeAll(alreadyUsed);
		String indent = prefix + "    ";
		String select = prefix + select() + selectColumnsString(indent);
		String from = prefix + "from " + Table.from(indent, tables);
		String where = prefix + "where " + clause.toSql(indent, allTables);
		String group = groupBy.isEmpty() ? "" : prefix + "group by " + Column.asGroupBy(indent, groupBy);
		String orderBy = order.isEmpty() ? "" : prefix + "order by " + Column.asOrderBy(indent, order);
		String sql = select + into + from + where + group + orderBy;
		bombIf(isCrossJoin(), "cross join query generated! sql produced: \n" + sql); 
		return sql;
	}

	public String toSql(String prefix, Set<Table> alreadyUsed) {
		return toSql(prefix, alreadyUsed, "");
	}
	
	@Override public String toString() {
	    return Db.fakeSql(this);
	}

	private String select() {
	    String top = count == null ? "" : "top " + paren(count + "");
		return "select " + (distinct ? "distinct " : "") + top;
	}

	private Set<Table> collectTables() {
		Set<Table> tables = clause.collectTables();
		collectTables(tables);
		return tables;
	}

	protected abstract String selectColumnsString(String prefix);

	public abstract List<Column<?>> columns();

	boolean isCrossJoin() {
		Set<Table> tables = collectTables();
		Set<JoinClause<?>> joins = collectJoins();
		if(tables.size() == 1) return false;
		if(joins.isEmpty()) 
			return true;
		Set<Table> tablesSeen = new HashSet<Table>();
		tablesSeen.add(first(tables));
		boolean removedOne = true;
		while(removedOne) {
			removedOne = false;
			for (Iterator<JoinClause<?>> i = joins.iterator(); i.hasNext(); ) {
				Set<Table> joinTables = i.next().tables();
				for (Table table : joinTables) {
					if(tablesSeen.contains(table)) {
						tablesSeen.addAll(joinTables);
						i.remove();
						removedOne = true;
						break;
					}
				}
			}
		}
		if(!tablesSeen.containsAll(tables))
			return true;
		return false;
	}

	public String toSql(String prefix) {
		return toSql(prefix, new HashSet<Table>());
	}
	
	public Table intoTemp(String name) {
		SelectInto into = new SelectInto(name, this);
		into.execute();
		return into.table();
	}

	public Select groupBy(Column<?> c) {
		groupBy.add(c);
		return this;
	}

    public void top(int n, OrderBy ... orderBys) {
        orderBy(orderBys);
        count = n;
    }

    public Select orderBy(OrderBy... orderBys) {
        order.addAll(list(orderBys));
        return this;
    }

    public List<Row> rows() {
    	return Db.rows(this);
    }

    @Override public List<Row> response() {
        return rows();
    }
    
    public MetaBucket.Key key(MetaBucket bucket) {
        return bucket.key("db.", urlEncode(serialize(Db.fakeSql(this))));
    } 

}
