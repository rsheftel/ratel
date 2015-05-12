package db.columns;

import db.*;

public class IntColumn extends ConcreteColumn<Integer> {
    private static final long serialVersionUID = 1L;

	public IntColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}
	
	@Override public Integer valueFromString(String s) {
		return Integer.valueOf(s);
	}
}
