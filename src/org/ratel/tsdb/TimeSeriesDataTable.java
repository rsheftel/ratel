package org.ratel.tsdb;

import static org.ratel.db.clause.Clause.*;
import static org.ratel.db.tables.TSDB.TimeSeriesDataBase.*;
import static org.ratel.db.temptables.TSDB.SeriesIdsBase.*;
import static org.ratel.tsdb.DataSource.*;
import static org.ratel.tsdb.DataSourceTable.*;
import static org.ratel.tsdb.TSAMTable.*;
import static org.ratel.tsdb.TimeSeries.*;
import static org.ratel.tsdb.TimeSeriesTable.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Log.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Sequence.*;

import java.util.*;

import org.ratel.amazon.*;

import org.ratel.tsdb.Observations.*;
import org.ratel.util.*;
import org.ratel.db.*;
import org.ratel.db.clause.*;
import org.ratel.db.columns.*;
import org.ratel.db.temptables.TSDB.*;
public class TimeSeriesDataTable extends Table {
    private static final long serialVersionUID = 1L;
    public IntColumn C_TIME_SERIES_ID = new IntColumn("time_series_id", "int", this, NOT_NULL);
    public IntColumn C_DATA_SOURCE_ID = new IntColumn("data_source_id", "int", this, NOT_NULL);
    public DatetimeColumn C_OBSERVATION_TIME = new DatetimeColumn("observation_time", "datetime", this, NOT_NULL);
    public FloatColumn C_OBSERVATION_VALUE = new FloatColumn("observation_value", "float", this, NOT_NULL);

    
    class ObservationRow extends Row {
        private static final long serialVersionUID = 1L;
        public ObservationRow(Row row) {
            super(row);
        }

        public void addInto(DataStore values) {
            values.put(time(), value(C_OBSERVATION_VALUE));
        }

        public SeriesSource seriesSource() {
            return new SeriesSource(value(seriesNameColumn()), value(sourceNameColumn()));
        }

        public Date time() {
            return new Date(value(C_OBSERVATION_TIME).getTime());
        }

    }
    
    protected TimeSeriesDataTable(String tableName, String alias) {
        super(tableName, alias);
    }
    
    public TimeSeriesDataTable(String alias) {
        this("TSDB..time_series_data", alias);
    }
    
    public TimeSeriesDataTable() {
        this("dataInTS"); 
    }
    
    public static TimeSeriesDataTable withName(String name) {
        return new TimeSeriesDataTable("TSDB.." + name, name);
    }

    private Clause dateMatches(Range dateRange) {
        return C_OBSERVATION_TIME.in(dateRange);
    }

    private List<ObservationRow> observationRows(Clause observationsMatch, Integer count, boolean includeNameColumns) {
        List<ObservationRow> result = empty();
        SelectMultiple s = select(observationsMatch);
        if(count != null) {
            s.top(count, C_OBSERVATION_TIME.descending());
        }
        if (includeNameColumns) {
            s.add(seriesNameColumn());
            s.add(sourceNameColumn());
        }
        List<Row> rows = s.rows();
        for (Row row : rows) 
            result.add(new ObservationRow(row));
        return result;
    }

    public static Observations observations(DataSource source, AttributeValues values) {
        return observations(source, null, null, values, null);
    }

    public static Observations observations(DataSource source, Range dateRange, AttributeValues values) {
        return observations(source, dateRange, null, values, null);
    }

    public static Observations observations(DataSource source, TimeSeries series) {
        return observations(source, null, series, null, null);
    }

    public static int observationsCount(DataSource source, TimeSeries series) {
        TimeSeriesDataTable data = forSeriesId(series.id());
        return data.count(matches(source, series, data));
    }

    private static Clause matches(DataSource source, TimeSeries series, TimeSeriesDataTable data) {
        return source.is(data.C_DATA_SOURCE_ID).and(series.is(data.C_TIME_SERIES_ID));
    }
    
