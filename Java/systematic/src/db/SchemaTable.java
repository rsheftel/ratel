package db;

import static util.Index.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import util.*;
public class SchemaTable {

    private final StringRow definition;
	private List<SchemaColumn> columns;

	public SchemaTable(StringRow definition) {
		this.definition = definition;
	}

	public SchemaTable(StringRow definition, List<SchemaColumn> columns) {
		this.definition = definition;
		this.columns = columns;
	}
	
	public String qualifiedName() {
		return dbName() + ".." + tableName();
	}

	private String dbName() {
		return definition.get("TABLE_CAT");
	}
	
	public String tableName() {
		return definition.get("TABLE_NAME");
	}

	public String fileName() {
		return dbName() + "/" + className() + ".java";
	}

	private String className() {
		return javaClassify(tableName()) + "Base";
	}

	public List<SchemaColumn> columns() {
		if (columns == null)
			columns = Schema.columns(this);
		return columns;
	}

	public String java(String superPackage) {
		StringBuilder buf = new StringBuilder();
		buf.append("package db." + superPackage + "." + dbName() + ";\n\n");
		buf.append("import db.*;\n");
		buf.append("import db.columns.*;\n\n");
		buf.append("public class " + className() + " extends Table {\n\n");
		buf.append("    private static final long serialVersionUID = 1L;");
		String constantDeclaration = "    public static final " + className() + " T_" + constantName();
		String constantInit = "new " + className() + "(" + dQuote(tableName()+"base")+ ");";
		buf.append(constantDeclaration + " = " + constantInit + "\n\n");
		buf.append("    " + subclassConstructor() + "\n\n");
		for (SchemaColumn c : columns())
            buf.append("    " + c.java() + ";\n");
		buf.append("\n");
		buf.append("\n}\n\n");
		return buf.toString();
	}

	private String constantName() {
		return javaConstify(tableName());
	}

	private String subclassConstructor() {
		return "public " + className() + "(String alias) { super(\"" + qualifiedName() + "\", alias); }"; 
	}
	
	public String catalog() {
		return dbName();
	}

    public boolean exists() {
        return Db.tableExists(dbName(), tableName());
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((columns == null) ? 0 : columns.hashCode());
        result = prime * result + ((definition == null) ? 0 : definition.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final SchemaTable other = (SchemaTable) obj;
        if (columns == null) {
            if (other.columns != null) return false;
        } else if (!columns.equals(other.columns)) return false;
        if (definition == null) {
            if (other.definition != null) return false;
        } else if (!definition.equals(other.definition)) return false;
        return true;
    }

    @Override public String toString() {
        return tableName();
    }

    public void create() {
        StringBuilder b = new StringBuilder();
        for (Index<SchemaColumn> sc : indexing(columns)) {
            b.append("    " + sc.value.sql());
            if (!sc.isLast()) b.append(",\n");
        }
        Db.execute("\ncreate table TSDB.." + tableName() + " (\n" + b + "\n)");
    }

    public void destroyIfExists() {
        if (exists()) Schema.dropTable(qualifiedName());
    }

    public void createPrimaryKey(Column<?> ... indexColumns) {
        StringBuilder b = new StringBuilder();
        for (Index<Column<?>> c : indexing(indexColumns)) {
            b.append("        " + c.value.name());
            if (!c.isLast()) b.append(",\n");
        }
        String name = "pk_" + tableName();
        if (Schema.hasPrimaryKey(dbName(), tableName())) return;
        Db.execute(
            "\nalter table " + qualifiedName() + " add constraint \n    " + 
            name + " primary key clustered (\n" +
            b.toString() + "\n    )"
        );
    }

    public List<String> columnNames() {
        if (columns == null) columns = Schema.columns(this);
        List<String> result = empty();
        for(SchemaColumn c : columns)
            result.add(c.name());
        return result;
    }
}
