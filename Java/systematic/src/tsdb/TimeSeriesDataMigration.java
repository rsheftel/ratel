package tsdb;

import static db.tables.TSDB.TimeSeriesMigrationCompleteBase.*;
import static java.util.Collections.*;
import static tsdb.TimeSeries.*;
import static tsdb.TimeSeriesTable.*;
import static util.Log.*;

import java.util.*;

import db.*;
import db.clause.*;
import db.tables.TSDB.*;

public class TimeSeriesDataMigration {
    
    /*
     * After the first run, run this to fix the settle/expiry time series, then re-run the load for these time series
     *  insert into time_series_attribute_map 
     *  select time_series_id, 22 as attribute_id, 222 as attribute_value_id
     *  from time_series where data_table = 'time_series_data' and time_series_name like '%_date'
     */

    @SuppressWarnings("deprecation") public static void main(String[] args) {
        debugSql(false);
        TimeSeriesMigrationCompleteBase T = T_TIME_SERIES_MIGRATION_COMPLETE;
        Clause matches = T.notExists(T.C_TIME_SERIES_ID.joinOn(TIME_SERIES));
        List<Integer> ids = TIME_SERIES.C_TIME_SERIES_ID.values(matches);
        sort(ids);
        for (Integer id : ids) {
            String current = TIME_SERIES.dataTable(id);
            TimeSeries series = series(id);
            try {
                String future = dataTable(series.attributes()).shortName();
                if(current.equals(future)) {
                    info("Skipping series " + series + ". Already in correct table " + current);
                    T.insert(T.C_TIME_SERIES_ID.with(id));
                    Db.commit();
                    continue;
                }
                info("Moving series " + series + " to table " + future);
                
                TimeSeriesDataTable data = TimeSeriesDataTable.createIfNeeded(future);
                TimeSeriesDataTable currentDataTable = TimeSeriesDataTable.withName(current);
                Clause seriesMatches = currentDataTable.C_TIME_SERIES_ID.is(id);
                SelectMultiple select = currentDataTable.select(seriesMatches);
                data.insert(select);
                currentDataTable.deleteAll(seriesMatches);
                TIME_SERIES.C_DATA_TABLE.updateOne(series.is(TIME_SERIES.C_TIME_SERIES_ID), future);
            } catch (Exception e) {
                err("Exception encountered on series " + series, e);
                info("Exception encountered on series " + series, e);
                Db.rollback();
            }
            T.insert(T.C_TIME_SERIES_ID.with(id));
            Db.commit();
        }
    }

}
