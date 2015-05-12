package db.columns;

import java.math.*;

import db.*;

public class NumericIdentityColumn extends ConcreteColumn<BigDecimal> {
    private static final long serialVersionUID = 1L;

	public NumericIdentityColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}
	
	@Override public BigDecimal valueFromString(String s) {
		return new BigDecimal(s);
	}
}
