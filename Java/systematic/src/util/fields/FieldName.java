package util.fields;

import static util.Errors.*;
import static util.Strings.*;

import java.io.*;
import java.util.*;

/**
 * FieldName *does not* allow nulls, only empty string. if the default value is 
 * set to null, it indicates that there is no default
 */
public abstract class FieldName<T> implements Serializable, Comparable<FieldName<T>> {
    private static final long serialVersionUID = 1L;
    
    private final String name;
    private final T defalt;
    
    public FieldName(String name) {
        this(name, null);
    }
    
    public FieldName(String name, T defalt) {
        this.name = name;
        this.defalt = defalt;
    }

    public void putInto(NewFields fields, T value) {
        if (value == null && !hasDefault())
            bomb(this  + " does not allow null");
        fields.putFromJms(this, toJmsString(value));
    }
    
    public static DateFieldName dateName(String name) {
        return new DateFieldName(name);
    }
    
    public static StringFieldName stringName(String name) {
        return new StringFieldName(name);
    }

    public static StringFieldName stringName(String name, String defalt) {
        return new StringFieldName(name, defalt);
    }

    public static IntFieldName intName(String name) {
        return new IntFieldName(name);
    }
    
    @Override public int compareTo(FieldName<T> other) {
        return name.compareTo(other.name);
    }

    public abstract String toJmsString(T value);
    public abstract T fromJmsString(String jms);
    
    @SuppressWarnings("deprecation") public boolean hasContentOn(NewFields fields) {
        return hasContent(fields.raw(this));
    }
    
    @SuppressWarnings("deprecation") public T from(NewFields fields) {
        if (fields.has(this) && fields.hasContent(this)) return fromJmsString(fields.raw(this));
        return tryDefault(fields);
    }

    private T tryDefault(NewFields fields) {
        if (defalt != null) return defalt; // if it has a defalt, the messaeg doesn't haven't to have it.
        fields.requireKey(this);
        throw bomb(this + " has no default.");
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        return equals( (FieldName<?>) obj);
    }

    public boolean equals(FieldName<?> other) {
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    @Override public String toString() {
        return "F-" + name;
    }

    public boolean hasDefault() {
        return defalt != null;
    }

    public static IntFieldName intName(String name, int defalt) {
        return new IntFieldName(name, defalt);
    }

    public static LongFieldName longName(String name) {
        return new LongFieldName(name);
    }

    public static LongFieldName longName(String name, long defalt) {
        return new LongFieldName(name, defalt);
    }

    public static DoubleFieldName doubleName(String name) {
        return new DoubleFieldName(name);
    }

    public static DoubleFieldName doubleName(String name, double defalt) {
        return new DoubleFieldName(name, defalt);
    }

    public static DateFieldName dateName(String name, Date defalt) {
        return new DateFieldName(name, defalt);
    }

}