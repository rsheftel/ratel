package org.ratel.tsdb;

import static org.ratel.tsdb.Attribute.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;

import java.util.*;

import org.ratel.db.clause.*;
import org.ratel.db.columns.*;

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
