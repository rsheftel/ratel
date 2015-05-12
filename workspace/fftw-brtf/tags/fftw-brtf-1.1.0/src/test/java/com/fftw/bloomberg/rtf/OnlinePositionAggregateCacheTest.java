package com.fftw.bloomberg.rtf;

import org.testng.annotations.*;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.PositionKey;

import java.math.BigDecimal;

/**
 * OnlinePositionAggregateCache Tester.
 *
 * @author mfranz
 * @version $Revision$, $Date$
 * @created June 9, 2008
 * @since 1.0
 */
public class OnlinePositionAggregateCacheTest {
    @Test
    public void testGetOnlinePosition() {
        //TODO: Test goes here...
        assert false : "testGetOnlinePosition not implemented.";
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testAddOnlinePosition() {
        OnlinePositionAggregateCache cache = new OnlinePositionAggregateCache();
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4");
        bp.setPrimeBroker("PB1");

        bp.setOpenPosition(new BigDecimal("78.907"));
        bp.setCurrentPosition(new BigDecimal("12.5"));
        bp.setTotalBuyVolume(new BigDecimal("6888.0"));
        bp.setTotalSellVolume(new BigDecimal("-6888"));
        bp.setRealizedPL(new BigDecimal("67690"));

        bp.setTotalNumberOfBuys(4);
        bp.setTotalNumberOfSells(3);

        cache.addOnlinePosition(bp);

        RtfOnlinePosition cacheItem = cache.getOnlinePosition(cache.getKeyForItem(bp));

        assert (cacheItem.getOpenPosition().compareTo(new BigDecimal("78.907")) == 0
                && cacheItem.getCurrentPosition().compareTo(new BigDecimal("12.5")) == 0
                && cacheItem.getRealizedPL().compareTo(new BigDecimal("67690")) == 0
                && cacheItem.getTotalBuyVolume().compareTo(new BigDecimal("6888")) == 0
                && cacheItem.getTotalSellVolume().compareTo(new BigDecimal("-6888")) == 0
                && cacheItem.getTotalNumberOfBuys() == 4
                && cacheItem.getTotalNumberOfSells() == 3
                && cacheItem.getCurrentAvgCost().compareTo(BigDecimal.ZERO) == 0
                && cacheItem.getOpenAverageCost().compareTo(BigDecimal.ZERO) == 0
        ) : "Failed to aggregate";
        RtfOnlinePosition other = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4");

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

        cacheItem = cache.getOnlinePosition(cache.getKeyForItem(bp));

        assert (cacheItem.getOpenPosition().compareTo(new BigDecimal("157.807")) == 0
                && cacheItem.getCurrentPosition().compareTo(new BigDecimal("24.89")) == 0
                && cacheItem.getRealizedPL().compareTo(new BigDecimal("135650")) == 0
                && cacheItem.getTotalBuyVolume().compareTo(new BigDecimal("13775")) == 0
                && cacheItem.getTotalSellVolume().compareTo(new BigDecimal("-13766")) == 0
                && cacheItem.getTotalNumberOfBuys() == 11
                && cacheItem.getTotalNumberOfSells() == 12
                && cacheItem.getCurrentAvgCost().compareTo(BigDecimal.ZERO) == 0
                && cacheItem.getOpenAverageCost().compareTo(BigDecimal.ZERO) == 0
        ) : "Failed to aggregate";

    }

}
