package com.fftw.positions.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.types.BBProductCode;

public class BatchAggregationStrategyTest {

    @Test(groups = { "unittest" })
    public void testExcelAggregationLogic() {
        BatchPositionAggregationCache<PositionKey> cache = new BatchPositionAggregationCache<PositionKey>(
            new ExcelBatchAggregationStrategy());

        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
            "Level3", "Level4", "PB", new BigDecimal("100"));

        bp.setProductCode(BBProductCode.Equity);
        bp.setFullCurrentNetPosition(new BigDecimal("100"));
        bp.setCurrentLongPosition(new BigDecimal("90"));
        bp.setCurrentShortPosition(new BigDecimal("89"));

        cache.addBatchPosition(bp);
        assertEquals(cache.count(), 1);

        BatchPosition cachedBp = cache.getBatchPosition(cache.getAggregateKeyForItem(bp));

        assertNotNull(cachedBp);
        assertEquals(cachedBp.getFullCurrentNetPosition().compareTo(new BigDecimal("100")), 0);
        assertEquals(cachedBp.getFullCurrentNetPositionWithoutComma().compareTo(new BigDecimal("100")), 0);

        assertEquals(cachedBp.getCurrentLongPosition().compareTo(new BigDecimal("90")), 0);
        assertEquals(cachedBp.getCurrentShortPosition().compareTo(new BigDecimal("89")), 0);

        // Put in an item to aggregate
        BatchPosition other = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
            "Level3", "Level4", "PB1", new BigDecimal("100"));

        other.setProductCode(BBProductCode.Equity);
        other.setFullCurrentNetPosition(new BigDecimal("100"));
        other.setCurrentLongPosition(new BigDecimal("90"));
        other.setCurrentShortPosition(new BigDecimal("89"));

        cache.addBatchPosition(other);
        assertEquals(cache.count(), 1);

        cachedBp = cache.getBatchPosition(cache.getAggregateKeyForItem(other));

        assertNotNull(cachedBp, "Failed to retrieve item from cache");
        assertEquals(cachedBp.getFullCurrentNetPosition().compareTo(new BigDecimal("200")), 0);

        assertEquals(cachedBp.getFullCurrentNetPositionWithoutComma().compareTo(new BigDecimal("200")), 0);
        assertEquals(cachedBp.getCurrentLongPosition().compareTo(new BigDecimal("180")), 0);
        assertEquals(cachedBp.getCurrentShortPosition().compareTo(new BigDecimal("178")), 0);

        // Put in an item to aggregate
        BatchPosition another = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
            "Level3", "Level4", "PB1", new BigDecimal("1000"));

        another.setProductCode(BBProductCode.Equity);
        another.setFullCurrentNetPosition(new BigDecimal("1000"));
        another.setCurrentLongPosition(new BigDecimal("20"));
        another.setCurrentShortPosition(new BigDecimal("2"));

        cache.addBatchPosition(another);
        assertEquals(cache.count(), 1);

        cachedBp = cache.getBatchPosition(cache.getAggregateKeyForItem(other));

        assertNotNull(cachedBp, "Failed to retrieve item from cache");
        assertEquals(cachedBp.getFullCurrentNetPosition().compareTo(new BigDecimal("1200")), 0);

        assertEquals(cachedBp.getFullCurrentNetPositionWithoutComma().compareTo(new BigDecimal("1200")), 0);
        assertEquals(cachedBp.getCurrentLongPosition().compareTo(new BigDecimal("200")), 0);
        assertEquals(cachedBp.getCurrentShortPosition().compareTo(new BigDecimal("180")), 0);
    }
}
