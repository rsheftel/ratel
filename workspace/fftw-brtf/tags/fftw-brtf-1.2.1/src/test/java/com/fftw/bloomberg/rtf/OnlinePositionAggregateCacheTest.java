package com.fftw.bloomberg.rtf;

import static org.testng.Assert.*;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePositionAggregationStrategy;

/**
 * OnlinePositionAggregateCache Tester.
 * 
 */
public class OnlinePositionAggregateCacheTest {

    @Test(groups = { "unittest" })
    public void testAddOnlinePosition() {
        OnlinePositionAggregateCache cache = new OnlinePositionAggregateCache(new RtfOnlinePositionAggregationStrategy());
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4");
        bp.setPrimeBroker("PB1");

        bp.setOpenPosition(new BigDecimal("78.907"));
        bp.setCurrentPosition(new BigDecimal("12.5"));
        bp.setTotalBuyVolume(new BigDecimal("6888.0"));
        bp.setTotalSellVolume(new BigDecimal("-6888"));
        bp.setRealizedPL(new BigDecimal("67690"));

        bp.setTotalNumberOfBuys(4);
        bp.setTotalNumberOfSells(3);

        cache.addOnlinePosition(bp);

        RtfOnlinePosition cacheItem = cache.getOnlinePosition(cache.getAggregateKeyForItem(bp));

        assertEquals(cacheItem.getOpenPosition().compareTo(new BigDecimal("78.907")), 0);
        assertEquals(cacheItem.getCurrentPosition().compareTo(new BigDecimal("12.5")), 0);
        assertEquals(cacheItem.getRealizedPL().compareTo(new BigDecimal("67690")), 0);
        assertEquals(cacheItem.getTotalBuyVolume().compareTo(new BigDecimal("6888")), 0);
        assertEquals(cacheItem.getTotalSellVolume().compareTo(new BigDecimal("-6888")), 0);
        assertEquals(cacheItem.getTotalNumberOfBuys(), 4);
        assertEquals(cacheItem.getTotalNumberOfSells(), 3);
        assertEquals(cacheItem.getCurrentAvgCost().compareTo(BigDecimal.ZERO), 0);
        assertEquals(cacheItem.getOpenAverageCost().compareTo(BigDecimal.ZERO), 0);

        RtfOnlinePosition other = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4");

        // The PBs must be different to aggregate
        other.setPrimeBroker("PB2");
        other.setOpenPosition(new BigDecimal("78.900"));
        other.setCurrentPosition(new BigDecimal("12.39"));
        other.setRealizedPL(new BigDecimal("67960"));
        other.setTotalBuyVolume(new BigDecimal("6887"));
        other.setTotalSellVolume(new BigDecimal("-6878"));

        other.setTotalNumberOfBuys(7);
        other.setTotalNumberOfSells(9);

        cache.addOnlinePosition(other);

        cacheItem = cache.getOnlinePosition(cache.getAggregateKeyForItem(bp));

        assertEquals(cacheItem.getOpenPosition().compareTo(new BigDecimal("157.807")), 0);
        assertEquals(cacheItem.getCurrentPosition().compareTo(new BigDecimal("24.89")), 0);
        assertEquals(cacheItem.getRealizedPL().compareTo(new BigDecimal("135650")), 0);
        assertEquals(cacheItem.getTotalBuyVolume().compareTo(new BigDecimal("13775")), 0);
        assertEquals(cacheItem.getTotalSellVolume().compareTo(new BigDecimal("-13766")), 0);
        assertEquals(cacheItem.getTotalNumberOfBuys(), 11);
        assertEquals(cacheItem.getTotalNumberOfSells(), 12);
        assertEquals(cacheItem.getCurrentAvgCost().compareTo(BigDecimal.ZERO), 0);
        assertEquals(cacheItem.getOpenAverageCost().compareTo(BigDecimal.ZERO), 0);
    }

}
