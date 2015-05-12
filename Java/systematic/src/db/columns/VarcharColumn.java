package db.columns;

import db.*;

public class VarcharColumn extends StringColumn {
    private static final long serialVersionUID = 1L;

	public VarcharColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}

}
