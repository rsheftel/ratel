package db.columns;

import db.*;

public class IntIdentityColumn extends ConcreteColumn<Integer> {
    private static final long serialVersionUID = 1L;
	
	public IntIdentityColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}
	
	@Override public Integer valueFromString(String s) {
		return Integer.valueOf(s);
	}
}
