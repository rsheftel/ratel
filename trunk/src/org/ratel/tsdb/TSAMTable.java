package org.ratel.tsdb;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Strings.*;

import java.util.*;

import org.ratel.db.*;
import org.ratel.db.clause.*;
import org.ratel.db.columns.*;
import org.ratel.db.tables.TSDB.*;
public class TSAMTable extends TimeSeriesAttributeMapBase {
    private static final long serialVersionUID = 1L;
    public static final TSAMTable TSAM = alias("TSAM");

    protected TSAMTable(String alias) {
        super(alias);
    }
    
    public static TSAMTable alias(String alias) {
        return new TSAMTable(alias);
    }

    public static int timeSeriesId(AttributeValues values) {
        return alias("tsam0").timeSeriesIdLookup(values).value();
    }

    public static List<Integer> timeSeriesIds(AttributeValues values) {
        return alias("tsam0").timeSeriesIdLookup(values).values();
    }
    
    public static List<String> timeSeriesNames(AttributeValues values) {
        SelectOne<Integer> ids = alias("tsam0").timeSeriesIdLookup(values);
        return TimeSeriesTable.TIME_SERIES.names(ids);
    }
    
    public static String timeSeriesName(AttributeValues values) {
        try {
            SelectOne<Integer> ids = alias("tsam0").timeSeriesIdLookup(values);
            return TimeSeriesTable.TIME_SERIES.name(ids);
        } catch (RuntimeException e) {
            throw bomb("failed to get time series name for \n" + join("\n", strings(values)), e);
        }
    }

    private SelectOne<Integer> timeSeriesIdLookup(AttributeValue first, AttributeValues rest) {
        return new SelectOne<Integer>(C_TIME_SERIES_ID, timeSeriesIdMatches(first, rest), false);
    }

    private Clause timeSeriesIdMatches(AttributeValue first, AttributeValues rest) {
        Clause matches =  attributeMatches(first);
        int i = 1;
        for (AttributeValue value : rest) {
            TSAMTable tsam = alias("tsam" + i++);
            matches = matches.and(tsam.attributeMatches(value));
            matches = matches.and(tsam.joinTo(C_TIME_SERIES_ID));
        }
        return matches;
    }

    Clause joinTo(IntColumn otherId) {
        return otherId.is(C_TIME_SERIES_ID);
    }

    public Clause attributeMatches(AttributeValue value) {
        return value.matches(C_ATTRIBUTE_ID, C_ATTRIBUTE_VALUE_ID);
    }

    public SelectOne<Integer> timeSeriesIdLookup(AttributeValues values) {
        bombIf(values.isEmpty(), "no attribute values to lookup from!");
        return timeSeriesIdLookup(values.first(), values.rest());
    }
    
    public Clause timeSeriesIdMatches(AttributeValues values) {
        bombIf(values.isEmpty(), "no attribute values to lookup from!");
        return timeSeriesIdMatches(values.first(), values.rest());
    }
    
    public void create(int tsId, AttributeValues values) {
        bombIf(TimeSeries.exists(values), "time series already exists with attributes " + values);
        for (AttributeValue value : values)
            add(tsId, value);
    }

    public void add(int tsId, AttributeValue value) {
        insert(
            C_ATTRIBUTE_VALUE_ID.with(value.id()),
            C_TIME_SERIES_ID.with(tsId),
            C_ATTRIBUTE_ID.with(value.attribute().id())
        );
    }

    public void deleteAttributes(int timeSeriesId) {
        deleteAll(C_TIME_SERIES_ID.is(timeSeriesId));
    }

    public AttributeValues attributes(TimeSeries timeSeries) {
        List<Row> rows = rows(timeSeries.is(C_TIME_SERIES_ID));
        AttributeValues result = new AttributeValues();
        for (Row row : rows) {
            Integer attributeId = row.value(C_ATTRIBUTE_ID);
            Integer valueId = row.value(C_ATTRIBUTE_VALUE_ID);
            result.add(AttributeValue.fromIds(attributeId, valueId));
        }
        return result;
    }

}
