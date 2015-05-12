package db.columns;

import db.*;

public class SysnameColumn extends ConcreteColumn<Character> {
    private static final long serialVersionUID = 1L;

	public SysnameColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}
}
