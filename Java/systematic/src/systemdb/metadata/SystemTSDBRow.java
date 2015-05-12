package systemdb.metadata;

import static systemdb.data.bars.BasicBarSmith.*;

import java.util.*;

import systemdb.data.*;
import systemdb.data.bars.*;
import tsdb.*;
import util.*;
import db.*;
import db.clause.*;
import db.columns.*;

public class SystemTSDBRow extends Row implements HistoricalDailyData {
    private static final long serialVersionUID = 1L;
    private final SystemTSDBTable table;

    public SystemTSDBRow(SystemTSDBTable table, Clause matches) {
        super(table.row(matches));
        this.table = table;
    }

    private SeriesSource ss(NvarcharColumn column) {
        return source().with(series(column));
    }

    private TimeSeries seriesMaybe(NvarcharColumn column) {
        if (isEmpty(column)) return null;
        return series(column);
    }

    private TimeSeries series(NvarcharColumn column) {
        return new TimeSeries(value(column));
    }

    private DataSource source() {
        return new DataSource(value(table.C_DATA_SOURCE));
    }

    @Override public List<Bar> lastBars(int count) {
        return barDefinition().bars(count);
    }

    @Override public List<Bar> bars(Range range) {
        return barDefinition().bars(range);
    }

    private DailyTSDBBarLoader barDefinition() {
        return new DailyTSDBBarLoader(this, source(), 
            seriesMaybe(table.C_NAME_OPEN),
            seriesMaybe(table.C_NAME_HIGH),
            seriesMaybe(table.C_NAME_LOW),
            series(table.C_NAME_CLOSE),
            seriesMaybe(table.C_NAME_VOLUME),
            seriesMaybe(table.C_NAME_OPEN_INTEREST),
            calculator(value(table.C_CALCULATE_METHOD))
        );
    }



    @Override public Double lastCloseBefore(Date date) {
        return ss(table.C_NAME_CLOSE).observationValueBefore(date);
    }
}