    public static Observations observations(DataSource source, Range dateRange, TimeSeries series) {
        return observations(source, dateRange, series, null, null);
    }
    
    public static Observations observations(DataSource source, Integer count, TimeSeries series) {
        return observations(source, null, series, null, count);
    }
    
    public static Observations observations(DataSource source, Range range, int count, TimeSeries series) {
        return observations(source, range, series, null, count);
    }

    private TsdbObservations observationsMap(Clause match, Integer count) {
        return new TsdbObservations(observationRows(match, count, true));
    }

    public static TsdbObservations observationsMap(DataSource source, AttributeValues values) {
        return observationsMap(source, null, null, values, null);
    }
    
    private static TsdbObservations observationsMap(DataSource source,
        Range dateRange, TimeSeries[] serieses, AttributeValues values, Integer count) {
        TsdbObservations result = new TsdbObservations();
        List<String> tableNames = dataTables(serieses, values);
        bombIf(count != null && tableNames.size() != 1, "count only supported for single series queries");
        for (String tableName : tableNames) {
          TsdbObservations obs = forName(tableName).observationsMapNonStatic(source, dateRange, serieses, values, count);
          result.add(obs);
        }
        return result;
    }

    public static Double observationValueBefore(DataSource source, TimeSeries series, Date date) {
        String tableName = the(dataTables(array(series), null));
        TimeSeriesDataTable table = forName(tableName);
        return table.observationValueBeforeNonStatic(source, series, date);
    }

    private Double observationValueBeforeNonStatic(DataSource source, TimeSeries series, Date date) {
        Clause isSource = source.is(C_DATA_SOURCE_ID);
        Clause isSeries = series.is(C_TIME_SERIES_ID);
        Clause isBeforeDate = C_OBSERVATION_TIME.lessThan(date);
        Clause matches = isSource.and(isSeries).and(isBeforeDate);
        List<ObservationRow> rows = observationRows(matches, 1, false);
        return rows.isEmpty() ? null : the(rows).value(C_OBSERVATION_VALUE);
    }

    private static List<String> dataTables(TimeSeries[] serieses, AttributeValues values) {
        List<String> tableNames;
        if(values == null || values.isEmpty()) {
            tableNames = TIME_SERIES.dataTables(serieses);
        } else {
            tableNames = TIME_SERIES.dataTables(values);
        }
        return tableNames;
    }

    private TsdbObservations observationsMapNonStatic(DataSource source,
            Range dateRange, TimeSeries[] serieses, AttributeValues values, Integer count) {
        bombUnless(serieses != null || values != null, "series or values must be set to pick time series");
        bombIf(serieses == null && values == null, "series and values cannot both be set");
        if (values == null) values = new AttributeValues();
        Clause matches;

        TimeSeriesIdsBase ids = null;
        boolean wasInTransaction = Db.inTransaction();
        if(values.isEmpty()) {
            //  we know that series cannot be null due to bombIf above, but javac does not
            bombIf(count != null && serieses != null && serieses.length != 1, "count only supported for single series queries");
            matches = seriesMatches(serieses);
        } else {
            SelectOne<Integer> lookup = TSAMTable.TSAM.timeSeriesIdLookup(values);
            Clause tsJoin = TIME_SERIES.joinTo(C_TIME_SERIES_ID);
            if(S3Cache.sqsDbMode()) {
                matches = C_TIME_SERIES_ID.in(lookup).and(tsJoin);
                bombIf(count != null && lookup.count() != 1, "count only supported for single series queries");
            } else {
                if(!wasInTransaction) Db.startTransactionIgnoringReadonly();
                lookup.intoTemp("time_series_ids");
                ids = new TimeSeriesIdsBase("ids");
                bombIf(count != null && ids.count(TRUE) != 1, "count only supported for single series queries");
                matches = C_TIME_SERIES_ID.is(ids.C_TIME_SERIES_ID).and(tsJoin);
            }
        }
        
        if(source != null) matches = matches.and(sourceMatches(source));
        else matches = matches.and(joinTo(DATA_SOURCE, C_DATA_SOURCE_ID));
        
        if(dateRange != null)
            matches = matches.and(dateMatches(dateRange));
        
        TsdbObservations result = observationsMap(matches, count);
        if(ids != null) ids.destroy();
        if (!wasInTransaction && Db.inTransaction()) Db.rollback();
        if(!result.isEmpty()) return result;

        if(source != null)
            bombUnless(source.exists(), "data source does not exist: " + source.name());
        if(values.isEmpty()) 
            requireAllExist(serieses);
        else 
            bombIf(TimeSeries.multiSeries(values).isEmpty(), "cannot find time series for values: " + values);
        return result;
    }
    
    
    
