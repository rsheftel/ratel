package org.ratel.tsdb;

import static org.ratel.db.columns.BinaryOperatorColumn.*;
import static org.ratel.db.columns.ConstantColumn.*;
import static org.ratel.db.tables.TSDB.TimeSeriesDataBase.*;
import static org.ratel.tsdb.Attribute.*;
import static org.ratel.tsdb.AttributeValues.*;
import static org.ratel.tsdb.DataSource.*;
import static org.ratel.tsdb.TimeSeriesDataTable.*;
import static org.ratel.tsdb.TimeSeriesGroupTable.*;
import static org.ratel.tsdb.TimeSeriesTable.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Log.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Range.*;
import static org.ratel.util.Sequence.*;
import org.ratel.tsdb.TimeSeriesGroupTable.*;

import org.ratel.tsdb.markit.*;

import org.ratel.db.*;
import org.ratel.db.clause.*;

public class TestTsdbPerformance extends DbTestCase {
    
    public void testAllTestsAreFunctionalOrSlow() throws Exception {
    
    }
    
    // took 22812 millis - same (22834 millis)
    public void slowtestControlFullSeries() throws Exception {
        AttributeValues values = values(  
            INSTRUMENT.value("cds"),
            CDS_TICKER.value("gm_snrfor_usd_mr")
        );
        TsdbObservations obs = observationsMap(INTERNAL, values);
        info("total rows: " + obs.totalRows());
    }
    
    public void slowtestControlOneDay() throws Exception {
        Db.setQueryTimeout(300);
        Clause isCds = TIME_SERIES.C_DATA_TABLE.like("time_series_data_cds_%");
        TIME_SERIES.C_DATA_TABLE.updateAll(isCds, T_TIME_SERIES_DATA.shortName());
        AttributeValues values = values(
            INSTRUMENT.value("cds"),
            TENOR.value("5y")
        );
        TsdbObservations obs = observationsMap(INTERNAL, onDayOf("2008/03/03"), values);
        info("total rows: " + obs.totalRows());
    }
    
    
    public void slowtestCdsByTenorOneDay() throws Exception {
        AttributeValues values = values(
            INSTRUMENT.value("cds"),
            TENOR.value("5y")
        );
        TsdbObservations obs = observationsMap(INTERNAL, onDayOf("2008/03/03"), values);
        info("total rows: " + obs.totalRows());
    }

    public void splitCdsByTenor() throws Exception {
        Db.getOutOfNoCommitTestMode();
        Db.setQueryTimeout(300);
        AttributeValues values = values(
            INSTRUMENT.value("cds"),
            TENOR.value("5y")
        );
        for (String tenor : MarkitLoader.TENORS) {
            if(list("6m", "1y", "2y", "3y", "4y", "5y").contains(tenor)) continue;
            values.replace(TENOR.value(tenor));
            split("TSDB..time_series_data_cds_" + tenor, values);
        }
    }
    
    public void split(String targetName, AttributeValues values) {
        TimeSeriesDataTable data = new TimeSeriesDataTable(targetName, "notNeeded");
        TimeSeriesGroup group;
        if(!targetName.equals("TSDB..time_series_data_cds_7y")) {
            data.schemaTable().create();
            data.createPrimaryKey();
            group = GROUPS.insert(targetName, values);
        } else {
            group = GROUPS.get(targetName);
        }
        SelectOne<Integer> seriesLookup = group.seriesLookup(now());
        TSAMTable tsam2 = TSAMTable.alias("our_tsam");
        for (int i : sequence(0, 99)) {
            if(targetName.equals("TSDB..time_series_data_cds_7y") && i < 10) continue;
            Clause mod = mod(tsam2.C_TIME_SERIES_ID, constant(100)).is(i);
            SelectOne<Integer> select2 = tsam2.C_TIME_SERIES_ID
                .select(tsam2.C_TIME_SERIES_ID.in(seriesLookup).and(mod));
            SelectMultiple select = T_TIME_SERIES_DATA.select(T_TIME_SERIES_DATA.C_TIME_SERIES_ID.in(select2));
            data.insert(select);
            Db.commit();
        }
        Clause seriesRowInGroup = TIME_SERIES.C_TIME_SERIES_ID.in(seriesLookup);
        TIME_SERIES.C_DATA_TABLE.updateAll(seriesRowInGroup, data.shortName());
        Db.commit();
    }
}
