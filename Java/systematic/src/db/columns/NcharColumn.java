package db.columns;

import db.*;

public class NcharColumn extends StringColumn {
    private static final long serialVersionUID = 1L;

	public NcharColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}
}
