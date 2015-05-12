package db;

import java.util.*;

public class Schema {

	public static List<SchemaTable> tables(String dbName) {
		ArrayList<SchemaTable> result = new ArrayList<SchemaTable>();
		for (String name : Db.tableNames(dbName)) 
			result.add(table(name));
		return result;
	}

	public static SchemaTable table(String name) {
		return new SchemaTable(Db.tableDefinition(name));
	}

	public static List<SchemaColumn> columns(SchemaTable table) {
		List<SchemaColumn> columns = new ArrayList<SchemaColumn>();
		List<StringRow> definitions = Db.columnDefinitions(table);
		for (StringRow definition : definitions) {
			columns.add(new SchemaColumn(definition));
		}
		return columns;
	}

    public static boolean hasPrimaryKey(String dbName, String tableName) {
        return Db.hasPrimaryKey(dbName, tableName);
    }

    public static void dropTable(String qualifiedName) {
        Db.execute("drop table " + qualifiedName);
    }
    
    public static boolean hasColumn(Table table, ConcreteColumn<?> column) {
        if (table.isTemp()) return true;
        return table(table.name()).columnNames().contains(column.name());
    }

}
