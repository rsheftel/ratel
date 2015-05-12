package cds;

import static cds.CdsTickerUniverseTable.*;
import static db.clause.Clause.*;
import static db.temptables.TSDB.SeriesIdsBase.*;
import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import static tsdb.TSAMTable.*;
import static util.Errors.*;

import java.util.*;

import tsdb.*;
import db.*;

public class MarkitTimeSeriesGroup implements TimeSeriesGroupDefinition {

    
    public static final MarkitTimeSeriesGroup GROUP_MARKIT = new MarkitTimeSeriesGroup();
    
    public static final AttributeValue MARKIT_QUOTE_TYPES = QUOTE_TYPE.value("spread", "price");
    
    public static void clearCache() {
        seriesIdsLoaded = false;
    }

    private static boolean seriesIdsLoaded = false;

    static final AttributeValues attributes = values(
        INSTRUMENT.value("cds"),
        MARKIT_QUOTE_TYPES
    );

    
    @Override public void delete(int id) {}

    @Override public SelectOne<Integer> seriesLookup(int id, Date asOf) {
        if (!lookupPopulated()) populateLookup();
        return T_SERIES_IDS.C_TIME_SERIES_ID.select(TRUE);
    }

    private boolean lookupPopulated() {
        return seriesIdsLoaded;
    }

    private void populateLookup() {
        T_SERIES_IDS.schemaTable().destroyIfExists();
        T_SERIES_IDS.schemaTable().create();
        List<Row> rows = CDS_UNIVERSE.rows(TRUE);
        for (Row ticker : rows) {
            String[] tenors = ticker.value(CDS_UNIVERSE.C_TENORS).split("\\s");
            T_SERIES_IDS.insert(TSAM.select(Column.columns(TSAM.C_TIME_SERIES_ID), TSAM.timeSeriesIdMatches(values(
                INSTRUMENT.value("cds"),
                MARKIT_QUOTE_TYPES,
                TENOR.value(tenors),
                TICKER.value(ticker.value(CDS_UNIVERSE.C_TICKER_ID).intValue()),
                TIER.value(ticker.value(CDS_UNIVERSE.C_TIER_ID)),
                CCY.value(ticker.value(CDS_UNIVERSE.C_CCY_ID))
            ))));
        }
        bombUnless(T_SERIES_IDS.rowExists(TRUE), "no data in series lookup");
        seriesIdsLoaded = true;
    }


}
