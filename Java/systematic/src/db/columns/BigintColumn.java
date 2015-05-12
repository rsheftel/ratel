package db.columns;

import db.*;

public class BigintColumn extends ConcreteColumn<Long> {
    private static final long serialVersionUID = 1L;

	public BigintColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}
	
	@Override public Long valueFromString(String s) {
		return Long.valueOf(s);
	}
}
