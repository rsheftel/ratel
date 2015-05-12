package db.columns;

import db.*;

public class VarbinaryColumn extends ConcreteColumn<Character> {
    private static final long serialVersionUID = 1L;

	public VarbinaryColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}
}
