package com.fftw.bloomberg.rtf;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/**
 * BatchPositionCache Tester.
 *
 * @author mfranz
 * @version $Revision$, $Date$
 * @created June 6, 2008
 * @since 1.0
 */
public class BatchPositionCacheTest {


    @Test(groups =
            {
                    "unittest"
                    })
    public void testAddBatchPosition() {

        BatchPositionCache cache = new BatchPositionCache();
        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4");


        bp.setFullCurrentNetPosition(new BigDecimal("100.23"));
        bp.setFullCurrentNetPositionWithoutComma(new BigDecimal("100.23"));
        bp.setCurrentLongPosition(new BigDecimal("90"));
        bp.setCurrentShortPosition(new BigDecimal("89"));


        cache.addBatchPosition(bp);

        assert cache.size() == 1 : "Failed to add item to cache";
        BatchPosition other = new BatchPosition(new LocalDate(), "IBM", "UnitTest", "Level1", "Level2", "Level3", "Level4");

        other.setFullCurrentNetPosition(new BigDecimal("100.23"));
        other.setFullCurrentNetPositionWithoutComma(new BigDecimal("100.23"));
        other.setCurrentLongPosition(new BigDecimal("90"));
        other.setCurrentShortPosition(new BigDecimal("89"));

        cache.addBatchPosition(other);

        assert cache.size() == 2 : "Failed to add second item to cache";
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testAggregateBatchPosition() {
        BatchPositionCache cache = new BatchPositionCache();
        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4");


        bp.setFullCurrentNetPosition(new BigDecimal("100.23"));
        bp.setFullCurrentNetPositionWithoutComma(new BigDecimal("100.23"));
        bp.setCurrentLongPosition(new BigDecimal("90"));
        bp.setCurrentShortPosition(new BigDecimal("89"));


        cache.aggregateBatchPosition(bp);

        BatchPosition cachedBp = cache.getCacheItem(new PositionKey(bp.getSecurityId(), bp.getAccount(), bp.getLevel1TagName(),
                bp.getLevel2TagName(), bp.getLevel3TagName(), bp.getLevel4TagName()));

        assert (cachedBp.getFullCurrentNetPosition().compareTo(new BigDecimal("100.23")) == 0
                && cachedBp.getFullCurrentNetPositionWithoutComma().compareTo(new BigDecimal("100.23")) == 0
                && cachedBp.getCurrentLongPosition().compareTo(new BigDecimal("90")) == 0
                && cachedBp.getCurrentShortPosition().compareTo(new BigDecimal("89")) == 0) : "Failed to aggregate";

        BatchPosition other = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4");

        other.setFullCurrentNetPosition(new BigDecimal("100.23"));
        other.setFullCurrentNetPositionWithoutComma(new BigDecimal("100.23"));
        other.setCurrentLongPosition(new BigDecimal("90"));
        other.setCurrentShortPosition(new BigDecimal("89"));

        cache.aggregateBatchPosition(other);

        cachedBp = cache.getCacheItem(new PositionKey(bp.getSecurityId(), bp.getAccount(), bp.getLevel1TagName(),
                bp.getLevel2TagName(), bp.getLevel3TagName(), bp.getLevel4TagName()));

        assert (cachedBp.getFullCurrentNetPosition().compareTo(new BigDecimal("100.23")) == 0
                && cachedBp.getFullCurrentNetPositionWithoutComma().compareTo(new BigDecimal("100.23")) == 0
                && cachedBp.getCurrentLongPosition().compareTo(new BigDecimal("180")) == 0
                && cachedBp.getCurrentShortPosition().compareTo(new BigDecimal("178")) == 0) : "Failed to aggregate";

    }

}
