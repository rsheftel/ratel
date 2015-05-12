package db.columns;

import db.*;

public class NtextColumn extends StringColumn {
    private static final long serialVersionUID = 1L;

	public NtextColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}
	
	@Override public String leftHandSide() {
		return "convert(varchar(8000), " + asSql() + ")";
	}
}
