package db.columns;

import db.*;

public class NvarcharColumn extends StringColumn {
    private static final long serialVersionUID = 1L;

	public NvarcharColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}


}
