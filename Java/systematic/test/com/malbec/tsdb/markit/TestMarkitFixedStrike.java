package com.malbec.tsdb.markit;

import static db.clause.Clause.*;
import static tsdb.Attribute.*;
import static tsdb.TSAMTable.*;
import static tsdb.TimeSeries.*;
import static util.Dates.*;

import java.util.*;

import tsdb.*;

import com.malbec.tsdb.loader.*;

import db.clause.*;

public class TestMarkitFixedStrike extends LoaderTestCase {
    public static final Date TEST_DATE = date("2009/04/08 15:00:00"); 
    
    public void testCanLoadTicker() throws Exception {
        Clause firstTickerMatches = CDS_TICKER.value("tlm_snrfor_usd_xr").matches(TSAM.C_ATTRIBUTE_ID, TSAM.C_ATTRIBUTE_VALUE_ID);
        Clause secondTickerMatches = CDS_TICKER.value("oge-okgas+elec_snrfor_usd_mr").matches(TSAM.C_ATTRIBUTE_ID, TSAM.C_ATTRIBUTE_VALUE_ID);
        Clause thirdTickerMatches = CDS_TICKER.value("dyn-holdings_secdom_usd_xr").matches(TSAM.C_ATTRIBUTE_ID, TSAM.C_ATTRIBUTE_VALUE_ID);
        Clause tickerMatches = parenGroup(parenGroup(firstTickerMatches).or(parenGroup(secondTickerMatches)).or(parenGroup(thirdTickerMatches)));
        FixedStrikeLoader loader = new FixedStrikeLoader("test/com/malbec/tsdb/markit/markitFixedStrike.csv", TEST_SOURCE, tickerMatches);
        loader.load();
        assertObservation("tlm_snrfor_usd_xr_100_price_5y", 0.0577904533989267, TEST_DATE);
        AttributeValues values = series("tlm_snrfor_usd_xr_100_price_5y").attributes();
        assertAttribute(values, QUOTE_TYPE, "price");
        assertAttribute(values, CDS_STRIKE, "100");
        assertAttribute(values, CDS_TICKER, "tlm_snrfor_usd_xr");
    }

    private void assertAttribute(AttributeValues values, Attribute attribute, String expected) {
        assertEquals(expected, values.get(attribute).name());
    }

}
