package db.columns;

import java.math.*;

import db.*;

public class NumericColumn extends ConcreteColumn<BigDecimal> {
    private static final long serialVersionUID = 1L;

	public NumericColumn(String name, String type, Table owner, boolean nullable) {
		super(name, type, owner, nullable);
	}
	
	@Override public BigDecimal valueFromString(String s) {
		return new BigDecimal(s);
	}

    public Cell<?> with(int tickerId) {
        return with(new BigDecimal(tickerId));
    }
}
