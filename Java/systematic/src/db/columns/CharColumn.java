package db.columns;

import db.*;
import db.clause.*;

public class CharColumn extends StringColumn {
    private static final long serialVersionUID = 1L;

	public CharColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}

	public Clause is(char charAt) {
		return is(charAt + "");
	}
}
