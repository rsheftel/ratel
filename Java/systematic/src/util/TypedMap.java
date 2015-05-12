package util;

import static util.Dates.*;
import static util.Errors.*;
import static util.Strings.*;

import java.util.*;

import db.columns.*;

public class TypedMap extends HashMap<String, String> {

    private static final long serialVersionUID = 1L;

    public double numeric(String lastField) {
        return FloatColumn.from(string(lastField));
    }

    private String string(String key) {
       String result = get(key);
       if (result == null) throw bomb(key + " not found in " + toHumanString(this));
        return result;
    }

    public long long_(String key, long defalt) {
        if (isEmpty(key)) return defalt;
        try {
            return Long.parseLong(string(key));
        } catch (NumberFormatException e) {
            throw bomb("can't parse" + string(key) + " as a number!", e);
        }
    }

    private boolean isEmpty(String key) {
        return Strings.isEmpty(get(key));
    }

    public Date todayAt(String key) {
        return timeOn(now(), key);
    }

    public Date timeOn(Date date, String key) {
        return Dates.timeOn(string(key).replaceFirst("\\+00:00", "").replaceFirst("\\.000", ""), date);
    }

}