    private void requireAllExist(TimeSeries[] serieses) {
        for (TimeSeries series : serieses) 
            bombUnless(series.exists(), "series " + series + " does not exist.");
    }

    private static Observations observations(DataSource source,
            Range dateRange, TimeSeries series, AttributeValues values, Integer count) {
        TsdbObservations result = observationsMap(source, dateRange, array(series), values, count);
        if (result.isEmpty()) return new Observations();
        return result.only();
    }

    public static TsdbObservations observationsMap(
            DataSource source, Range range, AttributeValues values) {
        return observationsMap(source, range, null, values, null);
    }

    public static TsdbObservations observationsMap(DataSource source,
            TimeSeries ... serieses) {
        return observationsMap(source, null, serieses, null, null);
    }


    public static TsdbObservations observationsMap(
            DataSource source, Range range, TimeSeries ... serieses) {
        return observationsMap(source, range, serieses, null, null);
    }
    
    public static TsdbObservations observationsMap(
            Range range, AttributeValues values) {
        return observationsMap(null, range, null, values, null);
    }
    
    public static TsdbObservations observationsMap(
            Range range, TimeSeries ... serieses) {
        return observationsMap(null, range, serieses, null, null);
    }
    

    public static TsdbObservations observationsMap(DataSource source, int count, TimeSeries[] serieses) {
        return observationsMap(source, null, serieses, null, count);
    }
    
    public static TsdbObservations observationsMap(DataSource source, int count, AttributeValues values) {
        return observationsMap(source, null, null, values, count);
    }
    
    VarcharColumn seriesNameColumn() {
        return TimeSeriesTable.TIME_SERIES.C_TIME_SERIES_NAME;
    }
    
    private Clause sourceMatches(DataSource source) {
        return source.joinIdToName(C_DATA_SOURCE_ID);
    }

    private Clause seriesMatches(TimeSeries ... serieses) {
        List<Integer> ids = empty();
        for (TimeSeries series : serieses)
            ids.add(series.id());
        return C_TIME_SERIES_ID.in(ids).and(TIME_SERIES.joinTo(C_TIME_SERIES_ID));
    }

    VarcharColumn sourceNameColumn() {
        return DataSourceTable.DATA_SOURCE.C_DATA_SOURCE_NAME;
    }

    public static void write(int seriesId, int sourceId, Observations observations) {
        Table temp = createTempData();
        int oldTimeout = Db.setQueryTimeout(300);
        try {
            writeToTemp(seriesId, sourceId, observations, temp);
            writeFromTemp(temp);
        } finally {
            Db.setQueryTimeout(oldTimeout);
        }
    }

    private static TimeSeriesDataTable forSeriesId(int seriesId) {
        return forName(TIME_SERIES.dataTable(seriesId));
    }

    static class TSDTCache extends HashMap<String, TimeSeriesDataTable> {
        private static final long serialVersionUID = 1L;
        public TimeSeriesDataTable cache(String name) {
            put(name, new TimeSeriesDataTable("TSDB.." + name, name));
            return get(name);
        }
    }
    private static final TSDTCache ALL = new TSDTCache();
    
    
    private static TimeSeriesDataTable forName(String name) {
        return ALL.containsKey(name) ? ALL.get(name) : ALL.cache(name);
    }
    
