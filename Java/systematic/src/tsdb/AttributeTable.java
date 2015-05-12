package tsdb;

import static db.Tables.*;
import static util.Errors.*;
import static util.Objects.*;

import java.math.*;
import java.util.*;

import db.*;
import db.clause.*;
import db.tables.TSDB.*;
public class AttributeTable extends AttributeBase {
    private static final long serialVersionUID = 1L;
	public static final AttributeTable ATTRIBUTE = new AttributeTable();
	public AttributeTable() {
		super("attr");
	}

	public AttributeTable(String alias) {
		super(alias);
	}
	
	public Definition definition(String name) {
		return new Definition(row(C_ATTRIBUTE_NAME.is(name)));
	}

	public List<Definition> allDefinitions() {
		List<Definition> result = empty();
		List<Row> rows = rows(Clause.TRUE);
		for (Row row : rows) result.add(new Definition(row));
		return result;
	}

	class Definition {

		private final Row data;

		private Definition(Row row) {
			this.data = bombNull(row, "can't have null definition");
		}

		public Map<String, Integer> valueIds(List<String> valueNames) {
			Map<String, Integer> result = emptyMap();
			List<Column<?>> columns = empty();
			Table table = valueTable();
			Column<?> idColumn = valueIdColumn(table).alias("id");
			Column<?> nameColumn = valueNameColumn(table).alias("name");
			columns.add(idColumn);
			columns.add(nameColumn);
			List<Row> rows = table.select(columns, valueNamesMatch(valueNames, table)).rows();
			for (Row row : rows) 
				result.put(row.string(nameColumn), intValue(idColumn, row));
			return result;
		}

		public Map<String, Integer> valueIds() {
			Map<String, Integer> result = emptyMap();
			List<Column<?>> columns = empty();
			Table table = valueTable();
			Column<?> idColumn = valueIdColumn(table).alias("id");
			Column<?> nameColumn = valueNameColumn(table).alias("name");
			columns.add(idColumn);
			columns.add(nameColumn);
			List<Row> rows = table.select(columns, Clause.TRUE).rows();
			for (Row row : rows)
				result.put(row.string(nameColumn), intValue(idColumn, row));
			return result;
		}

		private Integer intValue(Column<?> idColumn, Row row) {
			Object proto = row.value(idColumn);
			if (idColumn.typeMatches("int( identity)?")) return (Integer) proto;
			return ((BigDecimal)proto).intValueExact();
		}
		
		String valueName(Integer valueId) {
			return String.valueOf(valueRow(valueId).value(valueNameColumn(valueTable())));
		}

		private Column<?> valueNameColumn(Table table) {
			return table.column(data.value(C_DESCRIPTION_COL_NAME));
		}

		private Column<?> valueIdColumn(Table table) {
			bombNull(table, "no table to lookup data from?");
			return table.column(data.value(C_PRIMARY_KEY_COL_NAME));
		}
		
		private Table valueTable() {
			String tableName = data.value(C_TABLE_NAME);
			if(!tableName.matches(".*\\.\\..*")) tableName = "TSDB.." + tableName;
			Table table = table(tableName);
			bombNull(table, "no table named " + tableName);
			return table;
		}
		
		public String valueTableName() {
			return valueTable().name();
		}

		@SuppressWarnings("unchecked")
		public void createValues(List<String> valueNames, Cell<?> ... extra) {
			Table table = valueTable();
			List<Row> rows = empty();
			Column<String> valueNameColumn = (Column<String>) valueNameColumn(table);
			for (String valueName : valueNames) {
				List<Cell<?>> cells = copy(list(extra));
				cells.add(valueNameColumn.with(valueName));
				rows.add(new Row(cells));
			}
			table.insert(rows);
		}

		@SuppressWarnings("unchecked")
		private Clause valueNamesMatch(List<String> valueNames, Table table) {
			return ((Column<String>)valueNameColumn(table)).in(valueNames);
		}

		int id() {
			return data.value(C_ATTRIBUTE_ID);
		}

		public String name() {
			return data.value(C_ATTRIBUTE_NAME);
		}

		public Row valueRow(AttributeValue value) {
			return valueRow(value.id());
		}

		@SuppressWarnings("unchecked") private Row valueRow(int id) {
			Table t = valueTable();
			Column<?> idColumn = valueIdColumn(t);
			Clause matches;
			if (idColumn.typeMatches("int( identity)?"))
				matches = ((Column<Integer>) idColumn).is(id);
			else 
				matches = ((Column<BigDecimal>) idColumn).is(new BigDecimal(id));
			return valueTable().row(matches);
		}

	}


}
