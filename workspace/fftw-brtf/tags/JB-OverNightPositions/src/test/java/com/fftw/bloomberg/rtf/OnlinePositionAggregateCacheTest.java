package com.fftw.bloomberg.rtf;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.test.BaseTestNG;

/**
 * OnlinePositionAggregateCache Tester.
 * 
 * @created June 9, 2008
 * @since 1.0
 */
public class OnlinePositionAggregateCacheTest extends BaseTestNG {
    @Test
    public void testGetOnlinePosition() {
        // TODO: Test goes here...
        assert false : "testGetOnlinePosition not implemented.";
    }

    @Test(groups = { "unittest" })
    public void testAddOnlinePosition() {
        OnlinePositionAggregateCache cache = new OnlinePositionAggregateCache();
        RtfOnlinePosition op = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
                "Level4", new BigDecimal("78"));
        op.setPrimeBroker("PB1");

        // op.setOpenPosition(new BigDecimal("78.907"));
        op.setCurrentPosition(new BigDecimal("12.5"));
        op.setTotalBuyVolume(new BigDecimal("6888.0"));
        op.setTotalSellVolume(new BigDecimal("-6888"));
        op.setRealizedPL(new BigDecimal("67690"));

        op.setTotalNumberOfBuys(4);
        op.setTotalNumberOfSells(3);

        cache.addOnlinePosition(op);

        RtfOnlinePosition cacheItem = cache.getOnlinePosition(cache.getKeyForItem(op));

        assertComparable(cacheItem.getOpenPosition(), new BigDecimal("78"), "OpenPosition does not match");
        assertComparable(cacheItem.getCurrentPosition(), new BigDecimal("12.5"),
                "CurrentPosition does not match");
        assertComparable(cacheItem.getRealizedPL(), new BigDecimal("67690"), "RealizedPL does not match");
        assertComparable(cacheItem.getTotalBuyVolume(), new BigDecimal("6888"), "TotalBuyVolume");
        assertComparable(cacheItem.getTotalSellVolume(), new BigDecimal("-6888"), "TotalSellVolume");
        assertEquals(cacheItem.getTotalNumberOfBuys(), 4, "TotalNumberOfBuys");
        assertEquals(cacheItem.getTotalNumberOfSells(), 3, "TotalNumberOfSells");
        assertComparable(cacheItem.getCurrentAvgCost(), BigDecimal.ZERO, "CurrentAverageCost");
        assertComparable(cacheItem.getOpenAverageCost(), BigDecimal.ZERO, "OpenAverageCost");

        RtfOnlinePosition other = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
                "Level4", new BigDecimal("78"));

        // The PBs must be different to aggregate
        other.setPrimeBroker("PB2");
        // other.setOpenPosition(new BigDecimal("78.900"));
        other.setCurrentPosition(new BigDecimal("12.39"));
        other.setRealizedPL(new BigDecimal("67960"));
        other.setTotalBuyVolume(new BigDecimal("6887"));
        other.setTotalSellVolume(new BigDecimal("-6878"));

        other.setTotalNumberOfBuys(7);
        other.setTotalNumberOfSells(9);

        cache.addOnlinePosition(other);

        cacheItem = cache.getOnlinePosition(cache.getKeyForItem(op));

        assertComparable(cacheItem.getOpenPosition(), new BigDecimal("78"), "OpenPosition");
        assertComparable(cacheItem.getCurrentPosition(), new BigDecimal("24.89"), "CurrentPosition");
        assertComparable(cacheItem.getRealizedPL(), new BigDecimal("135650"), "RealizedPL");
        assertComparable(cacheItem.getTotalBuyVolume(), new BigDecimal("13775"), "TotalBuyVolume");
        assertComparable(cacheItem.getTotalSellVolume(), new BigDecimal("-13766"), "TotalSellVolume");
        assertEquals(cacheItem.getTotalNumberOfBuys(), 11, "TotalNumberOfBuys");
        assertEquals(cacheItem.getTotalNumberOfSells(), 12, "TotalNumberOfSells");
        assertComparable(cacheItem.getCurrentAvgCost(), BigDecimal.ZERO, "CurrentAverageCost");
        assertComparable(cacheItem.getOpenAverageCost(), BigDecimal.ZERO, "OpenAverageCost");
    }
}
