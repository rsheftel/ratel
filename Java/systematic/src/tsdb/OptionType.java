package tsdb;

import static tsdb.Attribute.*;
import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import db.clause.*;
import db.columns.*;

public enum OptionType {
	CALL, PUT;

	private static final List<String> OPTION_TYPE_FLAGS = list("C", "P");
	
	public Clause is(CharColumn c) {
		return c.is(name().charAt(0));
	}

	public AttributeValue value() {
		return OPTION_TYPE.value(name().toLowerCase());
	}

	public static OptionType fromFlag(String value) {
		value = value.toUpperCase();
		bombUnless(OPTION_TYPE_FLAGS.contains(value), "unknown option type " + value);
		return value.equals("C") ? CALL : PUT;
	}
}
