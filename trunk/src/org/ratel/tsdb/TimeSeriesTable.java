package org.ratel.tsdb;
import static org.ratel.tsdb.Attribute.*;
import static org.ratel.tsdb.TSAMTable.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;

import java.util.*;

import org.ratel.db.*;
import org.ratel.db.clause.*;
import org.ratel.db.columns.*;
import org.ratel.db.tables.TSDB.*;
public class TimeSeriesTable extends TimeSeriesBase {
    private static final long serialVersionUID = 1L;
    public static final TimeSeriesTable TIME_SERIES = new TimeSeriesTable();

    public TimeSeriesTable() {
        super("series");
    }

    public TimeSeriesTable(String alias) {
        super(alias);
    }
    
    public int id(String name) {
        return C_TIME_SERIES_ID.value(C_TIME_SERIES_NAME.is(name));
    }

    public List<String> names(AttributeValues values) {
        return TSAMTable.timeSeriesNames(values);
    }

    public String name(AttributeValues values) {
        return TSAMTable.timeSeriesName(values);
    }

    public String name(int id) {
        return C_TIME_SERIES_NAME.value(C_TIME_SERIES_ID.is(id));
    }

    public List<String> names(SelectOne<Integer> ids) {
        return C_TIME_SERIES_NAME.values(C_TIME_SERIES_ID.in(ids));
    }

    public String name(SelectOne<Integer> ids) {
        return C_TIME_SERIES_NAME.value(C_TIME_SERIES_ID.in(ids));
    }

    public Clause joinTo(IntColumn otherId) {
        return C_TIME_SERIES_ID.is(otherId);
    }

    public boolean exists(String name) {
        return rowExists(C_TIME_SERIES_NAME.is(name));
    }
    
    public void create(String name, AttributeValues values) {
        create(name, values, dataTable(values));
    }
    
    static TimeSeriesDataTable dataTable(AttributeValues values) {
        return TimeSeriesDataTable.createIfNeeded(dataTableName(values));
    }

    static String dataTableName(AttributeValues values) {
        return unMangledTableName(values).replaceAll("\\.", "dot");
    }

    private static String unMangledTableName(AttributeValues values) {
        if(values.has(INSTRUMENT)) {
            List<Attribute> attrs = dataTableInstrumentAttributes(values);
            if(!attrs.isEmpty())
                return "time_series_data_" + values.join("_", attrs);
        } else if (values.has(TRANSFORMATION))
            return "time_series_data_transformation_" + values.join("_", TRANSFORMATION);
        return "time_series_data";
    }

    private static List<Attribute> dataTableInstrumentAttributes(AttributeValues values) {
        if(values.has(INSTRUMENT, "cds")) {
            if(values.has(QUOTE_TYPE, "spread")) {
                if(values.has(TENOR))
                    return list(INSTRUMENT, QUOTE_TYPE, TENOR);
            } else if(values.has(QUOTE_TYPE))
                return list(INSTRUMENT, QUOTE_TYPE);
        } else if(values.has(INSTRUMENT, "std_equity_option")) {
            if(values.has(EXPIRY))
                return list(INSTRUMENT, EXPIRY);
        } else if(values.has(INSTRUMENT, "equity")) {
            if(values.has(QUOTE_TYPE, "close")) {
                if(values.has(QUOTE_CONVENTION))
                    return list(INSTRUMENT, QUOTE_TYPE, QUOTE_CONVENTION);
            } else if(values.has(QUOTE_TYPE))
                return list(INSTRUMENT, QUOTE_TYPE);
        } else if(values.has(INSTRUMENT, "mbs_tba")) {
            if(values.has(PROGRAM))
                return list(INSTRUMENT, PROGRAM);
        } else 
            return list(INSTRUMENT);
        return empty();
    }

    public void createAttributeValues(String name, AttributeValues values) {
        int id = new TimeSeries(name).id();
        TSAM.create(id, values);
    }
    
    public void addAtributeValue(int tsId, AttributeValue value) {
        TSAM.add(tsId, value);
    }

    public void delete(int id) {
        bombIf(
            TimeSeriesDataTable.hasData(id),
            "can't delete time seris without first deleting all data from time_series_data"
        );
        TSAM.deleteAttributes(id);
        deleteOne(C_TIME_SERIES_ID.is(id));
    }

    public void create(String name, AttributeValues values, TimeSeriesDataTable dataTable) {
        insert(C_DATA_TABLE.with(dataTable.shortName()), C_TIME_SERIES_NAME.with(name));
        createAttributeValues(name, values);
    }

    public String dataTable(int seriesId) {
        return C_DATA_TABLE.value(C_TIME_SERIES_ID.is(seriesId));
    }

    public List<String> dataTables(AttributeValues values) {
        Clause join = C_TIME_SERIES_ID.joinOn(TSAM);
        Clause idMatches = TSAM.timeSeriesIdMatches(values);
        return C_DATA_TABLE.distinct(idMatches.and(join));
    }

    public List<String> dataTables(TimeSeries[] serieses) {
        List<Integer> ids = empty();
        for (TimeSeries series : serieses) ids.add(series.id());
        return C_DATA_TABLE.distinct(C_TIME_SERIES_ID.in(ids));
    }

    public List<String> dataTables(Table temp) {
        return C_DATA_TABLE.distinct(C_TIME_SERIES_ID.joinOn(temp));
    }

    public List<String> dataTables(SelectOne<Integer> seriesLookup) {
        return C_DATA_TABLE.distinct(C_TIME_SERIES_ID.in(seriesLookup));
    }

    
}
