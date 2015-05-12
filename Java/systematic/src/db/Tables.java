package db;
import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;
public class Tables {

	private static final Map<String, Table> TABLES = new HashMap<String, Table>();
	
	public static void register(Table table) {
		if (TABLES.containsKey(table.name())) return;
		if (table.aliased().endsWith("base")) 
			TABLES.put(table.name(), table);
	}
	
	public static Table table(String name) {
		List<String> schemas = Db.SCHEMA_NAMES;
		if (name.contains("..")) schemas = list(name.replaceAll("\\.\\..*", ""));
		for (String schema : schemas) {
			String unqualifiedName = name.replaceAll(".*\\.\\.", "");
			String className = javaClassify(unqualifiedName );
			String qualifiedClassName = "db.tables." + schema + "." + className + "Base";
			try {
				Class<?> tableClass = Class.forName(qualifiedClassName);
				String constantName = "T_" + javaConstify(unqualifiedName);
				return (Table) tableClass.getField(constantName).get(null);
			} catch (Exception fallBackToSchemaTable) {
			}
		}
		return bombNull(TABLES.get(name), "could not find table " + name);
	}
}
