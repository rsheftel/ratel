package cds;

import static cds.CdsTickerUniverseTable.*;
import static tsdb.TimeSeries.*;
import static util.Dates.*;

import java.util.*;

import db.*;

public class TestMarkitTimeSeriesGroup extends DbTestCase {

    private static final String NAME1 = "gm_snrfor_usd_mr_spread_5y";
    private static final String NAME2 = "gm_snrfor_usd_mr_spread_10y";
    private Date today = date("2007/09/04");

    public void testCanCreateMarkitGroup() throws Exception {
        CDS_UNIVERSE.resetUniverse("gm", "snrfor", "usd", "5y 10y");
        MarkitTimeSeriesGroup group = new MarkitTimeSeriesGroup();
        SelectOne<Integer> lookup = group.seriesLookup(-1, today);
        List<Integer> ids = lookup.values();
        hasSeries(ids, NAME1);
        hasSeries(ids, NAME2);
        hasSeries(ids, "gm_snrfor_usd_xr_100_spread_5y");
        hasSeries(ids, "gm_snrfor_usd_xr_500_spread_5y");
        hasSeries(ids, "gm_snrfor_usd_xr_100_price_5y");
        hasSeries(ids, "gm_snrfor_usd_xr_500_price_5y");
        hasSeries(ids, "gm_snrfor_usd_xr_500_spread_10y");
        hasSeries(ids, "gm_snrfor_usd_xr_100_spread_10y");
        hasSeries(ids, "gm_snrfor_usd_xr_100_price_10y");
        hasSeries(ids, "gm_snrfor_usd_xr_500_price_10y");
        noSeries(ids, "gm_snrfor_usd_xr_100_price_1y");
        noSeries(ids, "gm_snrfor_usd_xr_500_price_1y");
        noSeries(ids, "aa_snrfor_usd_xr_100_price_10y");
        noSeries(ids, "aa_snrfor_usd_xr_500_price_10y");
        
    }

    private void hasSeries(List<Integer> ids, String name) {
        assertContains(series(name).id(), ids);
    }
    
    private void noSeries(List<Integer> ids, String name) {
        assertDoesNotContain(series(name).id(), ids);
    }

    
}
