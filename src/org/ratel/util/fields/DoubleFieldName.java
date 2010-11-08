package org.ratel.util.fields;

import java.math.*;

public class DoubleFieldName extends FieldName<Double> {

    private static final long serialVersionUID = 1L;

    public DoubleFieldName(String name) {
        super(name);
    }

    public DoubleFieldName(String name, Double defalt) {
        super(name, defalt);
    }

    @Override public Double fromJmsString(String jms) {
        return new BigDecimal(jms).doubleValue();
    }

    @Override public String toJmsString(Double value) {
        return value == null ? "" : String.valueOf(value);
    }

}
