package db;

import static java.lang.Integer.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;
public class SchemaColumn {

	private static final List<String> VARIABLE_LENGTH_TYPES = list(
	   "numeric", "decimal", "float", "char", "varchar", "nchar", "nvarchar", "binary", "varbinary"
	);
    private final StringRow definition;

	public SchemaColumn(StringRow definition) {
		this.definition = definition;
	}

	public String name() {
		return definition.get("COLUMN_NAME");
	}

	public String java() {
		String declaration = "public " + columnClass() + " " + constantName();
		String required = nullable() ? "NULL": "NOT_NULL";
        String ownerTable = "this";
        String args = join(", ", dQuote(name()), dQuote(type()), ownerTable, required);
        String initialization = "new " + columnClass() + paren(args); 
		return declaration + " = " + initialization;
	}

	String constantName() {
		return "C_" + name().toUpperCase();
	}

	String columnClass() {
		return javaClassify(type().replaceAll("\\(.*\\)", "")) + "Column";
	}

	private String type() {
		String name = definition.get("TYPE_NAME");
		String[] nameParts = name.split(" ");
		String size = "" + size();
		String digits = definition.get("DECIMAL_DIGITS");
		List<String> args = empty();
		if (!size.equals("0")) { 
		    args.add(size);
		    if (!digits.equals("0")) args.add(digits); 
		}
		if(!VARIABLE_LENGTH_TYPES.contains(first(nameParts)))
		    args = empty();
        return 
            first(nameParts) + 
            (args.isEmpty() ? "" : paren(commaSep(args))) + 
            (nameParts.length == 1 ? "" : " " + second(nameParts)); 
	}

    public int size() {
        String size = definition.get("COLUMN_SIZE");
        return isEmpty(size) ? -1 : parseInt(size);
    }

    public String sql() {
        return name() + " " + type() + (nullable() ? "" : " NOT NULL");
    }

    private boolean nullable() {
        return !definition.get("IS_NULLABLE").equals("NO ");
    }

}
