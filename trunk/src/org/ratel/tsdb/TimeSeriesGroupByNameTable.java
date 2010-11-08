package org.ratel.tsdb;

import static org.ratel.tsdb.TimeSeriesTable.*;

import java.util.*;

import org.ratel.db.*;
import org.ratel.db.clause.*;
import org.ratel.db.tables.TSDB.*;

public class TimeSeriesGroupByNameTable extends TimeSeriesGroupByNameBase implements TimeSeriesGroupDefinition {
    private static final long serialVersionUID = 1L;
    public static final TimeSeriesGroupByNameTable GROUP_BY_NAME = new TimeSeriesGroupByNameTable();

    public TimeSeriesGroupByNameTable() {
        super("group_by_name");
    }

    public void insert(int id, TimeSeries series) {
        insert(C_TIME_SERIES_GROUP_ID.with(id), C_TIME_SERIES_NAME.with(series.name()));
    }

    @Override public SelectOne<Integer> seriesLookup(int id, Date asOf) {
        return TIME_SERIES.C_TIME_SERIES_ID.select(idMatches(id));
    }

    private Clause idMatches(int id) {
        Clause groupIdMatches = C_TIME_SERIES_GROUP_ID.is(id);
        Clause seriesNameMatches = C_TIME_SERIES_NAME.joinOn(TIME_SERIES);
        return seriesNameMatches.and(groupIdMatches);
    }

    @Override public void delete(int id) {
        deleteAll(C_TIME_SERIES_GROUP_ID.is(id));
    }

}
