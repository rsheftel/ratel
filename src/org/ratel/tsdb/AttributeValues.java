package org.ratel.tsdb;

import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;

import java.util.*;

import org.ratel.util.*;

public class AttributeValues implements Iterable<AttributeValue> {
    private final Map<Attribute, AttributeValue> values;

    public AttributeValues(Map<Attribute, AttributeValue> values) {
        this.values = values;
    }
    
    public AttributeValues() {
        values = emptyMap();
    }
    
    public static AttributeValues values(AttributeValue ... values) {
        return new AttributeValues(values);
    }

    private AttributeValues(AttributeValue ... values) {
        this();
        add(list(values));
    }

    @Override public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("{\n");
        for (Attribute a : values.keySet()) 
            b.append("\t" + a.name() + " = " + get(a).names() + "\n");
        b.append("}");
        return b.toString();
    }
    
    public Set<Attribute> attributes() {
        return values.keySet();
    }

    public void add(AttributeValues newValues) {
        add((Iterable<AttributeValue>) newValues);
    }
    
    public void add(AttributeValue v) {
        add(list(v));
    }

    private void add(Iterable<AttributeValue> newValues) {
        for (AttributeValue value : newValues) {
            bombIf(has(value.attribute()), "duplicated attribute " + value + " in " + this);
            values.put(value.attribute(), value);
        }
    }
    
    public String join(String delim, Attribute ... attributes) {
        return join(delim, list(attributes));
    }
    
    public String join(String delim, List<Attribute> attributes) {
        return Strings.join(delim, valueNames(attributes));
    }

    private List<String> valueNames(List<Attribute> attributes) {
        List<String> result = empty();
        for (Attribute a : attributes) 
            result.add(get(a).name());
        return result;
    }

    public boolean has(Attribute attribute) {
        return values.containsKey(attribute);
    }

    public AttributeValue get(Attribute attribute) {
        return values.get(attribute);
    }

    public int size() {
        return values.size();
    }

    public AttributeValue remove(Attribute a) {
        return values.remove(a);
    }

    public void replace(AttributeValue value) {
        Attribute a = value.attribute();
        bombUnless(has(a), "cannot replace nonexistant attribute " + a);
        values.put(a, value);
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final AttributeValues other = (AttributeValues) obj;
        if (values == null) {
            if (other.values != null) return false;
        } else if (!values.equals(other.values)) return false;
        return true;
    }

    @Override public Iterator<AttributeValue> iterator() {
        return values.values().iterator();
    }

    public void addOrAppend(AttributeValue value) {
        if (!has(value.attribute())) { add(value); return; }
        get(value.attribute()).append(value.names());
        
    }

    public boolean isEmpty() {
        return attributes().isEmpty();
    }

    public AttributeValue first() {
        return Objects.first(values.values());
    }

    public AttributeValues rest() {
        AttributeValues result = new AttributeValues();
        result.add(values.values());
        result.remove(first().attribute());
        return result;
    }

    public boolean has(Attribute attr, String value) {
        return has(attr) && get(attr).equals(attr.value(value));
    }

    public String value(Attribute attribute) {
        return get(attribute).name();
    }

    public void requireHas(AttributeValue value) {
        bombUnless(has(value.attribute(), value.name()), value + " does not exist in " + attributes());
    }

    public void requireMissing(Attribute attr) {
        bombIf(has(attr), "attr " + attr + " was found but not expected!");
    }
    
    
}
