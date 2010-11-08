package org.ratel.tsdb;

import static org.ratel.tsdb.TimeSeriesGroupByAttributesTable.*;
import static org.ratel.cds.MarkitTimeSeriesGroup.*;
import static org.ratel.tsdb.TimeSeriesGroupByNameTable.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Range.*;

import java.util.*;

import org.ratel.db.*;
import org.ratel.db.clause.*;
import org.ratel.db.columns.*;
import org.ratel.db.tables.TSDB.*;

public class TimeSeriesGroupTable extends TimeSeriesGroupBase {
    private static final long serialVersionUID = 1L;
    private static final String ATTRIBUTE = "ATTRIBUTE";
    private static final String NAME = "NAME";
    private static final String MARKIT = "MARKIT";

    public class TimeSeriesGroup extends Row {
        private static final long serialVersionUID = 1L;

        TimeSeriesGroup(Row row) {
            super(row);
        }

        public void insert(List<TimeSeries> series) {
            requireType(NAME);
            for (TimeSeries timeSeries : series)
                GROUP_BY_NAME.insert(id(), timeSeries);
        }

        public void insert(TimeSeries ... series) {
            insert(list(series));
        }

        private Integer id() {
            return value(C_ID);
        }

        private void requireType(String type) {
            bombUnless(
                type().equals(type), 
                "can only insert time series into " + type + " groups"
            );
        }

        private String type() {
            return value(C_TYPE);
        }

        public void insert(AttributeValues values) {
            requireType(ATTRIBUTE);
            for (AttributeValue value : values)
                GROUP_BY_ATTRIBUTES.insert(id(), value);
        }
        
        private TimeSeriesGroupDefinition definition() {
            if(type().equals(NAME)) return GROUP_BY_NAME;
            if(type().equals(ATTRIBUTE)) return GROUP_BY_ATTRIBUTES;
            if(type().equals(MARKIT)) return GROUP_MARKIT;
            throw bomb("undefined time series group type " + type());
        }

        public Cell<?> cell(IntColumn col) {
            return col.with(id());
        }

        public String name() {
            return value(C_NAME);
        }

        public boolean anyPopulatedToday(Date asOf, DataSource source) {
            SelectOne<Integer> seriesLookup = seriesLookup(asOf);
            return TimeSeriesDataTable.anyExist(seriesLookup, source, onDayOf(asOf));
        }

        public boolean allPopulatedToday(Date asOf, DataSource source) {
            return TimeSeriesDataTable.allExist(seriesLookup(asOf), source, onDayOf(asOf));
        }

        SelectOne<Integer> seriesLookup(Date asOf) {
            return definition().seriesLookup(id(), asOf);
        }

        public List<String> notPopulatedToday(Date asOf, DataSource source) {
            return TimeSeriesDataTable.seriesNotPopulated(seriesLookup(asOf), source, onDayOf(asOf));
        }

        public void arbitrate(Date asOf, DataSource from, DataSource to) {
            TimeSeriesDataTable.copy(seriesLookup(asOf), onDayOf(asOf), from, to);
        }
        
        public void arbitrate(Date asOf, DataSource from, DataSource to, int hour) {
            TimeSeriesDataTable.copy(seriesLookup(asOf), onDayOf(asOf), from, to, hour);
        }

        public void delete() {
            definition().delete(id());
            GROUPS.deleteOne(C_ID.is(id()));
        }

        public void purge(Date asOf, DataSource source) {
            TimeSeriesDataTable.purge(seriesLookup(asOf), source, onDayOf(asOf));
        }

        public boolean isEmpty(Date asOf) {
            return definition().seriesLookup(id(), asOf).values().isEmpty();
        }


    }

    public static final TimeSeriesGroupTable GROUPS = new TimeSeriesGroupTable();
    
    public TimeSeriesGroupTable() {
        super("groups");
    }

    public TimeSeriesGroup insert(String name, List<TimeSeries> series) {
        insert(name, NAME);
        TimeSeriesGroup group = get(name);
        group.insert(series);
        return group;
    }

    public TimeSeriesGroup insert(String name, TimeSeries ... series ) {
        return insert(name, list(series));
    }
    public TimeSeriesGroup insert(String name, AttributeValues values) {
        TimeSeriesGroup group = insert(name, ATTRIBUTE);
        group.insert(values);
        return group;
    }

    public TimeSeriesGroup insert(String name, String type) {
        insert(
            C_NAME.with(name),
            C_TYPE.with(type)
        );
        return get(name);
    }

    public TimeSeriesGroup get(String name) {
        try {
            return new TimeSeriesGroup(row(nameMatches(name)));
        } catch (RuntimeException e) {
            throw bomb("group " + name + " not found.", e);
        }
    }

    public int id(String groupName) {
        return C_ID.value(nameMatches(groupName));
    }

    private Clause nameMatches(String groupName) {
        return C_NAME.is(groupName);
    }

    public boolean has(String groupName) {
        return rowExists(nameMatches(groupName));
    }


}