    private static void writeFromTemp(Table temp) {
        List<String> dataTables = TIME_SERIES.dataTables(temp);
        for (String dataTable : dataTables)
            forName(dataTable).writeFromTempNotStatic(temp);
    }
    
    @SuppressWarnings("deprecation") public static void checkTables() {
        List<String> dataTables = TIME_SERIES.C_DATA_TABLE.distinct(TRUE);
        int count = 0;
        for (String tableName : dataTables) {
            debug(tableName);
            TimeSeriesDataTable data = forName(tableName);
            SelectOne<Integer> seriesInDataTable = TIME_SERIES.C_TIME_SERIES_ID.select(TIME_SERIES.C_DATA_TABLE.is(tableName));
//            data.delete(data.C_TIME_SERIES_ID.notIn(seriesInDataTable), false);
            List<Row> badRows = data.rows(data.C_TIME_SERIES_ID.notIn(seriesInDataTable));
            count += badRows.size();
            for (Row row : badRows) {
                debug(
                    row.value(data.C_TIME_SERIES_ID) + "\t" + 
                    row.value(data.C_DATA_SOURCE_ID) + "\t" + 
                    row.value(data.C_OBSERVATION_TIME) + "\t" + 
                    row.value(data.C_OBSERVATION_VALUE)
                );
//                Date obsTime = row.value(data.C_OBSERVATION_TIME);
//                bombUnless(midnight(obsTime).equals(date("2008/04/18")), "unexpected date");
            }
        }
        debug("total: " + count);
    }
    
    @SuppressWarnings("deprecation") public static void main(String[] args) {
        debugSql(false);
        checkTables();
//        Db.commit();
    }

    @SuppressWarnings("unchecked")
    private void writeFromTempNotStatic(Table temp) {
        Clause pkMatches = joinTo(temp, C_TIME_SERIES_ID, C_DATA_SOURCE_ID, C_OBSERVATION_TIME);
        String shortName = name().replaceAll("TSDB\\.\\.", "");
        Clause dataTableMatches = TIME_SERIES.C_DATA_TABLE.is(shortName).and(temp.column(C_TIME_SERIES_ID).joinOn(TIME_SERIES));
        Row replacements = new Row(
            C_OBSERVATION_VALUE.withColumn((Column<Double>) temp.column("observation_value"))
        );
        updateAll(replacements, pkMatches);
        insert(temp.select(notExists(pkMatches).and(dataTableMatches)));
    }

    private static void writeToTemp(int seriesId, int sourceId, Observations observations, Table temp) {
        List<Row> rows = observationRows(seriesId, sourceId, observations);
        if (rows.isEmpty()) return;
        temp.insert(rows);
    }

    public static Row observationRow(int seriesId, int sourceId,
        Observations observations) {
        return the(observationRows(seriesId, sourceId, observations));
    }

    
    public static List<Row> observationRows(int seriesId, int sourceId,
            Observations observations) {
        List<Row> rows = empty();
        for (Date time : observations.times()) {
            rows.add(new Row(
                T_TIME_SERIES_DATA.C_TIME_SERIES_ID.with(seriesId),
                T_TIME_SERIES_DATA.C_DATA_SOURCE_ID.with(sourceId),
                T_TIME_SERIES_DATA.C_OBSERVATION_TIME.with(time),
                T_TIME_SERIES_DATA.C_OBSERVATION_VALUE.with(observations.value(time))
            ));
        }
        return rows;
    }

    private static Table createTempData() {
        return forName("time_series_data").createTemp("tdsbTemp");
    }
    
