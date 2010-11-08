package org.ratel.util.fields;

public class LongFieldName extends FieldName<Long> {

    private static final long serialVersionUID = 1L;

    public LongFieldName(String name) {
        super(name);
    }

    public LongFieldName(String name, long defalt) {
        super(name, defalt);
    }

    @Override public Long fromJmsString(String jms) {
        return Long.valueOf(jms);
    }

    @Override public String toJmsString(Long value) {
        return value == null ? "" : String.valueOf(value);
    }

}
