package db.columns;

import db.*;

public class XmlColumn extends ConcreteColumn<String> {
    private static final long serialVersionUID = 1L;

	public XmlColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}
}
