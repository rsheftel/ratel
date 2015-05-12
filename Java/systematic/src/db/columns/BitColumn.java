package db.columns;

import java.util.*;

import db.*;
import static util.Objects.*;
import static util.Errors.*;
import static util.Sql.*;

public class BitColumn extends ConcreteColumn<Boolean> {
    private static final long serialVersionUID = 1L;

	private static final List<String> VALID_BOOLEAN_STRINGS = list("0", "1", "t", "f", "true", "false");

	public BitColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}
	
	@Override public Boolean valueFromString(String s) {
		return toBoolean(s);
	}

    public static boolean toBoolean(String s) {
        bombNull(s, "can't parse null as bit");
        s = s.toLowerCase().trim();
		bombUnless(
			VALID_BOOLEAN_STRINGS.contains(s), 
			"can't parse string " + quote(s) + " as bit"
		);
		return list("1", "t", "true").contains(s);
    }

	public Cell<?> yes() {
		return with(true);
	}
	public Cell<?> no() {
		return with(false);
	}
}
