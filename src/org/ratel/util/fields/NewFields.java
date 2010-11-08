package org.ratel.util.fields;

import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Strings.*;

import java.util.*;

import org.ratel.util.*;

public class NewFields implements Sizable {
    Map<FieldName<?>, String> data = emptyMap();
    
    @Override public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override public int size() {
        return data.size();
    }
    
    public <T> void put(FieldName<T> name, T value) {
        name.putInto(this, value);
    }
    
    <T> void putFromJms(FieldName<T> name, String jms) {
        bombUnless(Strings.hasContent(jms) || name.hasDefault(), 
            name + " does not allow null, and \"\" counts as null on the message.");
        bombIf("null".equals(jms), "disallowed string");
        data.put(name, jms);
    }
    
    public boolean has(FieldName<?> name) {
        return data.containsKey(name);
    }
    
    public boolean hasContent(FieldName<?> name) {
        return name.hasContentOn(this);
    }
    
    @Deprecated
    String raw(FieldName<?> name) {
        requireKey(name);
        return data.get(name);
    }

    public void requireKey(FieldName<?> name) {
        if (!data.containsKey(name)) bomb(name + " not found in " + toHumanString(data));
    }
//
//    public static NewFields parse(String message) {
//        String[] fields = message.split("\\|");
//        NewFields result = new NewFields();
//        for(String s : fields) {
//            String [] parts = s.split("=");
//            result.put(parts[0], parts.length == 1 ? "" : parts[1]);
//        }
//        return result;
//    }

    
}