package amazon;

import static systemdb.metadata.MsivBacktestTable.*;
import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import static tsdb.DataSource.*;
import static tsdb.TimeSeries.*;
import static tsdb.TimeSeriesDataTable.*;
import static util.Log.*;
import static util.Range.*;
import static util.Times.*;
import systemdb.data.*;
import systemdb.metadata.*;

public class TestsSqsDb extends S3CacheableTestCase {
    
    public void testLittleQuery() throws Exception {
        assertEquals(0.015625, new Market("RE.TEST.TY.1C").fixedSlippage());
    }
    
    public void testCrossJoinFakeSql() throws Exception {
        BACKTEST.range(37859, "PTT10.XLYXLI");
    }
    
    public void testS3Cache() throws Exception {
        // run query through db (load s3)
        new Market("RE.TEST.TY.1C");
        server.stopServer();
        // run qurey with no db server (hit s3)
        new Market("RE.TEST.TY.1C");
    }
    
    public void testFiles() throws Exception {
        assertEquals(5249, new Symbol("RE.TEST.TY.1C").bars().size());
    }
    
    public void slowtestPerformance() throws Exception {
        S3Cache.beInSqsDbMode(false);
        long start = nowMillis();
        new Symbol("PTZ10.EWZEWW").observations();
        info("millis: " + reallyMillisSince(start));
        S3Cache.beInSqsDbMode(true);
        new Symbol("PTZ10.EWZEWW").observations();
        start = nowMillis();
        new Symbol("PTZ10.EWZEWW").observations();
        info("millis: " + reallyMillisSince(start));
    }


    public void slowTestBigQuery() throws Exception {
//        Db.RESULT_BUCKET.clear();
        assertEquals(1110315, series(values(
            TICKER.value("ty200903"),
            CONTRACT.value("ty"),
            QUOTE_CONVENTION.value("price"),
            FUTURE_MONTH.value("3"),
            QUOTE_TYPE.value("close"),
            EXPIRY.value("actual"),
            FUTURE_YEAR.value("2009"),
            INSTRUMENT.value("futures"),
            QUOTE_SIDE.value("mid"),
            FUTURE_MONTH_LETTER.value("h")
        )).id());
    }
    
    public void testObservationByValues() throws Exception {
        assertSize(21, observations(YAHOO, range("2007/01/01", "2007/02/01"), values(TICKER.value("aapl"), QUOTE_TYPE.value("close"))));
    }
    
    public void slowTestBigResultSet() throws Exception {
        series("ty200903_price_mid").observations(INTERNAL);
    }
}