    public static void writeFromR(String[] seriesNames, String[] sourceNames, long[] timesMillis, double[] values) {
        requireParallel(list(seriesNames), list(sourceNames));
        bombUnless(timesMillis.length == seriesNames.length, "times length does not match series names");
        bombUnless(values.length == seriesNames.length, "values length does not match series names");
        
        Map<String, Integer> seriesIds = emptyMap();
        for (String series : seriesNames)
            if(!seriesIds.containsKey(series))
                seriesIds.put(series, series(series).id());
        Map<String, Integer> sourceIds = emptyMap();
        for (String source : sourceNames)
            if(!sourceIds.containsKey(source))
                sourceIds.put(source, source(source).id());
        
        List<Row> rows = empty();
        for (int i : along(seriesNames))
            rows.add(new Row(
                    T_TIME_SERIES_DATA.C_TIME_SERIES_ID.with(seriesIds.get(seriesNames[i])),
                    T_TIME_SERIES_DATA.C_DATA_SOURCE_ID.with(sourceIds.get(sourceNames[i])),
                    T_TIME_SERIES_DATA.C_OBSERVATION_TIME.with(new Date(timesMillis[i])),
                    T_TIME_SERIES_DATA.C_OBSERVATION_VALUE.with(values[i])
            ));
        writeUsingTempWithCommits(rows);
    }

    public static void writeUsingTempWithCommits(List<Row> rows) {
        Table temp = createTempData();
        int oldTimeout = Db.setQueryTimeout(300);
        try {
            List<Row> myRows = Objects.copy(rows);
            int batchSize = 10000;
            int batchCount = rows.size() / batchSize + 1;
            while(myRows.size() > batchSize) {
                Log.info("batches left " + batchCount--);
                List<Row> nextRows = myRows.subList(0, batchSize);
                myRows = myRows.subList(batchSize, myRows.size());
                temp.insert(nextRows);
                writeFromTemp(temp);
                Db.commit();
                Db.execute("truncate table " + temp.name());
            }
            temp.insert(myRows);
            writeFromTemp(temp);
            Db.commit();
        } finally {
            try {
                temp.destroy();
            } catch (RuntimeException e) {
                err("failed to destroy temp table (probably didn't exist)", e);
            }
            Db.setQueryTimeout(oldTimeout);
        }
    }

    public static Observations latestObservation(SeriesSource ss) {
        return latest(ss, true);
    }

    public static Observations firstObservation(SeriesSource ss) {
        return latest(ss, false);
    }
    
    private static Observations latest(SeriesSource ss, boolean getMax) {
        TimeSeriesDataTable data = forSeriesId(ss.series().id());
        FunctionColumn<java.sql.Timestamp> col = getMax 
            ? data.C_OBSERVATION_TIME.max() 
            : data.C_OBSERVATION_TIME.min();
        Date latest = col.value(ss.matches(data.C_TIME_SERIES_ID, data.C_DATA_SOURCE_ID));
        bombIf(latest == null, "no " + (getMax ? "last" : "first") + " data point in " + ss);
        return ss.observations(Range.range(latest));
    }

    public static void purge(SeriesSource ss) {
        TimeSeriesDataTable data = forSeriesId(ss.series().id());
        data.deleteAll(ss.matches(data.C_TIME_SERIES_ID, data.C_DATA_SOURCE_ID));
    }
    
    public static boolean anyExist(SelectOne<Integer> seriesLookup, DataSource source, Range range) {
        List<String> dataTables = TIME_SERIES.dataTables(seriesLookup);
        for (String dataTable : dataTables)
            if(forName(dataTable).existsNonStatic(seriesLookup, source, range)) return true;
        return false;
    }
    
    public boolean existsNonStatic(SelectOne<Integer> seriesLookup, DataSource source, Range range) {
        return !seriesSourceDateMatches(seriesLookup, source, range).isEmpty();
    }

    private Clause seriesSourceDateMatches(SelectOne<Integer> seriesLookup, DataSource source, Range range) {
        Clause ssMatches = seriesSourceMatches(seriesLookup, source);
        Clause hasTodayValue = C_OBSERVATION_TIME.in(range);
        return ssMatches.and(hasTodayValue);
    }

