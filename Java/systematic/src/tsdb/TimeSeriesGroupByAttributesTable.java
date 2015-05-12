package tsdb;

import static db.clause.Clause.*;
import static tsdb.Attribute.*;
import static tsdb.TSAMTable.*;

import java.util.*;

import db.*;
import db.tables.TSDB.*;

public class TimeSeriesGroupByAttributesTable extends TimeSeriesGroupByAttributesBase implements TimeSeriesGroupDefinition {
    private static final long serialVersionUID = 1L;
	public static final TimeSeriesGroupByAttributesTable GROUP_BY_ATTRIBUTES = new TimeSeriesGroupByAttributesTable();
	
	public TimeSeriesGroupByAttributesTable() {
		super("group_by_attributes");
	}

	public void insert(int arbId, AttributeValue value) {
		for (String name : value.names()) 
			insert(
				C_TIME_SERIES_GROUP_ID.with(arbId),
				C_ATTRIBUTE_NAME.with(value.attribute().name()),
				C_ATTRIBUTE_VALUE_NAME.with(name)
			);
	}

	public AttributeValues attributes(int id) {
		AttributeValues result = new AttributeValues();
		for (Row row : rows(C_TIME_SERIES_GROUP_ID.is(id))) 
			result.addOrAppend(
				attribute(row.value(C_ATTRIBUTE_NAME)).value(row.value(C_ATTRIBUTE_VALUE_NAME))
			);
		return result;
	}

	public void deleteAll() {
	    deleteAll(TRUE);
	}

	@Override public SelectOne<Integer> seriesLookup(int id, Date asOf) {
		AttributeValues attributes = attributes(id);
		return TSAM.timeSeriesIdLookup(attributes);
	}

    @Override public void delete(int id) {
        deleteAll(C_TIME_SERIES_GROUP_ID.is(id));
    }


}
