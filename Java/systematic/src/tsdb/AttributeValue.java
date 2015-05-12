package tsdb;

import static db.clause.Clause.*;
import static tsdb.Attribute.*;
import static tsdb.TSAMTable.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import db.*;
import db.clause.*;
import db.columns.*;
public class AttributeValue implements Comparable<AttributeValue> {


	public static final AttributeValue SPREAD = QUOTE_TYPE.value("spread");
	
	private final Attribute attribute;
	private final List<String> names = empty();

	AttributeValue(Attribute attribute, String ... values) {
		this.attribute = attribute;
		this.names.addAll(nonEmpty(list(values)));
	}

	public List<Integer> ids() {
		return attribute.valueIds(names);
	}
	
	public int id() {
		return the(attribute.valueIds(names));
	}

	@SuppressWarnings("unchecked")
	public Clause matches(IntColumn attributeId, IntColumn valueId) {
		List<Integer> values = nonEmpty(
			attribute.valueIds(names), 
			"cannot find value for attribute (" + attribute + ") name: " + names
		);
		String comment = attribute + " = " + join(", ", strings(names));
		return comment(comment, attribute.matches(attributeId).and(valueId.in(values)));
	}

	public Attribute attribute() {
		return attribute;
	}

	public void create() {
		create(new Cell<?>[0]);
	}

    public static AttributeValue createdIfNecessary(Attribute attribute, String representation, Cell<?>... extra) {
        AttributeValue value = attribute.value(representation.toLowerCase());
        if (!value.exists()) value.create(extra);
        return value;
    }
	
	public AttributeValue createIfNeeded() {
		if(!exists())
			create();
		return this;
	}

	public void create(Cell<?> ... extra) {
		attribute.createValues(names, extra);
	}

	public boolean exists() {
		return attribute.valuesExist(names);
	}

	public static AttributeValue fromIds(Integer attributeId, Integer valueId) {
		Attribute attr = Attribute.attribute(attributeId);
		return new AttributeValue(attr, attr.valueName(valueId));
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attribute == null) ? 0 : attribute.hashCode());
		result = prime * result + ((names == null) ? 0 : names.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final AttributeValue other = (AttributeValue) obj;
		if (attribute == null) {
			if (other.attribute != null) return false;
		} else if (!attribute.equals(other.attribute)) return false;
		if (names == null) {
			if (other.names != null) return false;
		} else if (!names.equals(other.names)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return attribute + " = " + names;
	}

	public String name() {
		return the(names);
	}

	@Override public int compareTo(AttributeValue o) {
		int attrComparison = attribute.compareTo(o.attribute);
		return attrComparison == 0 ? name().compareTo(o.name()) : attrComparison;
	}

	public <T> T value(Column<T> c) {
		return attribute.extra(this).value(c);
	}

	public Clause tsamFilter() {
		return TSAM.attributeMatches(this);
	}

	public List<String> names() {
		return names;
	}

	public void append(List<String> toBeAdded) {
		names.addAll(toBeAdded);
	}

	public static void main(String[] args) {
	    // removed after version 4427 
	}
}
