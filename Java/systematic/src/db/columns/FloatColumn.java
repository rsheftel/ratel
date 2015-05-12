package db.columns;

import db.*;

public class FloatColumn extends ConcreteColumn<Double> {
    private static final long serialVersionUID = 1L;

	public FloatColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}

	@Override public Double valueFromString(String s) {
		return Double.valueOf(s);
	}
	
    public static double from(String s) {
        return Double.valueOf(s);
    }
}
