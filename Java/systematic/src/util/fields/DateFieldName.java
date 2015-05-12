package util.fields;

import static util.Dates.*;

import java.util.*;

import util.fields.FieldName;

public class DateFieldName extends FieldName<Date> {

    private static final long serialVersionUID = 1L;

    public DateFieldName(String name) {
        super(name);
    }

    public DateFieldName(String name, Date defalt) {
        super(name, defalt);
    }

    @Override public Date fromJmsString(String jms) {
        return date(jms);
    }

    @Override public String toJmsString(Date value) {
        return ymdHuman(value);
    }

}
