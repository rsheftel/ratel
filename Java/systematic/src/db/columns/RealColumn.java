package db.columns;

import db.*;

public class RealColumn extends ConcreteColumn<Float> {
    private static final long serialVersionUID = 1L;

	public RealColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}

	@Override public Float valueFromString(String s) {
		return Float.valueOf(s);
	}

	public Double doubleValue(Row row) {
		Float floatValue = row.value(this);
		return floatValue == null ? null : Double.parseDouble(floatValue.toString());
	}
	
}
