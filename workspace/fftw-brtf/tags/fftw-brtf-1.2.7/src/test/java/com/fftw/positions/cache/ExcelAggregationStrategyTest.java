package com.fftw.positions.cache;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.types.BBProductCode;

public class ExcelAggregationStrategyTest {

    @Test(groups = { "unittest" })
    public void testAddOnlinePosition() {
        
        // Test aggregation only PB is different, then change the open position
        OnlinePositionAggregationCache<OnlinePositionKey> cache = new OnlinePositionAggregationCache<OnlinePositionKey>(new ExcelOnlineAggregationStrategy());
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "PB1", BigDecimal.ZERO);
        
        bp.setProductCode(BBProductCode.Equity);
        bp.setCurrentPosition(new BigDecimal("12.5"));
        bp.setTotalBuyVolume(new BigDecimal("6888.0"));
        bp.setTotalSellVolume(new BigDecimal("-6888"));
        bp.setRealizedPL(new BigDecimal("67690"));

        bp.setTotalNumberOfBuys(4);
        bp.setTotalNumberOfSells(3);

        cache.addOnlinePosition(bp);

        RtfOnlinePosition cacheItem = cache.getOnlinePosition(cache.getAggregateKeyForItem(bp));

        assertEquals(cacheItem.getOpenPosition().compareTo(BigDecimal.ZERO), 0);
        assertEquals(cacheItem.getCurrentPosition().compareTo(new BigDecimal("12.5")), 0);
        assertEquals(cacheItem.getRealizedPL().compareTo(new BigDecimal("67690")), 0);
        assertEquals(cacheItem.getTotalBuyVolume().compareTo(new BigDecimal("6888")), 0);
        assertEquals(cacheItem.getTotalSellVolume().compareTo(new BigDecimal("-6888")), 0);
        assertEquals(cacheItem.getTotalNumberOfBuys(), 4);
        assertEquals(cacheItem.getTotalNumberOfSells(), 3);
        assertEquals(cacheItem.getCurrentAvgCost().compareTo(BigDecimal.ZERO), 0);
        assertEquals(cacheItem.getOpenAverageCost().compareTo(BigDecimal.ZERO), 0);

        // Record with different PB
        RtfOnlinePosition other = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "BP2", BigDecimal.ZERO);
        
        other.setProductCode(BBProductCode.Equity);
        other.setCurrentPosition(new BigDecimal("12.39"));
        other.setRealizedPL(new BigDecimal("67960"));
        other.setTotalBuyVolume(new BigDecimal("6887"));
        other.setTotalSellVolume(new BigDecimal("-6878"));

        other.setTotalNumberOfBuys(7);
        other.setTotalNumberOfSells(9);

        cache.addOnlinePosition(other);

        cacheItem = cache.getOnlinePosition(cache.getAggregateKeyForItem(bp));

        assertEquals(cacheItem.getOpenPosition().compareTo(BigDecimal.ZERO), 0);
        assertEquals(cacheItem.getCurrentPosition().compareTo(new BigDecimal("24.89")), 0);
        assertEquals(cacheItem.getRealizedPL().compareTo(new BigDecimal("135650")), 0);
        assertEquals(cacheItem.getTotalBuyVolume().compareTo(new BigDecimal("13775")), 0);
        assertEquals(cacheItem.getTotalSellVolume().compareTo(new BigDecimal("-13766")), 0);
        assertEquals(cacheItem.getTotalNumberOfBuys(), 11);
        assertEquals(cacheItem.getTotalNumberOfSells(), 12);
        assertEquals(cacheItem.getCurrentAvgCost().compareTo(BigDecimal.ZERO), 0);
        assertEquals(cacheItem.getOpenAverageCost().compareTo(BigDecimal.ZERO), 0);
        
        // Record with different open position
        RtfOnlinePosition open = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "BP2", BigDecimal.ONE);

        open.setProductCode(BBProductCode.Equity);
        open.setCurrentPosition(new BigDecimal("12.39"));
        open.setRealizedPL(new BigDecimal("67960"));
        open.setTotalBuyVolume(new BigDecimal("6887"));
        open.setTotalSellVolume(new BigDecimal("-6878"));

        open.setTotalNumberOfBuys(7);
        open.setTotalNumberOfSells(9);

        cache.addOnlinePosition(open);

        cacheItem = cache.getOnlinePosition(cache.getAggregateKeyForItem(bp));

        assertEquals(cacheItem.getOpenPosition().compareTo(BigDecimal.ONE), 0);
        assertEquals(cacheItem.getCurrentPosition().compareTo(new BigDecimal("37.28")), 0);
        assertEquals(cacheItem.getRealizedPL().compareTo(new BigDecimal("203610")), 0);
        assertEquals(cacheItem.getTotalBuyVolume().compareTo(new BigDecimal("20662")), 0);
        assertEquals(cacheItem.getTotalSellVolume().compareTo(new BigDecimal("-20644")), 0);
        assertEquals(cacheItem.getTotalNumberOfBuys(), 18);
        assertEquals(cacheItem.getTotalNumberOfSells(), 21);
        assertEquals(cacheItem.getCurrentAvgCost().compareTo(BigDecimal.ZERO), 0);
        assertEquals(cacheItem.getOpenAverageCost().compareTo(BigDecimal.ZERO), 0);
    }

}