    private Clause seriesSourceMatches(SelectOne<Integer> seriesLookup, DataSource source) {
        Clause seriesMatches = C_TIME_SERIES_ID.in(seriesLookup);
        Clause sourceMatches = source.is(C_DATA_SOURCE_ID);
        return seriesMatches.and(sourceMatches);
    }

    public static boolean allExist(SelectOne<Integer> seriesLookup, DataSource source, Range range) {
        List<String> dataTables = TIME_SERIES.dataTables(seriesLookup);
        for (String dataTable : dataTables) {
            TimeSeriesDataTable data = forName(dataTable);
            if(data.matchesUnpopulated(seriesLookup, source, range).exists()) return false;
        }
        return true;
    }

    private Clause matchesUnpopulated(SelectOne<Integer> seriesLookup, DataSource source, Range range) {
        Clause seriesMatches = TIME_SERIES.C_TIME_SERIES_ID.in(seriesLookup).and(TIME_SERIES.C_DATA_TABLE.is(shortName()));
        Clause sourceMatches = source.is(C_DATA_SOURCE_ID);
        Clause inRange = range.matches(C_OBSERVATION_TIME);
        Clause join = C_TIME_SERIES_ID.joinOn(TIME_SERIES);
        Clause noData = notExists(join.and(sourceMatches.and(inRange)));
        return seriesMatches.and(noData);
    }
    
    public static List<String> seriesNotPopulated(SelectOne<Integer> seriesLookup, DataSource source, Range range) {
        List<String> result = empty();
        List<String> dataTables = TIME_SERIES.dataTables(seriesLookup);
        for (String dataTable : dataTables) {
            TimeSeriesDataTable data = forName(dataTable);
            Clause matches = data.matchesUnpopulated(seriesLookup, source, range);
            result.addAll(TIME_SERIES.C_TIME_SERIES_NAME.values(matches));
        }
        return result;
    }
    
    public static void purge(SelectOne<Integer> seriesLookup, DataSource source, Range range) {
        List<String> dataTables = TIME_SERIES.dataTables(seriesLookup);
        for (String dataTable : dataTables) {
            TimeSeriesDataTable data = forName(dataTable);
            data.deleteAll(data.seriesSourceDateMatches(seriesLookup, source, range));
        }
    }

    public static void copy(SelectOne<Integer> seriesLookup, Range range, DataSource from, DataSource to) {
        List<String> dataTables = TIME_SERIES.dataTables(seriesLookup);
        boolean dataCopied = false;
        for (String dataTable : dataTables) {
            TimeSeriesDataTable data = forName(dataTable);
            dataCopied = data.copyNonStatic(seriesLookup, range, from, to, data.C_OBSERVATION_TIME) || dataCopied;
        }
        bombUnless(dataCopied, "no data to arbitrate!");
    }
    
    static class ForceHourColumn extends SyntheticColumn<java.sql.Timestamp> {
        private static final long serialVersionUID = 1L;

        private final DatetimeColumn column;
        private final int hour;

        public ForceHourColumn(DatetimeColumn column, int hour) {
            super("forceTime", column.type(), NOT_NULL, "forceTime_" + column.identity());
            this.column = column;
            this.hour = hour;
        }

        @Override public void collectTables(Set<Table> tables) {
            column.collectTables(tables);
        }
        
        @Override public String asSql() {
            return "dateadd(hour, " + hour + ", convert(datetime, floor(convert(float, " + column.asSql() + "))))";
        }
        
        @Override public String string(java.sql.Timestamp t) {
            return column.string(t);
        }
        
    }
    
    public static void copy(SelectOne<Integer> seriesLookup, Range range, DataSource from, DataSource to, int hour) {
        List<String> dataTables = TIME_SERIES.dataTables(seriesLookup);
        boolean dataCopied = false;
        for (String dataTable : dataTables) {
            TimeSeriesDataTable data = forName(dataTable);
            ForceHourColumn time = new ForceHourColumn(data.C_OBSERVATION_TIME, hour);
            dataCopied = data.copyNonStatic(seriesLookup, range, from, to, time) || dataCopied;
        }
        bombUnless(dataCopied, "no data to arbitrate!");
    }

