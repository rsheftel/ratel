package db.columns;

import db.*;

public class ImageColumn extends ConcreteColumn<Character> {
    private static final long serialVersionUID = 1L;

	public ImageColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}
}