    private boolean copyNonStatic(SelectOne<Integer> seriesLookup, Range range, DataSource from, DataSource to, Column<java.sql.Timestamp> time) {
        List<Column<?>> columns = Column.columns(
            C_TIME_SERIES_ID, 
            new ConstantColumn<Integer>(to.id()), 
            time, 
            C_OBSERVATION_VALUE
        );
        Clause todayValues = seriesSourceDateMatches(seriesLookup, from, range);
        SelectMultiple select = select(columns, todayValues);
        if(todayValues.isEmpty()) return false;
        try {
            insert(select);
            return true;
        } catch (RuntimeException e) {
            throw bomb(
                "cannot arbitrate over existing data!\n" + 
                "If this is really what you want to do, re-run manually with delete_existing set to true."
            , e);
        }
    }

    public static boolean hasData(int id) {
         return forSeriesId(id).C_TIME_SERIES_ID.is(id).exists(); 
    }

    public static void purgeAllData(TimeSeries series) {
        forSeriesId(series.id()).purgeData(series);
    }

    private void purgeData(TimeSeries series) {
        deleteAll(series.is(C_TIME_SERIES_ID));
    }

    public static SeriesIdsBase populateSeriesLookupTable(Range range, DataSource source, AttributeValues values) {
        T_TIME_SERIES_DATA.C_TIME_SERIES_ID.select(FALSE).intoTemp("series_ids");
//        new Generator().writeFile(ids.schemaTable(), "temptables");
        List<String> dataTables = TIME_SERIES.dataTables(values);
        for (String dataTable : dataTables) {
            TimeSeriesDataTable data = forName(dataTable);
            data.populateSeriesLookup(range, source, values);
        }
        return T_SERIES_IDS;
    }

    private void populateSeriesLookup(Range range, DataSource source, AttributeValues values) {
        Clause dateMatches = range.matches(C_OBSERVATION_TIME);
        Clause sourceMatches = source.is(C_DATA_SOURCE_ID);
        Clause valuesMatch = TSAM.timeSeriesIdMatches(values);
        Clause seriesMatches = C_TIME_SERIES_ID.in(TSAM.C_TIME_SERIES_ID.select(valuesMatch));
        Clause dataMatches = dateMatches.and(sourceMatches).and(seriesMatches);
        SelectMultiple ids = select(Column.columns(C_TIME_SERIES_ID), dataMatches);
        T_SERIES_IDS.insert(ids);
    }

    public void createPrimaryKey() {
        schemaTable().createPrimaryKey(C_TIME_SERIES_ID, C_DATA_SOURCE_ID, C_OBSERVATION_TIME);
    }

    public static TimeSeriesDataTable createIfNeeded(String name) {
        TimeSeriesDataTable data = withName(name);
        if(data.schemaTable().exists()) return data;
        data.schemaTable().create();
        addIndicesAndKeys(data);
        return data;
    }

    public static void addIndicesAndKeys(TimeSeriesDataTable data) {
        data.createPrimaryKey();
        data.createForeignKeys();
    }

    private void createForeignKeys() {
        Db.execute(
            "alter table " + shortName() + " add  constraint fk_" + shortName() + "_data_source" +
            " foreign key(data_source_id) references data_source (data_source_id)"
        );
        Db.execute(
            "alter table " + shortName() + " add  constraint fk_" + shortName() + "_time_series" +
            " foreign key(time_series_id) references time_series (time_series_id)"
        );
    }

    public static void deletePoint(DataSource source, TimeSeries series, Date date) {
        TimeSeriesDataTable tsd = forSeriesId(series.id());
        Clause matches = tsd.seriesMatches(series).and(tsd.sourceMatches(source)).and(tsd.C_OBSERVATION_TIME.is(date));
        tsd.deleteOne(matches);
    }
}
